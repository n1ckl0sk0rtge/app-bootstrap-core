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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class RemoveHandlerForMultipleCommandsTest {

    record TestCommand1() implements ICommand {
    }

    record TestCommand2() implements ICommand {
    }

    record TestCommand3() implements ICommand {
    }

    static class MultiCommandHandler implements ICommandHandler {
        private final AtomicInteger counter;

        MultiCommandHandler(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public void handle(@Nonnull ICommand command) throws Exception {
            if (command instanceof TestCommand1
                    || command instanceof TestCommand2
                    || command instanceof TestCommand3) {
                counter.incrementAndGet();
                System.out.println(
                        "MultiCommandHandler executed for " + command.getClass().getSimpleName());
            }
        }
    }

    @Test
    void removeHandlerForMultipleCommands() throws Exception {
        // Arrange
        final SimpleICommandBus simpleCommandBus = new SimpleICommandBus();
        final AtomicInteger counter = new AtomicInteger(0);
        final MultiCommandHandler handler = new MultiCommandHandler(counter);

        // Register handler for all three command types
        List<Class<? extends ICommand>> commandClasses =
                Arrays.asList(TestCommand1.class, TestCommand2.class, TestCommand3.class);
        simpleCommandBus.register(handler, commandClasses);

        // Verify handler is registered for all commands
        boolean result1 = simpleCommandBus.send(new TestCommand1()).get();
        boolean result2 = simpleCommandBus.send(new TestCommand2()).get();
        boolean result3 = simpleCommandBus.send(new TestCommand3()).get();
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
        assertThat(result3).isTrue();
        assertThat(counter.get()).isEqualTo(3);

        // Act - Remove handler for TestCommand1 and TestCommand2 only
        List<Class<? extends ICommand>> commandsToRemove =
                Arrays.asList(TestCommand1.class, TestCommand2.class);
        simpleCommandBus.unregister(handler, commandsToRemove);

        // Reset counter
        counter.set(0);

        // Assert - Handler should only be executed for TestCommand3
        result1 = simpleCommandBus.send(new TestCommand1()).get();
        result2 = simpleCommandBus.send(new TestCommand2()).get();
        result3 = simpleCommandBus.send(new TestCommand3()).get();

        assertThat(result1).isFalse();
        assertThat(result2).isFalse();
        assertThat(result3).isTrue();
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    void removeHandlerForAllCommands() throws Exception {
        // Arrange
        final SimpleICommandBus simpleCommandBus = new SimpleICommandBus();
        final AtomicInteger counter = new AtomicInteger(0);
        final MultiCommandHandler handler = new MultiCommandHandler(counter);

        // Register handler for all three command types
        List<Class<? extends ICommand>> commandClasses =
                Arrays.asList(TestCommand1.class, TestCommand2.class, TestCommand3.class);
        simpleCommandBus.register(handler, commandClasses);

        // Act - Remove handler for all commands
        simpleCommandBus.unregister(handler, commandClasses);

        // Assert - No handlers should be executed
        boolean result1 = simpleCommandBus.send(new TestCommand1()).get();
        boolean result2 = simpleCommandBus.send(new TestCommand2()).get();
        boolean result3 = simpleCommandBus.send(new TestCommand3()).get();

        assertThat(result1).isFalse();
        assertThat(result2).isFalse();
        assertThat(result3).isFalse();
        assertThat(counter.get()).isZero();
    }

    @Test
    void removeNonExistentHandlerForMultipleCommands() throws Exception {
        // Arrange
        final SimpleICommandBus simpleCommandBus = new SimpleICommandBus();
        final AtomicInteger counter = new AtomicInteger(0);
        final MultiCommandHandler handler = new MultiCommandHandler(counter);
        final MultiCommandHandler nonRegisteredHandler = new MultiCommandHandler(counter);

        // Register handler for all three command types
        List<Class<? extends ICommand>> commandClasses =
                Arrays.asList(TestCommand1.class, TestCommand2.class, TestCommand3.class);
        simpleCommandBus.register(handler, commandClasses);

        // Act - Try to remove a handler that wasn't registered
        simpleCommandBus.unregister(nonRegisteredHandler, commandClasses);

        // Assert - Original handler should still be executed for all commands
        boolean result1 = simpleCommandBus.send(new TestCommand1()).get();
        boolean result2 = simpleCommandBus.send(new TestCommand2()).get();
        boolean result3 = simpleCommandBus.send(new TestCommand3()).get();

        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
        assertThat(result3).isTrue();
        assertThat(counter.get()).isEqualTo(3);
    }
}
