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

import app.bootstrap.core.ddd.Entity;
import app.bootstrap.core.ddd.IRepository;
import app.bootstrap.core.ddd.Id;
import jakarta.annotation.Nonnull;

/**
 * Base for a command handler — the use case that orchestrates a single write: load the aggregate,
 * apply the change, save it, and hand its events off for delivery.
 *
 * <p><strong>The handler is the transaction boundary.</strong> The whole {@link
 * ICommandHandler#handle handle} body must run in a single transaction so that the state write and
 * the event hand-off commit together:
 *
 * <pre>{@code
 * repository.save(aggregate);
 * aggregate.commit(outbox::add);   // staged in the SAME transaction as the save
 * }</pre>
 *
 * <p>If the save and {@link app.bootstrap.core.messaging.IOutbox#add outbox.add} land in different
 * transactions, the {@link app.bootstrap.core.messaging.IOutbox dual write} is back: a crash
 * between them persists the state but loses the events. The library deliberately does <em>not</em>
 * manage transactions (it has no persistence-framework dependency) — the boundary is owned by
 * infrastructure, typically a declarative {@code @Transactional} on the handler or an explicit
 * transaction in the persistence adapter.
 */
public abstract class CommandHandler<I extends Id, E extends Entity<I>> implements ICommandHandler {
    @Nonnull protected final ICommandBus commandBus;
    @Nonnull protected final IRepository<I, E> repository;

    protected CommandHandler(
            @Nonnull ICommandBus commandBus, @Nonnull IRepository<I, E> repository) {
        this.commandBus = commandBus;
        this.repository = repository;
    }
}
