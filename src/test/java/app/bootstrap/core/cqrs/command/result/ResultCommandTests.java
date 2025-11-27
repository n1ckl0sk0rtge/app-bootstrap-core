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
package app.bootstrap.core.cqrs.command.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.bootstrap.core.cqrs.IResultCommand;
import app.bootstrap.core.cqrs.IResultCommandHandler;
import app.bootstrap.core.cqrs.SimpleICommandBus;
import jakarta.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResultCommandTests {

    private SimpleICommandBus commandBus;

    @BeforeEach
    void setUp() {
        commandBus = new SimpleICommandBus();
    }

    @Test
    void sendSync_shouldReturnValue_whenHandlerRegistered() throws Exception {
        // Given
        SimpleResultCommandHandler handler = new SimpleResultCommandHandler();
        commandBus.register(handler, SimpleResultCommand.class);
        SimpleResultCommand cmd = new SimpleResultCommand("ok");

        // When
        String result = commandBus.sendSync(cmd);

        // Then
        assertThat(result).isEqualTo("ok handled");
    }

    @Test
    void send_shouldReturnFutureWithValue_whenHandlerRegistered() throws Exception {
        // Given
        SimpleResultCommandHandler handler = new SimpleResultCommandHandler();
        commandBus.register(handler, SimpleResultCommand.class);
        SimpleResultCommand cmd = new SimpleResultCommand("async");

        // When
        CompletableFuture<String> future = commandBus.send(cmd);

        // Then
        assertThat(future).isNotNull();
        assertThat(future.get()).isEqualTo("async handled");
    }

    @Test
    void sendSync_shouldThrow_whenNoHandlerRegistered() {
        // Given
        SimpleResultCommand cmd = new SimpleResultCommand("x");

        // When & Then
        assertThatThrownBy(() -> commandBus.sendSync(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No handler registered for " + SimpleResultCommand.class.getName());
    }

    @Test
    void send_shouldCompleteExceptionally_whenNoHandlerRegistered() throws Exception {
        // Given
        SimpleResultCommand cmd = new SimpleResultCommand("x");

        // When
        CompletableFuture<String> future = commandBus.send(cmd);

        // Then
        assertThatThrownBy(future::get)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "No handler registered for " + SimpleResultCommand.class.getName());
    }

    @Test
    void sendSync_shouldPropagateException_whenHandlerThrows() {
        // Given
        IResultCommand<String> badCmd = new IResultCommand<>() {};
        @SuppressWarnings("unchecked")
        Class<? extends IResultCommand<String>> badCmdClass =
                (Class<? extends IResultCommand<String>>) badCmd.getClass();
        IResultCommandHandler<IResultCommand<String>, String> throwingHandler =
                new IResultCommandHandler<>() {
                    @Nonnull
                    @Override
                    public String handle(@Nonnull IResultCommand<String> command) throws Exception {
                        throw new RuntimeException("boom");
                    }
                };
        commandBus.register(throwingHandler, badCmdClass);

        // When & Then
        assertThatThrownBy(() -> commandBus.sendSync(badCmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");
    }
}
