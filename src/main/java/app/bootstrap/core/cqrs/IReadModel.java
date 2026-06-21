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

import jakarta.annotation.Nonnull;

/**
 * Optional marker for the full, logical read model — the complete denormalized record an
 * aggregate's events are projected into.
 *
 * <p>It is intentionally <em>not</em> a type parameter on any read-side port ({@link
 * IReadRepository}, {@link IProjectionStore}, {@link Projector} are keyed by id only). Ports speak
 * in terms of use-case-owned {@link IView} / {@link IProjection} DTOs instead, so the persistence
 * entity that holds the full record never has to appear in a use-case-layer signature.
 *
 * <p>Implement this on whatever owns the full record (a persistence entity in infrastructure, or a
 * widest "everything" view) when a single named handle for the logical read model is useful. It
 * carries no behaviour beyond {@link #getId()}.
 *
 * @param <I> the read model id type
 */
public interface IReadModel<I> {

    @Nonnull
    I getId();
}
