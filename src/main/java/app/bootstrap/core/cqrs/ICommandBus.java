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
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The Command Bus interface provides a centralized mechanism for dispatching commands to their
 * respective handlers in a CQRS (Command Query Responsibility Segregation) architecture.
 *
 * <p>This interface supports both synchronous and asynchronous command execution, allowing for
 * flexible command processing patterns. It manages the registration and unregistration of command
 * handlers and provides methods for sending commands to be processed by the appropriate handlers.
 *
 * <p>The command bus supports multiple handlers for a single command type, enabling cross-cutting
 * concerns like logging, validation, or notification to be handled by separate handlers.
 *
 * @see ICommand
 * @see ICommandHandler
 * @see ITrackableCommand
 * @since 1.0
 */
public interface ICommandBus {
    /**
     * Registers a command handler for a specific command type.
     *
     * <p>Multiple handlers can be registered for the same command type, and all registered handlers
     * will be executed when the command is sent.
     *
     * @param commandHandler the handler to register for processing commands
     * @param forCommand the command type that this handler should process
     */
    void register(
            @Nonnull ICommandHandler commandHandler, @Nonnull Class<? extends ICommand> forCommand);

    /**
     * Registers a command handler for multiple command types.
     *
     * <p>This is a convenience method that allows registering a single handler for multiple command
     * types in one call. The handler will be registered for each command type in the provided list.
     *
     * @param commandHandler the handler to register for processing commands
     * @param forCommands the list of command types that this handler should process
     */
    void register(
            @Nonnull ICommandHandler commandHandler,
            @Nonnull List<Class<? extends ICommand>> forCommands);

    /**
     * Unregisters a command handler from a specific command type.
     *
     * <p>After unregistration, the handler will no longer be invoked when commands of the specified
     * type are sent through the bus.
     *
     * @param commandHandler the handler to unregister
     * @param forCommand the command type to unregister the handler from
     */
    void unregister(
            @Nonnull ICommandHandler commandHandler, @Nonnull Class<? extends ICommand> forCommand);

    /**
     * Unregisters a command handler from multiple command types.
     *
     * <p>This is a convenience method that allows unregistering a single handler from multiple
     * command types in one call.
     *
     * @param commandHandler the handler to unregister
     * @param forCommands the list of command types to unregister the handler from
     */
    void unregister(
            @Nonnull ICommandHandler commandHandler,
            @Nonnull List<Class<? extends ICommand>> forCommands);

    /**
     * Registers a single result-command handler for a specific result command type.
     *
     * <p>Unlike fire-and-forget commands, result commands are intended to have exactly one handler
     * per command type. Registering another handler for the same command type should replace the
     * previous one (implementation-dependent) or be disallowed.
     *
     * @param <C> the concrete result command type
     * @param <R> the result type returned by the command
     * @param commandHandler the handler to register
     * @param forCommand the command type this handler processes
     */
    <C extends IResultCommand<R>, R> void register(
            @Nonnull IResultCommandHandler<C, R> commandHandler,
            @Nonnull Class<? extends IResultCommand<R>> forCommand);

    /**
     * Unregisters the result-command handler for a specific result command type.
     *
     * @param <C> the concrete result command type
     * @param <R> the result type returned by the command
     * @param commandHandler the handler to unregister
     * @param forCommand the command type to unregister the handler from
     */
    <C extends IResultCommand<R>, R> void unregister(
            @Nonnull IResultCommandHandler<C, R> commandHandler,
            @Nonnull Class<? extends IResultCommand<R>> forCommand);

    /**
     * Asynchronously sends a command to be processed by registered handlers.
     *
     * <p>This method returns immediately, and the command processing is performed asynchronously
     * using an executor service. All registered handlers for the command type will be executed.
     *
     * <p>If implement and the command implements {@link ITrackableCommand}, its status will be
     * tracked and can be queried through the command status repository.
     *
     * @param command the command to send for processing
     * @return a CompletableFuture that completes with true if all handlers executed successfully,
     *     false if no handlers were registered for the command type
     * @throws Exception if an error occurs during command processing setup
     */
    @Nonnull
    CompletableFuture<Boolean> send(@Nonnull ICommand command) throws Exception;

    /**
     * Synchronously sends a command and waits for its execution to complete. This method executes
     * the command handlers in the current thread without using the executor service.
     *
     * @param command the command to send
     * @return true if all handlers executed successfully
     * @throws Exception if any handler throws an exception during execution
     */
    Boolean sendSync(@Nonnull ICommand command) throws Exception;

    /**
     * Asynchronously sends a result command to be processed by its registered handler.
     *
     * @param <R> the result type of the command
     * @param command the command to send
     * @return a CompletableFuture that completes with the command result
     * @throws Exception if an error occurs during command processing setup
     */
    @Nonnull
    <R> CompletableFuture<R> send(@Nonnull IResultCommand<R> command) throws Exception;

    /**
     * Synchronously sends a result command and returns its result.
     *
     * @param <R> the result type of the command
     * @param command the command to send
     * @return the command result
     * @throws Exception if the handler throws during execution or no handler is registered
     */
    @Nonnull
    <R> R sendSync(@Nonnull IResultCommand<R> command) throws Exception;
}
