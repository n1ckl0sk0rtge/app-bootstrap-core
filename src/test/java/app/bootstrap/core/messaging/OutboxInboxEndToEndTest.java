/*
 * App Bootstrap Core
 * Copyright (C) 2026
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.bootstrap.core.messaging;

import static org.junit.jupiter.api.Assertions.*;

import app.bootstrap.core.ddd.AggregateRoot;
import app.bootstrap.core.ddd.DomainEvent;
import app.bootstrap.core.ddd.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * End-to-end exercise of the reliable-messaging loop after the inbox became a durable per-consumer
 * mailbox: an aggregate stages an event in the {@link IOutbox} on the write side, a relay drains
 * the outbox and {@link IInbox#receive(IEvent) delivers} it into every subscriber's inbox, and each
 * consumer's projector drains its inbox with {@link IInbox#fetchUnprocessed(int)} and applies the
 * side effect under {@link IInbox#markProcessed(UUID)}.
 *
 * <p>The point the tests make together: fault tolerance hands off from the outbox to the inboxes at
 * delivery time. The outbox survives only until {@code receive} has committed to <em>every</em>
 * subscriber inbox; from there each inbox is its own durable replay source, and the
 * received&nbsp;&rarr;&nbsp;processed lifecycle plus tombstone-on-process makes the side effect
 * effectively-once even under fan-out redelivery.
 */
class OutboxInboxEndToEndTest {

    // ---- A minimal aggregate + domain event (the write side) -------------------------------

    static final class OrderId extends Id {
        OrderId() {
            super(UUID.randomUUID());
        }
    }

    static final class OrderConfirmed extends DomainEvent {
        OrderConfirmed(OrderId aggregateId) {
            super(aggregateId, Order.class);
        }
    }

    static final class Order extends AggregateRoot<OrderId> {
        Order(OrderId id) {
            super(id);
        }

        void confirm() {
            apply(new OrderConfirmed(getId()));
        }
    }

    // ---- A consumer: its own durable inbox, projector, and read model ----------------------

    /**
     * One subscriber. Its inbox is a durable mailbox; its projector drains received-but-unprocessed
     * events oldest-first and applies a guarded side effect, marking each processed in
     * (conceptually) the same transaction.
     */
    static final class Consumer {
        final InMemoryInbox inbox = new InMemoryInbox();
        final List<String> readModel = new ArrayList<>();
        final AtomicInteger sideEffectRuns = new AtomicInteger();

        /** Projector tick: drain the inbox and apply each event exactly once. */
        void project() {
            for (IEvent event : inbox.fetchUnprocessed(100)) {
                if (inbox.alreadyProcessed(event.getEventId())) {
                    continue; // defensive against a concurrent drain
                }
                sideEffectRuns.incrementAndGet();
                readModel.add(((OrderConfirmed) event).getAggregateId().toString());
                inbox.markProcessed(event.getEventId());
            }
        }
    }

    private InMemoryOutbox outbox;

    @BeforeEach
    void setUp() {
        outbox = new InMemoryOutbox();
    }

    /**
     * Write side: stage the aggregate's events in the outbox (would share the state-change txn).
     */
    private UUID confirmAndStage(Order order) {
        order.confirm();
        // Capture the event id before commit() clears the uncommitted changes.
        UUID eventId = order.getUncommittedChanges().get(0).getEventId();
        order.commit(outbox::add);
        return eventId;
    }

    /**
     * Relay tick: fetch staged events and deliver each into every listed inbox via {@code receive}
     * (idempotent on event id). The relay marks the batch published only when {@code ackOutbox} is
     * {@code true} — which the caller passes only once it knows every subscriber inbox has
     * received, the "ack after receive to all inboxes, not after a bus publish" rule. Passing
     * {@code false} models an ack the relay has not yet committed (a crash before {@link
     * IOutbox#markPublished(List)}, or a sibling inbox that has not received), which keeps the
     * outbox row alive and forces a redelivery to <em>all</em> {@code receivers} — including ones
     * that have already received and processed the event.
     */
    private void relayTick(List<Consumer> receivers, boolean ackOutbox) {
        List<IEvent> batch = outbox.fetchUnpublished(100);
        for (IEvent event : batch) {
            for (Consumer consumer : receivers) {
                consumer.inbox.receive(event);
            }
        }
        if (ackOutbox) {
            outbox.markPublished(batch.stream().map(IEvent::getEventId).toList());
        }
    }

