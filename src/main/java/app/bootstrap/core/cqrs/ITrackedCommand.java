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
package app.bootstrap.core.cqrs;

import jakarta.annotation.Nonnull;

/**
 * Snapshot of a tracked command's lifecycle state, as returned by {@link
 * ICommandTrackingRepository#fetch()}.
 *
 * <p>Extends {@link ITrackableCommand} with the recorded {@link CommandStatus}. Instances represent
 * what the repository <em>stored</em> about a command, not the command as sent on the bus.
 */
public interface ITrackedCommand extends ITrackableCommand {

    @Nonnull
    CommandStatus status();

    default boolean isTerminal() {
        return this.status() == CommandStatus.COMPLETED
                || this.status() == CommandStatus.FAILED
                || this.status() == CommandStatus.UNKNOWN;
    }
}
