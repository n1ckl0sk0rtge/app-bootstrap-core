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
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainEventTest {

    // Test-specific ID class
    static class TestId extends Id {
        public TestId() {
            super(UUID.randomUUID());
        }

        public TestId(UUID uuid) {
            super(uuid);
        }
    }

    // Test-specific aggregate root
    static class TestAggregateRoot extends AggregateRoot<TestId> {
        public TestAggregateRoot(TestId id) {
            super(id, new ArrayList<>());
        }
    }

    // Test-specific domain event implementation
    static class TestDomainEvent extends DomainEvent {
        private final String eventData;

        public TestDomainEvent(@Nonnull Id aggregateId,
                             @Nonnull Class<? extends AggregateRoot<?>> aggregateType,
                             String eventData) {
            super(aggregateId, aggregateType, null);
            this.eventData = eventData;
        }

        public TestDomainEvent(@Nonnull Id aggregateId,
                             @Nonnull Class<? extends AggregateRoot<?>> aggregateType,
                             Long eventVersion,
                             String eventData) {
            super(aggregateId, aggregateType, eventVersion);
            this.eventData = eventData;
        }

        public TestDomainEvent(@Nonnull UUID eventId,
                             @Nonnull Date timestamp,
                             @Nonnull Id aggregateId,
                             @Nonnull Class<? extends AggregateRoot<?>> aggregateType,
                             Long eventVersion,
                             String eventData) {
            super(eventId, timestamp, aggregateId, aggregateType, eventVersion);
            this.eventData = eventData;
        }

        public String getEventData() {
            return eventData;
        }

        // Getter methods to access protected fields for testing
        public UUID getEventId() {
            return eventId;
        }

        public Id getAggregateId() {
            return aggregateId;
        }

        public Class<? extends AggregateRoot<?>> getAggregateType() {
            return aggregateType;
        }

        public Long getEventVersion() {
            return eventVersion;
        }

        public Date getEventTimestamp() {
            return timestamp;
        }
    }

    @Test
    void shouldCreateDomainEventWithGeneratedIdAndTimestamp() {
        // Arrange
        TestId aggregateId = new TestId();
        Class<TestAggregateRoot> aggregateType = TestAggregateRoot.class;
        String eventData = "Test event data";

        // Act
        TestDomainEvent event = new TestDomainEvent(aggregateId, aggregateType, eventData);

        // Assert
        assertNotNull(event.getEventId());
        assertNotNull(event.getEventTimestamp());
        assertEquals(aggregateId, event.getAggregateId());
        assertEquals(aggregateType, event.getAggregateType());
        assertNull(event.getEventVersion());
        assertEquals(eventData, event.getEventData());
        assertNotNull(event.getTimestamp());
        assertEquals(event.getEventTimestamp(), event.getTimestamp());
    }

    @Test
    void shouldCreateDomainEventWithEventVersion() {
        // Arrange
        TestId aggregateId = new TestId();
        Class<TestAggregateRoot> aggregateType = TestAggregateRoot.class;
        Long eventVersion = 5L;
        String eventData = "Test event data with version";

        // Act
        TestDomainEvent event = new TestDomainEvent(aggregateId, aggregateType, eventVersion, eventData);

        // Assert
        assertNotNull(event.getEventId());
        assertNotNull(event.getEventTimestamp());
        assertEquals(aggregateId, event.getAggregateId());
        assertEquals(aggregateType, event.getAggregateType());
        assertEquals(eventVersion, event.getEventVersion());
        assertEquals(eventData, event.getEventData());
    }

    @Test
    void shouldCreateDomainEventWithAllParameters() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        Date timestamp = new Date(System.currentTimeMillis() - 1000);
        TestId aggregateId = new TestId();
        Class<TestAggregateRoot> aggregateType = TestAggregateRoot.class;
        Long eventVersion = 10L;
        String eventData = "Test event data with all params";

        // Act
        TestDomainEvent event = new TestDomainEvent(eventId, timestamp, aggregateId, aggregateType, eventVersion, eventData);

        // Assert
        assertEquals(eventId, event.getEventId());
        assertEquals(timestamp, event.getEventTimestamp());
        assertEquals(aggregateId, event.getAggregateId());
        assertEquals(aggregateType, event.getAggregateType());
        assertEquals(eventVersion, event.getEventVersion());
        assertEquals(eventData, event.getEventData());
        assertEquals(timestamp, event.getTimestamp());
    }

    @Test
    void shouldGenerateUniqueEventIds() {
        // Arrange
        TestId aggregateId = new TestId();
        Class<TestAggregateRoot> aggregateType = TestAggregateRoot.class;

        // Act
        TestDomainEvent event1 = new TestDomainEvent(aggregateId, aggregateType, "Event 1");
        TestDomainEvent event2 = new TestDomainEvent(aggregateId, aggregateType, "Event 2");

        // Assert
        assertNotEquals(event1.getEventId(), event2.getEventId());
    }

    @Test
    void shouldGenerateTimestampsInOrder() throws InterruptedException {
        // Arrange
        TestId aggregateId = new TestId();
        Class<TestAggregateRoot> aggregateType = TestAggregateRoot.class;

        // Act
        TestDomainEvent event1 = new TestDomainEvent(aggregateId, aggregateType, "Event 1");
        Thread.sleep(1); // Ensure different timestamps
        TestDomainEvent event2 = new TestDomainEvent(aggregateId, aggregateType, "Event 2");

        // Assert
        assertTrue(event1.getEventTimestamp().before(event2.getEventTimestamp()) || event1.getEventTimestamp().equals(event2.getEventTimestamp()));
    }

    @Test
    void shouldReturnCorrectTimestamp() {
        // Arrange
        Date specificTimestamp = new Date(1234567890L);
        TestId aggregateId = new TestId();
        Class<TestAggregateRoot> aggregateType = TestAggregateRoot.class;

        // Act
        TestDomainEvent event = new TestDomainEvent(
            UUID.randomUUID(), 
            specificTimestamp, 
            aggregateId, 
            aggregateType, 
            1L, 
            "Test data"
        );

        // Assert
        assertEquals(specificTimestamp, event.getTimestamp());
    }

    @Test
    void shouldHandleNullEventVersion() {
        // Arrange
        TestId aggregateId = new TestId();
        Class<TestAggregateRoot> aggregateType = TestAggregateRoot.class;

        // Act
        TestDomainEvent event = new TestDomainEvent(aggregateId, aggregateType, null, "Test data");

        // Assert
        assertNull(event.getEventVersion());
    }

    @Test
    void shouldPreserveAllFieldsWhenUsingFullConstructor() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        Date timestamp = new Date();
        TestId aggregateId = new TestId();
        Class<TestAggregateRoot> aggregateType = TestAggregateRoot.class;
        Long eventVersion = 42L;
        String eventData = "Preserved data";

        // Act
        TestDomainEvent event = new TestDomainEvent(eventId, timestamp, aggregateId, aggregateType, eventVersion, eventData);

        // Assert
        assertEquals(eventId, event.getEventId());
        assertEquals(timestamp, event.getEventTimestamp());
        assertEquals(aggregateId, event.getAggregateId());
        assertEquals(aggregateType, event.getAggregateType());
        assertEquals(eventVersion, event.getEventVersion());
        assertEquals(eventData, event.getEventData());
    }
}
