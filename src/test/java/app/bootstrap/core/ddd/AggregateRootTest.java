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
package app.bootstrap.core.ddd;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class AggregateRootTest {

    // Test-specific ID class
    static class TestId extends Id {
        public TestId() {
            super(UUID.randomUUID());
        }

        public TestId(UUID uuid) {
            super(uuid);
        }
    }

    // Test-specific domain event
    static class TestDomainEvent implements IDomainEvent {
        private final String data;
        private final Date timestamp;

        public TestDomainEvent(String data) {
            this.data = data;
            this.timestamp = new Date();
        }

        public String getData() {
            return data;
        }

        @Nonnull
        @Override
        public Date getTimestamp() {
            return timestamp;
        }
    }

    // Test-specific aggregate root
    static class TestAggregateRoot extends AggregateRoot<TestId> {
        private String name;

        public TestAggregateRoot(TestId id) {
            super(id, new ArrayList<>());
            this.name = "Default";
        }

        public TestAggregateRoot(TestId id, int version, List<IDomainEvent> events) {
            super(id, version, events);
            this.name = "Default";
        }

        public void changeName(String newName) {
            this.name = newName;
            apply(new TestDomainEvent("Name changed to " + newName));
        }

        public String getName() {
            return name;
        }
    }

    @Test
    void shouldCreateAggregateRootWithInitialVersion() {
        // Arrange
        TestId id = new TestId();

        // Act
        TestAggregateRoot aggregate = new TestAggregateRoot(id);

        // Assert
        assertEquals(0, aggregate.getVersion());
        assertEquals(id, aggregate.getId());
        assertFalse(aggregate.hasUncommitedChanges());
        assertEquals(0, aggregate.getUncommittedChanges().size());
    }

    @Test
    void shouldCreateAggregateRootWithSpecificVersion() {
        // Arrange
        TestId id = new TestId();
        int initialVersion = 5;

        // Act
        TestAggregateRoot aggregate = new TestAggregateRoot(id, initialVersion, new ArrayList<>());

        // Assert
        assertEquals(initialVersion, aggregate.getVersion());
        assertEquals(id, aggregate.getId());
    }

    @Test
    void shouldTrackUncommittedChanges() {
        // Arrange
        TestAggregateRoot aggregate = new TestAggregateRoot(new TestId());

        // Act
        aggregate.changeName("New Name");

        // Assert
        assertTrue(aggregate.hasUncommitedChanges());
        assertEquals(1, aggregate.getUncommittedChanges().size());
        TestDomainEvent event = (TestDomainEvent) aggregate.getUncommittedChanges().get(0);
        assertEquals("Name changed to New Name", event.getData());
    }

    @Test
    void shouldCalculateNextVersionCorrectly() {
        // Arrange
        TestAggregateRoot aggregate = new TestAggregateRoot(new TestId(), 5, new ArrayList<>());

        // Act
        aggregate.changeName("First Change");
        aggregate.changeName("Second Change");

        // Assert
        assertEquals(5, aggregate.getVersion());
        assertEquals(7, aggregate.getNextVersion());
    }

    @Test
    void shouldMarkChangesAsCommitted() {
        // Arrange
        TestAggregateRoot aggregate = new TestAggregateRoot(new TestId(), 5, new ArrayList<>());
        aggregate.changeName("New Name");

        // Act
        aggregate.markChangesAsCommitted();

        // Assert
        assertEquals(6, aggregate.getVersion());
        assertFalse(aggregate.hasUncommitedChanges());
        assertEquals(0, aggregate.getUncommittedChanges().size());
    }

    @Test
    void shouldCommitAndPublishEvents() {
        // Arrange
        TestAggregateRoot aggregate = new TestAggregateRoot(new TestId(), 5, new ArrayList<>());
        aggregate.changeName("New Name");
        AtomicInteger publishedEventsCount = new AtomicInteger(0);

        // Act
        aggregate.commit(events -> {
            publishedEventsCount.set(events.size());
            TestDomainEvent event = (TestDomainEvent) events.get(0);
            assertEquals("Name changed to New Name", event.getData());
        });

        // Assert
        assertEquals(6, aggregate.getVersion());
        assertFalse(aggregate.hasUncommitedChanges());
        assertEquals(0, aggregate.getUncommittedChanges().size());
        assertEquals(1, publishedEventsCount.get());
    }

    @Test
    void shouldPreserveEquality() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        TestId id1 = new TestId(uuid);
        TestId id2 = new TestId(uuid);

        // Act
        TestAggregateRoot aggregate1 = new TestAggregateRoot(id1);
        TestAggregateRoot aggregate2 = new TestAggregateRoot(id2);

        // Assert
        assertEquals(aggregate1, aggregate2);
        assertEquals(aggregate1.hashCode(), aggregate2.hashCode());
    }
}
