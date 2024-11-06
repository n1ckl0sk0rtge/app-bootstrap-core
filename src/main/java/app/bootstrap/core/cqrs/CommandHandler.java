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
import app.bootstrap.core.ddd.Entity;
import app.bootstrap.core.ddd.IRepository;
import jakarta.annotation.Nonnull;

public abstract class CommandHandler<I extends Id, E extends Entity<I>> implements ICommandHandler {
    @Nonnull protected final ICommandBus commandBus;
    @Nonnull protected final IRepository<I, E> repository;

    protected CommandHandler(
            @Nonnull ICommandBus commandBus, @Nonnull IRepository<I, E> repository) {
        this.commandBus = commandBus;
        this.repository = repository;
    }
}
