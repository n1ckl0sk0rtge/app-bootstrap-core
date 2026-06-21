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
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Reference {@link ICommandBus} decorator that runs every <em>synchronous</em> dispatch inside an
 * {@link IUnitOfWork}, so each command's writes commit atomically without each handler having to
 * remember to open a transaction. Registration calls and the asynchronous {@code send} variants are
 * forwarded unchanged — an async command runs on a worker thread, so its unit of work must be
 * opened there (by the underlying bus or the handler), not on the enqueuing thread this decorator
 * sees.
 *
 * <p>Rollback depends on the wrapped bus <em>propagating</em> handler failures: the typed {@code
 * sendSync(IResultCommand)} path does, so a throwing handler rolls the unit of work back. A bus
 * that swallows fire-and-forget handler exceptions (as the reference {@link SimpleICommandBus}
 * does) returns normally, and the unit of work commits — make handlers whose atomicity matters
 * throw.
 */
public final class TransactionalCommandBus implements ICommandBus {

    @Nonnull private final ICommandBus delegate;
    @Nonnull private final IUnitOfWork unitOfWork;

    public TransactionalCommandBus(@Nonnull ICommandBus delegate, @Nonnull IUnitOfWork unitOfWork) {
        this.delegate = delegate;
        this.unitOfWork = unitOfWork;
    }

    // ---- Synchronous dispatch — wrapped in a unit of work ----------------------------------

    @Nonnull
    @Override
    public Boolean sendSync(@Nonnull ICommand command) throws Exception {
        return unitOfWork.execute(() -> delegate.sendSync(command));
    }

    @Nonnull
    @Override
    public <R> R sendSync(@Nonnull IResultCommand<R> command) throws Exception {
        return unitOfWork.execute(() -> delegate.sendSync(command));
    }

    // ---- Asynchronous dispatch — forwarded (the UoW belongs on the worker thread) ----------

    @Nonnull
    @Override
    public CompletableFuture<Boolean> send(@Nonnull ICommand command) throws Exception {
        return delegate.send(command);
    }

    @Nonnull
    @Override
    public <R> CompletableFuture<R> send(@Nonnull IResultCommand<R> command) throws Exception {
        return delegate.send(command);
    }

    // ---- Registration — forwarded unchanged ------------------------------------------------

    @Override
    public void register(
            @Nonnull ICommandHandler commandHandler,
            @Nonnull Class<? extends ICommand> forCommand) {
        delegate.register(commandHandler, forCommand);
    }

    @Override
    public void register(
            @Nonnull ICommandHandler commandHandler,
            @Nonnull List<Class<? extends ICommand>> forCommands) {
        delegate.register(commandHandler, forCommands);
    }

    @Override
    public <C extends IResultCommand<R>, R> void register(
            @Nonnull IResultCommandHandler<C, R> commandHandler,
            @Nonnull Class<? extends IResultCommand<R>> forCommand) {
        delegate.register(commandHandler, forCommand);
    }

    @Override
    public void unregister(
            @Nonnull ICommandHandler commandHandler,
            @Nonnull Class<? extends ICommand> forCommand) {
        delegate.unregister(commandHandler, forCommand);
    }

    @Override
    public void unregister(
            @Nonnull ICommandHandler commandHandler,
            @Nonnull List<Class<? extends ICommand>> forCommands) {
        delegate.unregister(commandHandler, forCommands);
    }

    @Override
    public <C extends IResultCommand<R>, R> void unregister(
            @Nonnull IResultCommandHandler<C, R> commandHandler,
            @Nonnull Class<? extends IResultCommand<R>> forCommand) {
        delegate.unregister(commandHandler, forCommand);
    }
}
