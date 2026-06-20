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
 * End-to-end exercise of the reliable-messaging loop the two contracts exist for: an aggregate
 * stages an event in the {@link IOutbox} on the write side, a relay drains the outbox to an {@link
 * IEventBus}, and an inbox-guarded consumer applies a side effect with {@link IInbox} dedupe.
 *
 * <p>The point the tests make together: {@code IOutbox} gives <em>at-least-once</em> delivery (a
 * relay can redeliver after crashing before it acknowledges), and {@code IInbox} turns that into an
 * <em>effectively-once</em> side effect on the consumer.
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

    // ---- Infrastructure under test ---------------------------------------------------------

    private InMemoryOutbox outbox;
    private InMemoryInbox inbox;
    private InMemoryEventBus eventBus;

    /** Stand-in read model the consumer maintains: the order ids it has confirmed. */
    private List<String> confirmedReadModel;

    /** How many times the guarded side effect actually ran — the value dedupe protects. */
    private AtomicInteger sideEffectRuns;

    @BeforeEach
    void setUp() {
        outbox = new InMemoryOutbox();
        inbox = new InMemoryInbox();
        eventBus = new InMemoryEventBus();
        confirmedReadModel = new ArrayList<>();
        sideEffectRuns = new AtomicInteger();

        // The consumer: guard the side effect with the inbox so a redelivered event is skipped.
        eventBus.subscribe(
                OrderConfirmed.class,
                event -> {
                    if (inbox.alreadyProcessed(event.getEventId())) {
                        return; // a redelivery of an event we have already applied — skip
                    }
                    sideEffectRuns.incrementAndGet();
                    confirmedReadModel.add(event.getAggregateId().toString());
                    inbox.markProcessed(event.getEventId());
                });
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
     * Relay tick: fetch staged events, publish each to the bus, and — only if {@code acknowledge} —
     * mark them published. Passing {@code false} models a relay that crashes after dispatch but
     * before {@link IOutbox#markPublished}, which is exactly what forces a later redelivery.
     */
    private void relayTick(boolean acknowledge) {
        List<IEvent> batch = outbox.fetchUnpublished(100);
        batch.forEach(eventBus::publish);
        if (acknowledge) {
            outbox.markPublished(batch.stream().map(IEvent::getEventId).toList());
        }
    }

    // ---- Tests -----------------------------------------------------------------------------

    @Test
    void shouldDeliverEventEndToEndAndApplyExactlyOnce() {
        Order order = new Order(new OrderId());

        UUID eventId = confirmAndStage(order);
        assertEquals(1, outbox.size(), "event staged in the outbox by the write side");

        relayTick(true);

        assertEquals(1, sideEffectRuns.get(), "side effect ran once");
        assertEquals(List.of(order.getId().toString()), confirmedReadModel);
        assertEquals(0, outbox.size(), "outbox drained after acknowledge");
        assertTrue(inbox.alreadyProcessed(eventId), "consumer recorded the event as processed");
        assertEquals(1, inbox.size());
    }

    @Test
    void shouldNotDoubleApplyWhenRelayRedeliversAfterCrashBeforeAck() {
        Order order = new Order(new OrderId());
        confirmAndStage(order);

        // First tick dispatches but never acknowledges — the relay "crashed" before markPublished.
        relayTick(false);
        assertEquals(1, sideEffectRuns.get(), "side effect ran on first delivery");
        assertEquals(1, outbox.size(), "unacknowledged event is still staged for redelivery");

        // Second tick redelivers the same event (at-least-once); the inbox makes it a no-op.
        relayTick(true);
        assertEquals(1, sideEffectRuns.get(), "redelivery did not run the side effect again");
        assertEquals(
                List.of(order.getId().toString()), confirmedReadModel, "read model not doubled");
        assertEquals(0, outbox.size(), "outbox drained once the relay acknowledged");
    }

    @Test
    void shouldApplyEachDistinctEventOnceAcrossARedeliveringRelay() {
        Order first = new Order(new OrderId());
        Order second = new Order(new OrderId());
        confirmAndStage(first);
        confirmAndStage(second);

        // A flaky relay: dispatch-without-ack, then a clean tick that redelivers both and acks.
        relayTick(false);
        relayTick(true);

        assertEquals(2, sideEffectRuns.get(), "each distinct event applied exactly once");
        assertEquals(
                List.of(first.getId().toString(), second.getId().toString()),
                confirmedReadModel,
                "both orders confirmed, in staging order, no duplicates");
        assertEquals(2, inbox.size());
        assertEquals(0, outbox.size());
    }
}