    // ---- Tests -----------------------------------------------------------------------------

    @Test
    void shouldDeliverEventEndToEndAndApplyExactlyOnce() {
        Consumer consumer = new Consumer();
        Order order = new Order(new OrderId());

        UUID eventId = confirmAndStage(order);
        assertEquals(1, outbox.size(), "event staged in the outbox by the write side");

        relayTick(List.of(consumer), true);
        assertEquals(0, outbox.size(), "outbox drops the event once every inbox has received it");
        assertEquals(1, consumer.inbox.size(), "event durably staged in the consumer's inbox");

        consumer.project();

        assertEquals(1, consumer.sideEffectRuns.get(), "side effect ran once");
        assertEquals(List.of(order.getId().toString()), consumer.readModel);
        assertTrue(
                consumer.inbox.alreadyProcessed(eventId), "inbox recorded the event as processed");
        assertEquals(0, consumer.inbox.unprocessedCount(), "nothing left to drain");
    }

    @Test
    void shouldReplayFromInboxAcrossAProjectorRestart() {
        Consumer consumer = new Consumer();
        Order order = new Order(new OrderId());
        confirmAndStage(order);

        // Delivered durably into the inbox, but the projector crashed before draining.
        relayTick(List.of(consumer), true);
        assertEquals(1, consumer.inbox.unprocessedCount(), "received but not yet processed");
        assertEquals(0, consumer.sideEffectRuns.get(), "projector had not run yet");

        // The inbox is the durable replay source: on restart the projector drains it and catches
        // up.
        consumer.project();

        assertEquals(1, consumer.sideEffectRuns.get(), "projector replayed the staged event");
        assertEquals(List.of(order.getId().toString()), consumer.readModel);
    }

    @Test
    void shouldBeIdempotentWhenRelayRedeliversBeforeOutboxAck() {
        Consumer consumer = new Consumer();
        Order order = new Order(new OrderId());
        confirmAndStage(order);

        // First tick: the inbox receives, but the relay crashes before the outbox ack (ackOutbox =
        // false), so the event stays staged for redelivery.
        relayTick(List.of(consumer), false);
        consumer.project();
        assertEquals(1, consumer.sideEffectRuns.get(), "side effect ran on first delivery");
        assertEquals(1, outbox.size(), "unacknowledged event still staged for redelivery");

        // Second tick redelivers the same event into the inbox; receive is a no-op on the processed
        // tombstone, so the projector finds nothing new to apply.
        relayTick(List.of(consumer), true);
        consumer.project();
        assertEquals(
                1, consumer.sideEffectRuns.get(), "redelivery did not run the side effect again");
        assertEquals(
                List.of(order.getId().toString()), consumer.readModel, "read model not doubled");
        assertEquals(0, outbox.size(), "outbox drained once the event reached the inbox");
    }

    @Test
    void shouldNotDoubleApplyUnderFanOutRedeliveryWhenASiblingInboxLags() {
        // Two subscribers fan out off one outbox. The relay cannot ack until BOTH inboxes have
        // received; while one lags, the outbox redelivers the event to BOTH — and the consumer that
        // already processed it must not apply it twice.
        Consumer fast = new Consumer();
        Consumer slow = new Consumer();
        Order order = new Order(new OrderId());
        confirmAndStage(order);

        // Tick 1: only fast receives; slow lags, so the relay cannot ack and the event stays
        // staged.
        relayTick(List.of(fast), false);
        fast.project();
        assertEquals(1, fast.sideEffectRuns.get(), "fast consumer applied it");
        assertEquals(1, outbox.size(), "outbox still holds the event for the lagging sibling");

        // Tick 2: both receive now. The redelivery into `fast` lands on a processed tombstone (a
        // no-op); `slow` receives it for the first time. Only now can the relay ack the outbox.
        relayTick(List.of(fast, slow), true);
        fast.project();
        slow.project();

        assertEquals(
                1, fast.sideEffectRuns.get(), "fan-out redelivery did not double-apply on fast");
        assertEquals(1, slow.sideEffectRuns.get(), "slow consumer applied it exactly once");
        assertEquals(
                List.of(order.getId().toString()), fast.readModel, "fast read model not doubled");
        assertEquals(List.of(order.getId().toString()), slow.readModel);
        assertEquals(0, outbox.size(), "outbox drops the event only after every inbox received it");
    }
}
