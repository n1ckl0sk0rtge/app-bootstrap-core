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

class IdTest {

    // Test-specific ID class
    static class TestId extends Id {
        public TestId() {
            super(UUID.randomUUID());
        }

        public TestId(UUID uuid) {
            super(uuid);
        }
    }

    // Another test-specific ID class to test equality between different ID types
    static class AnotherTestId extends Id {
        public AnotherTestId(UUID uuid) {
            super(uuid);
        }
    }

    @Test
    void shouldCreateIdWithRandomUuid() {
        // Act
        TestId id = new TestId();
        
        // Assert
        assertNotNull(id.getUuid());
    }

    @Test
    void shouldCreateIdWithSpecificUuid() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        
        // Act
        TestId id = new TestId(uuid);
        
        // Assert
        assertEquals(uuid, id.getUuid());
    }

    @Test
    void shouldConvertToString() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        TestId id = new TestId(uuid);
        
        // Act
        String result = id.toString();
        
        // Assert
        assertEquals(uuid.toString(), result);
    }

    @Test
    void shouldBeEqualWhenUuidsAreEqual() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        TestId id1 = new TestId(uuid);
        TestId id2 = new TestId(uuid);
        
        // Assert
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenUuidsAreDifferent() {
        // Arrange
        TestId id1 = new TestId();
        TestId id2 = new TestId();
        
        // Assert
        assertNotEquals(id1, id2);
        assertNotEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void shouldBeEqualEvenWithDifferentIdTypes() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        TestId id1 = new TestId(uuid);
        AnotherTestId id2 = new AnotherTestId(uuid);
        
        // Assert
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void shouldNotBeEqualToNull() {
        // Arrange
        TestId id = new TestId();
        
        // Assert
        assertNotEquals(null, id);
    }

    @Test
    void shouldNotBeEqualToDifferentType() {
        // Arrange
        TestId id = new TestId();
        Object otherObject = new Object();
        
        // Assert
        assertNotEquals(id, otherObject);
    }

    @Test
    void shouldBeEqualToSelf() {
        // Arrange
        TestId id = new TestId();
        
        // Assert
        assertEquals(id, id);
    }
}