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
 * A partial WRITE slice of read model {@code R}.
 *
 * <p>Carries the id plus only the fields that should be updated, so a caller never has to
 * materialize the full {@code R} to update a few values. The repository's {@code upsert} merges the
 * carried fields into the existing read model.
 *
 * @param <I> the read model id type
 * @param <R> the read model this projection is a slice of
 */
public interface IProjection<I, R extends IReadModel<I>> {

    @Nonnull
    I getId();
}
