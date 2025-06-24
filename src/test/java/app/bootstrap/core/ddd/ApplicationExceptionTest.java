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

import org.junit.jupiter.api.Test;

class ApplicationExceptionTest {

    // Test-specific ApplicationException implementation
    static class TestApplicationException extends ApplicationException {
        public TestApplicationException(String message, String errorCode) {
            super(message, errorCode);
        }

        public TestApplicationException(String message, String errorCode, Object context) {
            super(message, errorCode, context);
        }
    }

    @Test
    void shouldCreateExceptionWithMessageAndErrorCode() {
        // Arrange
        String message = "Test error message";
        String errorCode = "TEST_ERROR_CODE";

        // Act
        TestApplicationException exception = new TestApplicationException(message, errorCode);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getContext());
    }

    @Test
    void shouldCreateExceptionWithMessageErrorCodeAndContext() {
        // Arrange
        String message = "Test error message";
        String errorCode = "TEST_ERROR_CODE";
        Object context = new Object();

        // Act
        TestApplicationException exception = new TestApplicationException(message, errorCode, context);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(context, exception.getContext());
    }

    @Test
    void shouldReturnCorrectErrorCode() {
        // Arrange
        String errorCode = "SPECIFIC_ERROR_CODE";
        TestApplicationException exception = new TestApplicationException("Error message", errorCode);

        // Act
        String retrievedErrorCode = exception.getErrorCode();

        // Assert
        assertEquals(errorCode, retrievedErrorCode);
    }

    @Test
    void shouldReturnNullContextWhenNotProvided() {
        // Arrange
        TestApplicationException exception = new TestApplicationException("Error message", "ERROR_CODE");

        // Act
        Object context = exception.getContext();

        // Assert
        assertNull(context);
    }

    @Test
    void shouldReturnCorrectContextWhenProvided() {
        // Arrange
        String contextValue = "Context information";
        TestApplicationException exception = new TestApplicationException(
                "Error message", "ERROR_CODE", contextValue);

        // Act
        Object retrievedContext = exception.getContext();

        // Assert
        assertEquals(contextValue, retrievedContext);
    }
}