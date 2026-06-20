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
package app.bootstrap.core.messaging;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryInboxTest {

    private InMemoryInbox inbox;

    @BeforeEach
    void setUp() {
        inbox = new InMemoryInbox();
    }

    @Test
    void shouldReportUnknownIdAsNotProcessed() {
        assertFalse(inbox.alreadyProcessed(UUID.randomUUID()));
        assertEquals(0, inbox.size());
    }

    @Test
    void shouldReportProcessedAfterMark() {
        UUID id = UUID.randomUUID();

        inbox.markProcessed(id);

        assertTrue(inbox.alreadyProcessed(id));
        assertEquals(1, inbox.size());
    }

    @Test
    void shouldTreatDistinctIdsIndependently() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        inbox.markProcessed(first);

        assertTrue(inbox.alreadyProcessed(first));
        assertFalse(inbox.alreadyProcessed(second));
    }

    @Test
    void shouldBeIdempotentOnRepeatedMark() {
        UUID id = UUID.randomUUID();

        inbox.markProcessed(id);
        inbox.markProcessed(id);

        assertEquals(1, inbox.size());
    }

    @Test
    void shouldDedupeRedeliveredEventSoSideEffectRunsOnce() {
        // Mirrors a consumer guarding a side effect against at-least-once redelivery.
        UUID eventId = UUID.randomUUID();
        AtomicInteger sideEffectRuns = new AtomicInteger();

        for (int delivery = 0; delivery < 3; delivery++) {
            if (inbox.alreadyProcessed(eventId)) {
                continue; // redelivery of an event we have already applied — skip
            }
            sideEffectRuns.incrementAndGet();
            inbox.markProcessed(eventId);
        }

        assertEquals(1, sideEffectRuns.get());
    }
}
