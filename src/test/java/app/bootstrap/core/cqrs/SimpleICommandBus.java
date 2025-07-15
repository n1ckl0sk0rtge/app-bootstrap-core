/*
 * App Bootstrap Core
 * Copyright (C) 2025
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SimpleICommandBus implements ICommandBus, ICommandStatusReadRepository {

    @SuppressWarnings("all")
    @Nonnull
    private final Map<Class<? extends ICommand>, List<ICommandHandler>> handlers;

    private final ExecutorService executorService;

    private final Map<UUID, CommandStatus> trackableCommandMap;

    public SimpleICommandBus() {
        this.handlers = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.trackableCommandMap = new ConcurrentHashMap<>();
    }

    @Override
    public void updateStatus(@Nonnull UUID commandId, @Nonnull CommandStatus status) {
        this.trackableCommandMap.put(commandId, status);
        System.out.println("updated command with id " + commandId + " to new status " + status);
    }

    @Nonnull
    @Override
    public CommandStatus getStatus(@Nonnull UUID commandId) {
        return this.trackableCommandMap.getOrDefault(commandId, CommandStatus.UNKNOWN);
    }

    @Override
    public void register(
            @Nonnull ICommandHandler commandHandler,
            @Nonnull Class<? extends ICommand> forCommand) {
        this.handlers
                .computeIfAbsent(forCommand, k -> new java.util.ArrayList<>())
                .add(commandHandler);
    }

    @Override
    public void register(
            @Nonnull ICommandHandler commandHandler,
            @Nonnull List<Class<? extends ICommand>> forCommands) {
        for (Class<? extends ICommand> forCommand : forCommands) {
            this.handlers
                    .computeIfAbsent(forCommand, k -> new java.util.ArrayList<>())
                    .add(commandHandler);
        }
    }

    @Override
    public void unregister(
            @Nonnull ICommandHandler commandHandler,
            @Nonnull Class<? extends ICommand> forCommand) {
        List<ICommandHandler> handlersForCommand = this.handlers.get(forCommand);
        if (handlersForCommand != null) {
            handlersForCommand.remove(commandHandler);
            if (handlersForCommand.isEmpty()) {
                this.handlers.remove(forCommand);
            }
        }
    }

    @Override
    public void unregister(
            @Nonnull ICommandHandler commandHandler,
            @Nonnull List<Class<? extends ICommand>> forCommands) {
        for (Class<? extends ICommand> forCommand : forCommands) {
            unregister(commandHandler, forCommand);
        }
    }

    @Nonnull
    @Override
    public CompletableFuture<Boolean> send(@Nonnull ICommand command) throws Exception {
        final List<ICommandHandler> handlersForCommand = handlers.get(command.getClass());
        if (handlersForCommand == null || handlersForCommand.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        if (command instanceof ITrackableCommand trackableCommand) {
            this.trackableCommandMap.put(trackableCommand.getId(), CommandStatus.PENDING);
        }

        final CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        this.executorService.submit(
                () -> completableFuture.complete(executeCommand(handlersForCommand, command)));
        return completableFuture;
    }

    @Nonnull
    public Boolean sendSync(@Nonnull ICommand command) throws Exception {
        final List<ICommandHandler> handlersForCommand = handlers.get(command.getClass());
        if (handlersForCommand == null || handlersForCommand.isEmpty()) {
            return false;
        }

        return executeCommand(handlersForCommand, command);
    }

    @Nonnull
    private Boolean executeCommand(
            @Nonnull List<ICommandHandler> handlers, @Nonnull ICommand command) {
        boolean allSucceeded = true;
        for (ICommandHandler handler : handlers) {
            try {
                handler.handle(command);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                allSucceeded = false;
            }
        }
        return allSucceeded;
    }
}
