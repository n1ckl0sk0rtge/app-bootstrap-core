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

import jakarta.annotation.Nonnull;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory reference {@link IInbox} that models the durable mailbox as a per-event-id map of rows,
 * each carrying a monotonic receive-sequence and a processed flag. {@link #receive} inserts-or-
 * ignores (idempotent on event id), {@link #fetchUnprocessed} returns unprocessed rows oldest-first
 * by sequence, and {@link #markProcessed} flips the flag without deleting the row — the tombstone
 * survives so a fan-out redelivery lands as a no-op. Thread-safe so a relay can {@code receive}
 * while a consumer drains. A durable implementation is a single table per consumer keyed by event
 * id.
 */
public final class InMemoryInbox implements IInbox {

    private record Entry(IEvent event, long seq, boolean processed) {}

    private final Map<UUID, Entry> rows = new LinkedHashMap<>();
    private long seqGen = 0;

    @Override
    public synchronized void receive(@Nonnull IEvent event) {
        rows.putIfAbsent(event.getEventId(), new Entry(event, seqGen++, false)); // idempotent
    }

    @Nonnull
    @Override
    public synchronized List<IEvent> fetchUnprocessed(int limit) {
        return rows.values().stream()
                .filter(r -> !r.processed())
                .sorted(Comparator.comparingLong(Entry::seq))
                .limit(limit)
                .map(Entry::event)
                .toList();
    }

    @Override
    public synchronized boolean alreadyProcessed(@Nonnull UUID eventId) {
        final Entry r = rows.get(eventId);
        return r != null && r.processed();
    }

    @Override
    public synchronized void markProcessed(@Nonnull UUID eventId) {
        rows.computeIfPresent(eventId, (k, r) -> new Entry(r.event(), r.seq(), true));
    }

    /** Total rows held, processed and unprocessed — i.e. distinct events received. */
    public synchronized int size() {
        return rows.size();
    }

    /** Number of received-but-unprocessed rows still awaiting a drain. */
    public synchronized int unprocessedCount() {
        return (int) rows.values().stream().filter(r -> !r.processed()).count();
    }
}
