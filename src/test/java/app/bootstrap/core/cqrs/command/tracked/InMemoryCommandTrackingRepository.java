/*
 * App Bootstrap Core
 * Copyright (C) 2026
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
package app.bootstrap.core.cqrs.command.tracked;

import app.bootstrap.core.cqrs.CommandStatus;
import app.bootstrap.core.cqrs.ICommandTrackingRepository;
import app.bootstrap.core.cqrs.ITrackableCommand;
import app.bootstrap.core.cqrs.ITrackedCommand;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryCommandTrackingRepository implements ICommandTrackingRepository {

    private final Map<UUID, ITrackedCommand> store = new ConcurrentHashMap<>();

    @Override
    public void update(@Nonnull ITrackableCommand trackableCommand, @Nonnull CommandStatus status) {
        store.put(
                trackableCommand.id(),
                new TrackedCommand(
                        trackableCommand.id(),
                        trackableCommand.type(),
                        trackableCommand.metadata(),
                        status));
    }

    @Nonnull
    @Override
    public List<ITrackedCommand> fetch() {
        return List.copyOf(store.values());
    }
}
