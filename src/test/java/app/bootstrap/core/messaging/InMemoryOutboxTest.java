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

import app.bootstrap.core.ddd.IDomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryOutboxTest {

    /** A plain system event — not a domain event. */
    record TestEvent(UUID getEventId, Instant getTimestamp) implements IEvent {}

    /** A domain event, to prove the wildcard accepts {@code List<IDomainEvent>}. */
    record TestDomainEvent(UUID getEventId, Instant getTimestamp) implements IDomainEvent {}

    private static TestEvent event() {
        return new TestEvent(UUID.randomUUID(), Instant.now());
    }

    private static List<UUID> idsOf(List<IEvent> events) {
        return events.stream().map(IEvent::getEventId).toList();
    }

    private InMemoryOutbox outbox;

    @BeforeEach
    void setUp() {
        outbox = new InMemoryOutbox();
    }

    @Test
    void shouldReturnEmptyWhenNothingStaged() {
        assertTrue(outbox.fetchUnpublished(10).isEmpty());
        assertEquals(0, outbox.size());
    }

    @Test
    void shouldStageAndFetchOldestFirst() {
        TestEvent first = event();
        TestEvent second = event();
        outbox.add(List.of(first, second));

        List<IEvent> fetched = outbox.fetchUnpublished(10);

        assertEquals(List.of(first.getEventId(), second.getEventId()), idsOf(fetched));
        assertEquals(2, outbox.size());
    }

    @Test
    void shouldPreserveOrderAcrossMultipleAdds() {
        TestEvent first = event();
        TestEvent second = event();
        TestEvent third = event();
        outbox.add(List.of(first));
        outbox.add(List.of(second, third));

        assertEquals(
                List.of(first.getEventId(), second.getEventId(), third.getEventId()),
                idsOf(outbox.fetchUnpublished(10)));
    }

    @Test
    void shouldRespectLimit() {
        TestEvent first = event();
        TestEvent second = event();
        TestEvent third = event();
        outbox.add(List.of(first, second, third));

        List<IEvent> fetched = outbox.fetchUnpublished(2);

        assertEquals(List.of(first.getEventId(), second.getEventId()), idsOf(fetched));
    }

    @Test
    void shouldExcludePublishedAfterAck() {
        TestEvent first = event();
        TestEvent second = event();
        outbox.add(List.of(first, second));

        outbox.markPublished(List.of(first.getEventId()));

        assertEquals(List.of(second.getEventId()), idsOf(outbox.fetchUnpublished(10)));
        assertEquals(1, outbox.size());
    }

    @Test
    void shouldReturnUnacknowledgedEventsAgainUntilMarked() {
        TestEvent event = event();
        outbox.add(List.of(event));

        // A relay that fetches but never acknowledges (e.g. crashed before markPublished)
        // must see the event again — this is the at-least-once guarantee.
        assertEquals(List.of(event.getEventId()), idsOf(outbox.fetchUnpublished(10)));
        assertEquals(List.of(event.getEventId()), idsOf(outbox.fetchUnpublished(10)));

        outbox.markPublished(List.of(event.getEventId()));
        assertTrue(outbox.fetchUnpublished(10).isEmpty());
    }

    @Test
    void shouldIgnoreUnknownIdsOnMarkPublished() {
        TestEvent event = event();
        outbox.add(List.of(event));

        outbox.markPublished(List.of(UUID.randomUUID()));

        assertEquals(List.of(event.getEventId()), idsOf(outbox.fetchUnpublished(10)));
    }

    @Test
    void shouldAcceptDomainEventListThroughWildcard() {
        // Mirrors aggregate.commit(outbox::add): a List<IDomainEvent> must be accepted by
        // add(List<? extends IEvent>) and come back out as plain IEvents.
        List<IDomainEvent> domainEvents =
                List.of(
                        new TestDomainEvent(UUID.randomUUID(), Instant.now()),
                        new TestDomainEvent(UUID.randomUUID(), Instant.now()));
        outbox.add(domainEvents);

        assertEquals(
                domainEvents.stream().map(IDomainEvent::getEventId).toList(),
                idsOf(outbox.fetchUnpublished(10)));
    }
}
