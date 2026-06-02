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

import static org.assertj.core.api.Assertions.assertThat;

import app.bootstrap.core.cqrs.CommandStatus;
import app.bootstrap.core.cqrs.ITrackedCommand;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestTrackedCommand {

    @Test
    void recordExposesAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        Class<? extends ITrackedCommand> type = SimpleTrackedCommand.class;
        Map<String, String> metadata = Map.of("source", "test");

        // When
        TrackedCommand tracked = new TrackedCommand(id, type, metadata, CommandStatus.PENDING);

        // Then
        assertThat(tracked.id()).isEqualTo(id);
        assertThat(tracked.type()).isEqualTo(type);
        assertThat(tracked.metadata()).isEqualTo(metadata);
        assertThat(tracked.status()).isEqualTo(CommandStatus.PENDING);
    }

    @Test
    void isTerminal_returnsTrue_whenStatusIsCompleted() {
        TrackedCommand tracked = newTrackedCommand(CommandStatus.COMPLETED);
        assertThat(tracked.isTerminal()).isTrue();
    }

    @Test
    void isTerminal_returnsTrue_whenStatusIsFailed() {
        TrackedCommand tracked = newTrackedCommand(CommandStatus.FAILED);
        assertThat(tracked.isTerminal()).isTrue();
    }

    @Test
    void isTerminal_returnsTrue_whenStatusIsUnknown() {
        TrackedCommand tracked = newTrackedCommand(CommandStatus.UNKNOWN);
        assertThat(tracked.isTerminal()).isTrue();
    }

    @Test
    void isTerminal_returnsFalse_whenStatusIsPending() {
        TrackedCommand tracked = newTrackedCommand(CommandStatus.PENDING);
        assertThat(tracked.isTerminal()).isFalse();
    }

    @Test
    void isTerminal_returnsFalse_whenStatusIsProcessing() {
        TrackedCommand tracked = newTrackedCommand(CommandStatus.PROCESSING);
        assertThat(tracked.isTerminal()).isFalse();
    }

    private static TrackedCommand newTrackedCommand(CommandStatus status) {
        return new TrackedCommand(UUID.randomUUID(), SimpleTrackedCommand.class, Map.of(), status);
    }
}
