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

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryInboxTest {

    /** Minimal {@link IEvent} carrying its own id and timestamp. */
    private record TestEvent(UUID getEventId, Instant getTimestamp) implements IEvent {
        static TestEvent withId(UUID id) {
            return new TestEvent(id, Instant.now());
        }
    }

    private InMemoryInbox inbox;

    @BeforeEach
    void setUp() {
        inbox = new InMemoryInbox();
    }

    @Test
    void shouldReportUnknownIdAsNotProcessed() {
        assertFalse(inbox.alreadyProcessed(UUID.randomUUID()));
        assertEquals(0, inbox.size());
        assertEquals(0, inbox.unprocessedCount());
    }

    @Test
    void shouldStageReceivedEventAsUnprocessed() {
        TestEvent event = TestEvent.withId(UUID.randomUUID());

        inbox.receive(event);

        assertFalse(inbox.alreadyProcessed(event.getEventId()), "received but not yet processed");
        assertEquals(List.of(event), inbox.fetchUnprocessed(10), "drains as unprocessed");
        assertEquals(1, inbox.size());
        assertEquals(1, inbox.unprocessedCount());
    }

    @Test
    void shouldBeIdempotentOnRepeatedReceive() {
        TestEvent event = TestEvent.withId(UUID.randomUUID());

        inbox.receive(event);
        inbox.receive(event);

        assertEquals(1, inbox.size(), "redelivery of the same event id is a no-op");
        assertEquals(List.of(event), inbox.fetchUnprocessed(10));
    }

    @Test
    void shouldReportProcessedAfterMark() {
        TestEvent event = TestEvent.withId(UUID.randomUUID());

        inbox.receive(event);
        inbox.markProcessed(event.getEventId());

        assertTrue(inbox.alreadyProcessed(event.getEventId()));
        assertEquals(List.of(), inbox.fetchUnprocessed(10), "processed events no longer drain");
        assertEquals(0, inbox.unprocessedCount());
    }

    @Test
    void shouldKeepProcessedRowAsTombstone() {
        TestEvent event = TestEvent.withId(UUID.randomUUID());

        inbox.receive(event);
        inbox.markProcessed(event.getEventId());

        // markProcessed flips a flag, it does not delete — the row survives so a fan-out redelivery
        // lands as a no-op rather than re-inserting an already-applied event.
        assertEquals(1, inbox.size(), "processed row kept as a tombstone");

        inbox.receive(event); // a redelivery while the outbox row still lingers
        assertEquals(1, inbox.size());
        assertTrue(inbox.alreadyProcessed(event.getEventId()), "redelivery did not reopen the row");
        assertEquals(List.of(), inbox.fetchUnprocessed(10), "redelivery did not re-stage it");
    }

    @Test
    void shouldDrainOldestFirstByReceiveSequence() {
        TestEvent first = TestEvent.withId(UUID.randomUUID());
        TestEvent second = TestEvent.withId(UUID.randomUUID());
        TestEvent third = TestEvent.withId(UUID.randomUUID());

        inbox.receive(first);
        inbox.receive(second);
        inbox.receive(third);

        assertEquals(
                List.of(first, second, third),
                inbox.fetchUnprocessed(10),
                "oldest-first by receive sequence, not by timestamp");
    }

    @Test
    void shouldHonourFetchLimit() {
        TestEvent first = TestEvent.withId(UUID.randomUUID());
        TestEvent second = TestEvent.withId(UUID.randomUUID());

        inbox.receive(first);
        inbox.receive(second);

        assertEquals(List.of(first), inbox.fetchUnprocessed(1), "limit caps the batch");
    }

    @Test
    void shouldOnlyReturnRemainingUnprocessedAfterAPartialDrain() {
        TestEvent first = TestEvent.withId(UUID.randomUUID());
        TestEvent second = TestEvent.withId(UUID.randomUUID());
        inbox.receive(first);
        inbox.receive(second);

        inbox.markProcessed(first.getEventId());

        assertEquals(
                List.of(second),
                inbox.fetchUnprocessed(10),
                "a processed event drops out, the rest still drain oldest-first");
        assertEquals(1, inbox.unprocessedCount());
        assertEquals(2, inbox.size(), "both rows still held");
    }

    @Test
    void shouldTreatMarkOfUnknownIdAsNoOp() {
        inbox.markProcessed(UUID.randomUUID());

        assertEquals(0, inbox.size(), "marking an id never received creates nothing");
    }

    @Test
    void shouldDrainRedeliveredEventOnceAcrossRestart() {
        // Mirrors a restarting projector: it drains its inbox, applies each event, and marks it
        // processed in the same transaction — a redelivered event already in the inbox is not
        // re-applied because it has left the unprocessed set.
        TestEvent event = TestEvent.withId(UUID.randomUUID());
        AtomicInteger sideEffectRuns = new AtomicInteger();

        for (int tick = 0; tick < 3; tick++) {
            inbox.receive(event); // relay redelivers on every tick (at-least-once)
            for (IEvent e : inbox.fetchUnprocessed(10)) {
                if (inbox.alreadyProcessed(e.getEventId())) {
                    continue;
                }
                sideEffectRuns.incrementAndGet();
                inbox.markProcessed(e.getEventId());
            }
        }

        assertEquals(1, sideEffectRuns.get(), "side effect ran exactly once despite redelivery");
    }
}
