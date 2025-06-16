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

import java.util.UUID;
import org.junit.jupiter.api.Test;

class EntityTest {

    // Test-specific ID class
    static class TestId extends Id {
        public TestId() {
            super(UUID.randomUUID());
        }

        public TestId(UUID uuid) {
            super(uuid);
        }
    }

    // Test-specific entity
    static class TestEntity extends Entity<TestId> {
        private String name;

        public TestEntity(TestId id) {
            super(id);
            this.name = "Default";
        }

        public TestEntity(TestId id, String name) {
            super(id);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    void shouldCreateEntityWithId() {
        // Arrange
        TestId id = new TestId();

        // Act
        TestEntity entity = new TestEntity(id);

        // Assert
        assertEquals(id, entity.getId());
        assertEquals("Default", entity.getName());
    }

    @Test
    void shouldCreateEntityWithIdAndName() {
        // Arrange
        TestId id = new TestId();
        String name = "Test Name";

        // Act
        TestEntity entity = new TestEntity(id, name);

        // Assert
        assertEquals(id, entity.getId());
        assertEquals(name, entity.getName());
    }

    @Test
    void shouldBeEqualWhenIdsAreEqual() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        TestId id1 = new TestId(uuid);
        TestId id2 = new TestId(uuid);

        // Act
        TestEntity entity1 = new TestEntity(id1, "Name 1");
        TestEntity entity2 = new TestEntity(id2, "Name 2");

        // Assert
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenIdsAreDifferent() {
        // Arrange
        TestId id1 = new TestId();
        TestId id2 = new TestId();

        // Act
        TestEntity entity1 = new TestEntity(id1, "Same Name");
        TestEntity entity2 = new TestEntity(id2, "Same Name");

        // Assert
        assertNotEquals(entity1, entity2);
        assertNotEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void shouldNotBeEqualToDifferentType() {
        // Arrange
        TestEntity entity = new TestEntity(new TestId());
        Object otherObject = new Object();

        // Assert
        assertNotEquals(entity, otherObject);
    }
}
