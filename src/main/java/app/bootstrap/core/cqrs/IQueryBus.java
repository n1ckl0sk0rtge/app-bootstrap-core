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
import java.util.concurrent.CompletableFuture;

/**
 * The Query Bus interface provides a centralized mechanism for dispatching queries to their
 * respective handlers in a CQRS (Command Query Responsibility Segregation) architecture.
 *
 * <p>This interface supports both synchronous and asynchronous query execution, allowing for
 * flexible query processing patterns. It manages the registration and removal of query handlers and
 * provides methods for sending queries to be processed by the appropriate handlers.
 *
 * <p>Unlike the command bus, the query bus typically has a one-to-one relationship between query
 * types and handlers, as queries are expected to return specific result types.
 *
 * @see IQuery
 * @see IQueryHandler
 * @since 1.0
 */
public interface IQueryBus {
    /**
     * Registers a query handler for a specific query type.
     *
     * <p>Each query type should typically have only one handler registered, as queries are expected
     * to return a specific result type. Registering multiple handlers for the same query type may
     * result in unpredictable behavior.
     *
     * @param <Q> the query type that extends IQuery
     * @param <R> the result type returned by the query
     * @param queryHandler the handler to register for processing queries
     * @param forQuery the query type that this handler should process
     */
    <Q extends IQuery<R>, R> void register(
            @Nonnull IQueryHandler<Q, R> queryHandler,
            @Nonnull Class<? extends IQuery<R>> forQuery);

    /**
     * Removes a query handler for a specific query type.
     *
     * <p>After removal, the handler will no longer be invoked when queries of the specified type
     * are sent through the bus.
     *
     * @param <R> the result type returned by the query
     * @param forQuery the query type to remove the handler for
     */
    <R> void remove(@Nonnull Class<? extends IQuery<R>> forQuery);

    /**
     * Asynchronously sends a query to be processed by the registered handler.
     *
     * <p>This method returns immediately, and the query processing is performed asynchronously. The
     * result is returned wrapped in a CompletableFuture for non-blocking operations.
     *
     * @param <R> the result type returned by the query
     * @param query the query to send for processing
     * @return a CompletableFuture containing the query result
     */
    @Nonnull
    <R> CompletableFuture<R> send(@Nonnull IQuery<R> query);

    /**
     * Synchronously sends a query and waits for its execution to complete.
     *
     * <p>This method executes the query handler in the current thread and blocks until the result
     * is available.
     *
     * @param <R> the result type returned by the query
     * @param query the query to send for processing
     * @return the query result
     */
    @Nonnull
    <R> R sendSync(@Nonnull IQuery<R> query) throws Exception;
}
