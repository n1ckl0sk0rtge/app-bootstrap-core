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

public interface ICommandBus {
    void register(
            @Nonnull ICommandHandler commandHandler, @Nonnull Class<? extends ICommand> forCommand);

    void register(
            @Nonnull ICommandHandler commandHandler,
            @Nonnull List<Class<? extends ICommand>> forCommands);

    void unregister(
            @Nonnull ICommandHandler commandHandler, @Nonnull Class<? extends ICommand> forCommand);

    void unregister(
            @Nonnull ICommandHandler commandHandler,
            @Nonnull List<Class<? extends ICommand>> forCommands);

    @Nonnull
    CompletableFuture<Boolean> send(@Nonnull ICommand command) throws Exception;
}
