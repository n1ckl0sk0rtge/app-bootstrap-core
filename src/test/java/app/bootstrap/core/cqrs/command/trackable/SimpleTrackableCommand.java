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

import app.bootstrap.core.cqrs.TrackableCommand;
import jakarta.annotation.Nonnull;
import java.util.UUID;

public final class SimpleTrackableCommand extends TrackableCommand {
    private final String message;

    public SimpleTrackableCommand(@Nonnull String message) {
        super(UUID.randomUUID());
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
