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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestCommandTrackingRepository {

    private InMemoryCommandTrackingRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCommandTrackingRepository();
    }

    @Test
    void fetch_returnsEmptyList_whenNothingTracked() {
        assertThat(repository.fetch()).isEmpty();
    }

    @Test
    void update_storesTrackedCommand_withGivenStatus() {
        // Given
        SimpleTrackedCommand command = new SimpleTrackedCommand("hello");

        // When
        repository.update(command, CommandStatus.PENDING);

        // Then
        List<ITrackedCommand> tracked = repository.fetch();
        assertThat(tracked).hasSize(1);
        ITrackedCommand entry = tracked.get(0);
        assertThat(entry.id()).isEqualTo(command.id());
        assertThat(entry.type()).isEqualTo(SimpleTrackedCommand.class);
        assertThat(entry.metadata()).isEqualTo(command.metadata());
        assertThat(entry.status()).isEqualTo(CommandStatus.PENDING);
        assertThat(entry.isTerminal()).isFalse();
    }

    @Test
    void update_overridesStatus_whenCalledTwiceForSameCommand() {
        // Given
        SimpleTrackedCommand command = new SimpleTrackedCommand("hello");
        repository.update(command, CommandStatus.PENDING);

        // When
        repository.update(command, CommandStatus.COMPLETED);

        // Then
        List<ITrackedCommand> tracked = repository.fetch();
        assertThat(tracked).hasSize(1);
        assertThat(tracked.get(0).status()).isEqualTo(CommandStatus.COMPLETED);
        assertThat(tracked.get(0).isTerminal()).isTrue();
    }

    @Test
    void fetch_returnsAllTrackedCommands_inAnyOrder() {
        // Given
        SimpleTrackedCommand first = new SimpleTrackedCommand("first");
        SimpleTrackedCommand second = new SimpleTrackedCommand("second");
        repository.update(first, CommandStatus.PROCESSING);
        repository.update(second, CommandStatus.FAILED);

        // When
        List<ITrackedCommand> tracked = repository.fetch();

        // Then
        assertThat(tracked).hasSize(2);
        assertThat(tracked)
                .extracting(ITrackedCommand::id)
                .containsExactlyInAnyOrder(first.id(), second.id());
        assertThat(tracked)
                .extracting(ITrackedCommand::status)
                .containsExactlyInAnyOrder(CommandStatus.PROCESSING, CommandStatus.FAILED);
    }
}
