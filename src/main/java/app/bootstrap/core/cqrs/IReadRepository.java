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
import java.util.Optional;

/**
 * The query side of a read model: fetch use-case-owned {@link IView views} by id.
 *
 * <p>This port is keyed only by the id type {@code I} and the view type {@code V}; it deliberately
 * does <em>not</em> mention the read-model persistence entity, so a use-case-layer query handler
 * can depend on it without referencing infrastructure. A single read model may expose many views,
 * and an implementation may back them with one table or several — that choice stays in
 * infrastructure.
 *
 * <p>The write side lives in {@link IProjectionStore}. An implementation typically realises both.
 *
 * @param <I> the read model id type
 */
public interface IReadRepository<I> {

    /** Read a single view (subset of fields) of the read model identified by {@code id}. */
    @Nonnull
    <V extends IView<I>> Optional<V> read(@Nonnull I id, @Nonnull Class<V> view);
}
