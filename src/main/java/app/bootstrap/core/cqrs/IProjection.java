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
 * A write slice that a projector applies to a read model, keyed by {@code I}.
 *
 * <p>Carries the id plus only the fields that should change, so a projector never has to
 * materialize the full record to update a few values. {@link IProjectionStore#upsert} merges the
 * carried fields into the existing row (creating it when absent).
 *
 * <p>A projection is a use-case-owned DTO, never the persistence entity. One logical read model can
 * be maintained by <em>many</em> projectors, each upserting its own projection over a disjoint set
 * of fields; because each carries only its own fields, concurrent writers do not clobber each
 * other.
 *
 * @param <I> the read model id type
 */
public interface IProjection<I> {

    @Nonnull
    I getId();
}
