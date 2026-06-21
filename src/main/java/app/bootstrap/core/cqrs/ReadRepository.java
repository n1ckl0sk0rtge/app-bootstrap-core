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
import jakarta.annotation.Nonnull;

/**
 * Convenience base for an adapter that realises both sides of a read model's persistence — the
 * {@link IReadRepository query side} and the {@link IProjectionStore write side}.
 *
 * <p>Lives in (or is wired from) infrastructure: this is where the persistence entity is mapped to
 * and from the use-case-owned {@link IView} / {@link IProjection} DTOs, so that entity never
 * crosses the use-case boundary.
 *
 * @param <I> the read model id type
 */
public abstract class ReadRepository<I> implements IReadRepository<I>, IProjectionStore<I> {
    @Nonnull protected final IDomainEventBus domainEventBus;

    protected ReadRepository(@Nonnull IDomainEventBus domainEventBus) {
        this.domainEventBus = domainEventBus;
    }
}
