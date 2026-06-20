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

/**
 * Marker for an <em>integration event</em>: an event published as part of a bounded context's
 * <strong>public, cross-boundary contract</strong>, intended for other contexts or external
 * systems.
 *
 * <p>This is the deliberate counterpart to {@link app.bootstrap.core.ddd.IDomainEvent}. A domain
 * event is an <em>internal</em> record of something that happened inside one aggregate; its shape
 * is coupled to the domain model and is free to change as that model evolves. Leaking domain events
 * across a context boundary couples every consumer to your internal model. An integration event is
 * the opposite by intent: a stable, intentionally-shaped message you are willing to keep
 * backward-compatible for outside subscribers.
 *
 * <p>The usual flow is to <em>translate</em>, not forward: a listener reacts to one or more
 * internal domain events and emits a separate integration event carrying only the fields the
 * outside world needs. Both kinds are {@link IEvent}s, so both ride the same {@link IOutbox} and
 * {@link IEventBus} — the marker exists so the boundary is explicit in the type system (and
 * checkable by arch/lint rules), not because the transport differs.
 *
 * <pre>{@code
 * public record OrderPlacedIntegrationEvent(UUID getEventId, Instant getTimestamp,
 *                                           String orderId, String customerId, long totalCents)
 *         implements IIntegrationEvent {}
 * }</pre>
 */
public interface IIntegrationEvent extends IEvent {}
