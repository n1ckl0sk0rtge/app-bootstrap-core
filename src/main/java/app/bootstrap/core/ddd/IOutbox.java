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
package app.bootstrap.core.ddd;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * Transactional outbox for reliable domain-event delivery.
 *
 * <p>Publishing a domain event to anything that lives outside the aggregate's write transaction — a
 * broker, another bounded context, or an <em>asynchronously</em> dispatched in-process listener —
 * is a dual write: the state change commits to the database, and the event handoff happens against
 * a separate, non-durable channel. A crash between the two loses the event with no record that it
 * was owed, and the read side drifts out of sync permanently.
 *
 * <p>The outbox removes the dual write by turning the event handoff into a second row in the same
 * database, written in the <em>same transaction</em> as the aggregate. A separate relay then reads
 * staged events and dispatches them, retrying until each is acknowledged.
 *
 * <p>Typical wiring, driven by {@link AggregateRoot#commit(java.util.function.Consumer)}:
 *
 * <pre>{@code
 * // write side — inside the aggregate's save transaction
 * aggregate.commit(outbox::add);
 *
 * // relay — separate thread / process, at-least-once
 * List<IDomainEvent> batch = outbox.fetchUnpublished(100);
 * batch.forEach(domainEventBus::publish);
 * outbox.markPublished(batch.stream().map(IDomainEvent::getEventId).toList());
 * }</pre>
 *
 * <p><strong>Delivery semantics.</strong> A relay may crash after dispatching but before {@link
 * #markPublished}, so delivery is <em>at-least-once</em>: consumers must be idempotent (dedupe on
 * {@link IDomainEvent#getEventId()}). Events are handed back oldest-first to preserve the order in
 * which they were staged.
 *
 * <p>Implementations back this with durable storage. The {@code add} call must enlist in the same
 * transaction that persists the aggregate, otherwise the dual write is not actually removed.
 */
public interface IOutbox {

    /**
     * Stages events for later dispatch.
     *
     * <p>Must run inside the same transaction that persists the originating aggregate; on rollback
     * the staged events must roll back with it.
     *
     * @param events the domain events to stage, in the order they were produced
     */
    void add(@Nonnull List<IDomainEvent> events);

    /**
     * Returns the oldest staged events that have not yet been acknowledged via {@link
     * #markPublished}, up to {@code limit}, in staging order.
     *
     * <p>Events remain returned by subsequent calls until acknowledged, so a relay that fails to
     * dispatch a batch simply sees it again.
     *
     * @param limit the maximum number of events to return; must be non-negative
     * @return the next batch of unpublished events, oldest first
     */
    @Nonnull
    List<IDomainEvent> fetchUnpublished(int limit);

    /**
     * Acknowledges that the given events have been dispatched, so they are no longer returned by
     * {@link #fetchUnpublished}.
     *
     * <p>Acknowledging an unknown or already-acknowledged id is a no-op.
     *
     * @param eventIds the {@link IDomainEvent#getEventId() ids} of the dispatched events
     */
    void markPublished(@Nonnull List<UUID> eventIds);
}
