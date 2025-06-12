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
package app.bootstrap.core.cqrs.command.register;

import static org.assertj.core.api.Assertions.assertThat;

import app.bootstrap.core.cqrs.ICommand;
import app.bootstrap.core.cqrs.ICommandHandler;
import app.bootstrap.core.cqrs.SimpleICommandBus;
import jakarta.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RegisterMultipleHandlersForSameCommandTest {

    record TestCommand() implements ICommand {}

    static class TestCommandHandler1 implements ICommandHandler {
        private final AtomicInteger counter;

        TestCommandHandler1(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public void handle(@Nonnull ICommand command) throws Exception {
            if (command instanceof TestCommand) {
                counter.incrementAndGet();
                System.out.println("TestCommandHandler1 executed");
            }
        }
    }

    static class TestCommandHandler2 implements ICommandHandler {
        private final AtomicInteger counter;

        TestCommandHandler2(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public void handle(@Nonnull ICommand command) throws Exception {
            if (command instanceof TestCommand) {
                counter.incrementAndGet();
                System.out.println("TestCommandHandler2 executed");
            }
        }
    }

    @Test
    void registerMultipleHandlersForSameCommand() throws Exception {
        // Arrange
        final SimpleICommandBus simpleCommandBus = new SimpleICommandBus();
        final AtomicInteger counter = new AtomicInteger(0);
        final TestCommandHandler1 handler1 = new TestCommandHandler1(counter);
        final TestCommandHandler2 handler2 = new TestCommandHandler2(counter);

        // Act
        simpleCommandBus.register(handler1, TestCommand.class);
        simpleCommandBus.register(handler2, TestCommand.class);
        boolean result = simpleCommandBus.send(new TestCommand()).get();

        // Assert
        assertThat(result).isTrue();
        assertThat(counter.get()).isEqualTo(2);
    }
}
