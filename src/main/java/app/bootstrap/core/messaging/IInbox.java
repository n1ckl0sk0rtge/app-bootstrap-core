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

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * Per-consumer durable mailbox — the consumer-side half of reliable messaging.
 *
 * <p>Where {@link IOutbox} makes the <em>producer</em> reliable by staging events in the write
 * transaction, the inbox makes a <em>consumer</em> reliable by durably staging every event it
 * receives and tracking it through a <strong>received&nbsp;&rarr;&nbsp;processed</strong>
 * lifecycle. Each row records both that the consumer received an event and whether it has yet
 * applied it, so the inbox is itself the consumer's durable replay source: a projector that
 * restarts asks its inbox "what did I receive but not yet apply?" and resumes from there.
 *
 * <p><strong>Where fault tolerance hands off.</strong> The outbox only has to survive until the
 * event is durably in <em>every</em> subscriber's inbox; the relay may drop the outbox row the
 * moment {@link #receive} has committed to all of them. From that point each inbox is its own
 * durable replay source — delivery responsibility has moved from producer to consumer.
 *
 * <p><strong>Lifecycle of one row.</strong>
 *
 * <pre>{@code
 * receive(event)        -> row { eventId, payload, seq, processed=false }   (idempotent on eventId)
 * fetchUnprocessed(n)   -> rows where processed=false, oldest-first by seq
 * markProcessed(id)     -> flips processed=true   (same txn as the side effect)
 * <retention reaper>    -> deletes processed rows older than a window
 * }</pre>
 *
 * <p><strong>Two ends.</strong> {@link #receive} is called by the delivery/relay side; {@link
 * #fetchUnprocessed}, {@link #alreadyProcessed}, and {@link #markProcessed} by the consumer side.
 * The consumer drains its inbox on each tick and on startup:
 *
 * <pre>{@code
 * // consumer tick / startup recovery — drain received-but-unprocessed events, oldest first
 * for (IEvent event : inbox.fetchUnprocessed(100)) {
 *     // runs inside one transaction with the side effect it guards
 *     if (inbox.alreadyProcessed(event.getEventId())) {
 *         continue; // a concurrent drain already applied it — skip
 *     }
 *     applyTheSideEffect(event);                 // upsert a read model, call a collaborator, ...
 *     inbox.markProcessed(event.getEventId());   // same transaction as the side effect
 * }
 * }</pre>
 *
 * <p><strong>Atomicity.</strong> {@link #markProcessed} must commit in the <em>same
 * transaction</em> as the side effect it guards. If the side effect commits but the mark does not,
 * the event stays unprocessed and the next drain runs the side effect again (back to
 * double-processing); if the mark commits but the side effect does not, the event is lost. When the
 * side effect is not transactional (e.g. a call to a remote system) effectively-once does not hold
 * — make the downstream call idempotent too, or accept the narrow double-call window.
 *
 * <p><strong>Tombstones, not deletes.</strong> {@link #markProcessed} flips a flag; it does
 * <em>not</em> delete the row. The processed row must survive as a tombstone until the matching
 * outbox row is gone. A fan-out redelivery — the outbox still holds the event because a
 * <em>sibling</em> consumer's inbox had not yet acknowledged {@link #receive} — would otherwise
 * re-insert the event here and the consumer would apply it twice. Rows are pruned later by a
 * retention reaper whose window exceeds the maximum possible outbox redelivery lag.
 *
 * <p><strong>Ordering.</strong> {@link #fetchUnprocessed} returns events oldest-first by a
 * monotonic receive-sequence the inbox assigns on {@link #receive}, not by {@link
 * IEvent#getTimestamp()} (which is subject to ties and clock skew). As long as the relay delivers
 * in outbox-staging order, per-aggregate order is preserved.
 *
 * <p>Implementations back this with durable storage — typically a single table per consumer keyed
 * by {@link IEvent#getEventId()} (the idempotency key for {@link #receive}) with a {@code
 * BIGSERIAL} receive-sequence for ordering and a {@code processed} flag. A concurrent drain claims
 * rows with {@code SELECT ... FOR UPDATE SKIP LOCKED}, or the impl guarantees single-threaded drain
 * per consumer.
 */
public interface IInbox {

    /**
     * Producer/relay side. Durably stage an incoming event for this consumer in received-but-
     * unprocessed state, assigning it a monotonic receive-sequence that preserves staging order.
     *
     * <p>Idempotent on {@link IEvent#getEventId()}: a no-op if the event was already received,
     * whether or not it has since been processed. This idempotency is what lets the relay drop the
     * outbox row the moment {@code receive} has committed to every subscriber's inbox — a later
     * fan-out redelivery simply lands as a no-op.
     *
     * @param event the event to stage for this consumer
     */
    void receive(@Nonnull IEvent event);

    /**
     * Consumer side. Returns received-but-unprocessed events, oldest-first by receive-sequence
     * (which preserves outbox staging order), up to {@code limit}. The consumer drains these on
     * each tick and on startup; events remain returned until acknowledged via {@link
     * #markProcessed}.
     *
     * @param limit the maximum number of events to return; must be non-negative
     * @return the next batch of unprocessed events, oldest first
     */
    @Nonnull
    List<IEvent> fetchUnprocessed(int limit);

    /**
     * Consumer side. Returns whether the event with the given id has been received <em>and</em>
     * processed via {@link #markProcessed}. A defensive guard against concurrent or duplicate
     * drains.
     *
     * @param eventId the {@link IEvent#getEventId() id} of the event about to be handled
     * @return {@code true} if this id was received and already marked processed, so skip it
     */
    boolean alreadyProcessed(@Nonnull UUID eventId);

    /**
     * Consumer side. Marks a received event processed, so a later {@link #alreadyProcessed} for the
     * same id returns {@code true} and {@link #fetchUnprocessed} no longer returns it.
     *
     * <p>Must commit in the same transaction as the side effect it guards. Flips the row's flag; it
     * does not delete the row — the tombstone must survive until the matching outbox row is gone
     * (see the type-level note on tombstones). A no-op for unknown or already-processed ids.
     *
     * @param eventId the {@link IEvent#getEventId() id} of the handled event
     */
    void markProcessed(@Nonnull UUID eventId);
}
