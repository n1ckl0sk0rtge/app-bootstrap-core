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
package app.bootstrap.core.cqrs.command;

import app.bootstrap.core.cqrs.CommandStatus;
import app.bootstrap.core.cqrs.CommandStatusReadRepository;
import app.bootstrap.core.cqrs.ICommand;
import app.bootstrap.core.cqrs.ICommandBus;
import app.bootstrap.core.cqrs.ICommandHandler;
import jakarta.annotation.Nonnull;
import java.util.UUID;

public final class SimpleCommandHandler implements ICommandHandler {
    @Nonnull ICommandBus commandBus;
    final CommandStatusReadRepository commandStatusReadRepository;

    public SimpleCommandHandler(@Nonnull ICommandBus commandBus) {
        this.commandBus = commandBus;
        this.commandStatusReadRepository = ((CommandStatusReadRepository) this.commandBus);
    }

    @Override
    public void handle(@Nonnull ICommand command) throws Exception {

        if (command instanceof SimpleTrackableCommand simpleTrackableCommand) {
            logCommandStatus(simpleTrackableCommand.getId());
            commandStatusReadRepository.updateStatus(
                    simpleTrackableCommand.getId(), CommandStatus.PROCESSING);
            logCommandStatus(simpleTrackableCommand.getId());
            commandStatusReadRepository.updateStatus(
                    simpleTrackableCommand.getId(), CommandStatus.COMPLETED);
        }
    }

    private void logCommandStatus(@Nonnull UUID trackableCommandId) {
        System.out.println(commandStatusReadRepository.getStatus(trackableCommandId));
    }
}
