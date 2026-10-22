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
package app.bootstrap.core.cqrs;

import jakarta.annotation.Nonnull;
import java.util.Objects;

/**
 * Availability of a requested projection, independent of transaction visibility and freshness.
 *
 * <p>Consumers may return this from their own repository methods; {@link IReadRepository} remains
 * unchanged. {@link Pending} means the entity exists but its requested projection is unavailable,
 * not that a returned view is stale. Freshness can be described separately with {@link
 * ProjectionState}.
 *
 * @param <V> the requested view type
 */
public sealed interface ProjectedViewRead<V extends IView<?>>
        permits ProjectedViewRead.Found, ProjectedViewRead.Missing, ProjectedViewRead.Pending {

    /** Returns an available, non-null view. */
    @Nonnull
    static <V extends IView<?>> Found<V> found(@Nonnull V view) {
        return new Found<>(view);
    }

    /** Returns the absence of the requested entity. */
    @Nonnull
    static <V extends IView<?>> Missing<V> missing() {
        return new Missing<>();
    }

    /** Returns an existing entity whose requested projection is not yet available. */
    @Nonnull
    static <V extends IView<?>> Pending<V> pending() {
        return new Pending<>();
    }

    /** An available view; this alone makes no freshness guarantee. */
    record Found<V extends IView<?>>(@Nonnull V view) implements ProjectedViewRead<V> {
        public Found {
            Objects.requireNonNull(view, "view");
        }
    }

    /** The requested entity does not exist. */
    record Missing<V extends IView<?>>() implements ProjectedViewRead<V> {}

    /** The entity exists, but its requested projection is not yet available. */
    record Pending<V extends IView<?>>() implements ProjectedViewRead<V> {}
}
