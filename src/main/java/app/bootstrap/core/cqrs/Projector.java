/*
 * App Bootstrap Core
 * Copyright (C) 2024
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
package app.bootstrap.core.cqrs;

import app.bootstrap.core.ddd.IDomainEventBus;
import app.bootstrap.core.messaging.IEvent;
import jakarta.annotation.Nonnull;

/**
 * Base for a projector: an {@link IProjector event listener} that keeps a read model in sync by
 * upserting {@link IProjection} slices.
 *
 * <p>Depends only on the {@link IProjectionStore write side} (keyed by {@code I}), never on the
 * read-model persistence entity, so a projector can live in the use-case layer. Many projectors may
 * maintain the same read model, each owning a disjoint set of its fields; a projector that also
 * needs to query existing state can inject an {@link IReadRepository} separately.
 *
 * @param <I> the read model id type
 * @param <P> the projection type this projector upserts
 * @param <E> the event type this projector consumes (e.g. {@code IEvent} for any event, or a
 *     narrower domain/integration event type)
 */
public abstract class Projector<I, P extends IProjection<I>, E extends IEvent>
        implements IProjector<E> {
    @Nonnull protected final IDomainEventBus domainEventBus;
    @Nonnull protected final IProjectionStore<I, P> store;

    protected Projector(
            @Nonnull IDomainEventBus domainEventBus, @Nonnull IProjectionStore<I, P> store) {
        this.store = store;
        this.domainEventBus = domainEventBus;
    }
}
