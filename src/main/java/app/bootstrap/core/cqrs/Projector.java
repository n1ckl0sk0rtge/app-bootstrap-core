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

import app.bootstrap.core.Id;
import app.bootstrap.core.ddd.IReadRepository;
import jakarta.annotation.Nonnull;

public abstract class Projector<I extends Id, R extends IReadModel<I>> implements IProjector {
    @Nonnull protected final IReadRepository<I, R> repository;

    protected Projector(@Nonnull IReadRepository<I, R> repository) {
        this.repository = repository;
    }
}
