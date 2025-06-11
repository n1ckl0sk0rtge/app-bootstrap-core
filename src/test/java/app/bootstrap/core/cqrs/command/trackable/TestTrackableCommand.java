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
package app.bootstrap.core.cqrs.command.trackable;

import static org.assertj.core.api.Assertions.assertThat;

import app.bootstrap.core.cqrs.CommandStatus;
import app.bootstrap.core.cqrs.SimpleICommandBus;
import org.junit.jupiter.api.Test;

class TestTrackableCommand {

    @Test
    void testTrackable() throws Exception {
        final SimpleICommandBus simpleCommandBus = new SimpleICommandBus();
        final SimpleTrackableCommandHandler simpleTrackableCommandHandler =
                new SimpleTrackableCommandHandler(simpleCommandBus);
        simpleCommandBus.register(simpleTrackableCommandHandler, SimpleTrackableCommand.class);

        final SimpleTrackableCommand trackableCommand = new SimpleTrackableCommand("message");
        boolean success = simpleCommandBus.send(trackableCommand).get();

        assertThat(success).isTrue();
        assertThat(simpleCommandBus.getStatus(trackableCommand.getId()))
                .isEqualTo(CommandStatus.COMPLETED);
    }
}
