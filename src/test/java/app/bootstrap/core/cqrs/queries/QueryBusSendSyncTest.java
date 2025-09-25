/*
 * App Bootstrap Core
 * Copyright (C) 2024
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
package app.bootstrap.core.cqrs.queries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.bootstrap.core.cqrs.IQueryHandler;
import app.bootstrap.core.cqrs.SimpleQueryBus;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryBusSendSyncTest {

    private SimpleQueryBus queryBus;

    @BeforeEach
    void setUp() {
        queryBus = new SimpleQueryBus();
    }

    @Test
    void sendSync_shouldReturnResult_whenHandlerIsRegistered() throws Exception {
        // Given
        SimpleQueryHandler handler = new SimpleQueryHandler(queryBus);
        queryBus.register(handler, SimpleQuery.class);
        SimpleQuery query = new SimpleQuery();

        // When
        String result = queryBus.sendSync(query);

        // Then
        assertThat(result).isNotNull().isEqualTo("Test");
    }

    @Test
    void sendSync_shouldThrowIllegalArgumentException_whenNoHandlerIsRegistered() {
        // Given
        SimpleQuery query = new SimpleQuery();

        // When & Then
        assertThatThrownBy(() -> queryBus.sendSync(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No handler registered for " + SimpleQuery.class.getName());
    }

    @Test
    void sendSync_shouldPropagateException_whenHandlerThrowsException() {
        // Given
        IQueryHandler<SimpleQuery, String> throwingHandler = new IQueryHandler<SimpleQuery, String>() {
            @Nonnull
            @Override
            public String handle(@Nonnull SimpleQuery query) throws Exception {
                throw new RuntimeException("Handler error");
            }
        };
        queryBus.register(throwingHandler, SimpleQuery.class);
        SimpleQuery query = new SimpleQuery();

        // When & Then
        assertThatThrownBy(() -> queryBus.sendSync(query))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Handler error");
    }
}