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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IValueObjectTest {

    // Test-specific value object implementation
    static class TestValueObject implements IValueObject {
        private final String value;

        public TestValueObject(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestValueObject that = (TestValueObject) o;
            return value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    @Test
    void shouldCreateValueObject() {
        // Arrange
        String value = "test value";

        // Act
        TestValueObject valueObject = new TestValueObject(value);

        // Assert
        assertEquals(value, valueObject.getValue());
    }

    @Test
    void shouldBeEqualWhenValuesAreEqual() {
        // Arrange
        String value = "test value";

        // Act
        TestValueObject valueObject1 = new TestValueObject(value);
        TestValueObject valueObject2 = new TestValueObject(value);

        // Assert
        assertEquals(valueObject1, valueObject2);
        assertEquals(valueObject1.hashCode(), valueObject2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenValuesAreDifferent() {
        // Arrange
        TestValueObject valueObject1 = new TestValueObject("value1");
        TestValueObject valueObject2 = new TestValueObject("value2");

        // Assert
        assertNotEquals(valueObject1, valueObject2);
        assertNotEquals(valueObject1.hashCode(), valueObject2.hashCode());
    }

    @Test
    void shouldBeInstanceOfIValueObject() {
        // Arrange
        TestValueObject valueObject = new TestValueObject("test");

        // Assert
        assertThat(valueObject).isInstanceOf(IValueObject.class);
    }
}
