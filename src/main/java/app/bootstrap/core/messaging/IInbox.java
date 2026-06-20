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
import java.util.UUID;

/**
 * Consumer-side deduplication store — the idempotent-receiver half of reliable messaging.
 *
 * <p>{@link IOutbox} guarantees <em>at-least-once</em> delivery: a relay may crash after
 * dispatching an event but before acknowledging it, so the same event can be delivered more than
 * once. That pushes a duty onto every consumer — handling an event twice must have the same effect
 * as handling it once. The inbox is the contract that lets a consumer discharge that duty by
 * remembering which events it has already processed and skipping the repeats.
 *
 * <p>The store is keyed by {@link IEvent#getEventId()}, the stable identity an event keeps across
 * redeliveries. A handler consults the inbox before doing its work:
 *
 * <pre>{@code
 * // consumer — runs inside the same transaction as the side effect it guards
 * if (inbox.alreadyProcessed(event.getEventId())) {
 *     return; // a redelivery of an event we have already applied — skip
 * }
 * applyTheSideEffect(event);             // update a read model, call a collaborator, ...
 * inbox.markProcessed(event.getEventId());
 * }</pre>
 *
 * <p><strong>Atomicity.</strong> {@link #markProcessed} must commit in the <em>same
 * transaction</em> as the side effect it guards. If the side effect commits but the mark does not,
 * the next redelivery runs the side effect again (back to double-processing); if the mark commits
 * but the side effect does not, the event is lost. When the side effect is not transactional (e.g.
 * a call to a remote system) the at-least-once guarantee still holds, but exactly-once does not —
 * design the downstream call to be idempotent too, or accept the narrow double-call window.
 *
 * <p>This is the mirror image of {@link IOutbox}: the outbox makes the <em>producer</em> reliable
 * by staging events in the write transaction; the inbox makes the <em>consumer</em> reliable by
 * recording what it has already applied. A complete pipeline uses both.
 *
 * <p>Implementations back this with durable storage — typically a single table of processed event
 * ids with the id as the primary key, so {@link #markProcessed} is an insert and the uniqueness
 * constraint is the dedupe. Old ids can be pruned once redelivery of an event that old is no longer
 * possible.
 */
public interface IInbox {

    /**
     * Returns whether the event with the given id has already been processed and acknowledged via
     * {@link #markProcessed}.
     *
     * @param eventId the {@link IEvent#getEventId() id} of the event about to be handled
     * @return {@code true} if this id was already marked processed, so the handler should skip it
     */
    boolean alreadyProcessed(@Nonnull UUID eventId);

    /**
     * Records that the event with the given id has been processed, so a later {@link
     * #alreadyProcessed} for the same id returns {@code true}.
     *
     * <p>Must commit in the same transaction as the side effect it guards. Marking an id that is
     * already recorded is a no-op.
     *
     * @param eventId the {@link IEvent#getEventId() id} of the handled event
     */
    void markProcessed(@Nonnull UUID eventId);
}
