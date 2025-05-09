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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SimpleQueryBus implements IQueryBus {
    @Nonnull private final Map<Class<? extends IQuery>, IQueryHandler> handlers = new HashMap<>();

    @Override
    public <Q extends IQuery<R>, R> void register(
            @Nonnull IQueryHandler<Q, R> queryHandler,
            @Nonnull Class<? extends IQuery<R>> forQuery) {
        handlers.put(forQuery, queryHandler);
    }

    @Override
    public <R> void remove(@Nonnull Class<? extends IQuery<R>> forQuery) {
        handlers.remove(forQuery);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <R> CompletableFuture<R> send(@Nonnull IQuery<R> query) {
        try {
            final IQueryHandler<IQuery<R>, R> handler = handlers.get(query.getClass());
            if (handler == null) {
                throw new IllegalArgumentException(
                        "No handler registered for " + query.getClass().getName());
            }
            final CompletableFuture<R> completableFuture = new CompletableFuture<>();
            final ExecutorService executors = Executors.newCachedThreadPool();
            executors.submit(
                    () -> {
                        completableFuture.complete(handler.handle(query));
                        return null;
                    });
            executors.shutdown();
            return completableFuture;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return CompletableFuture.supplyAsync(() -> null);
        }
    }
}
