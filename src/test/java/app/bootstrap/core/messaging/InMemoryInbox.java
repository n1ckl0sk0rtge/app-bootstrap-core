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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * In-memory reference {@link IInbox} that records processed event ids in a set: {@link
 * #markProcessed} inserts, {@link #alreadyProcessed} is a membership check. Thread-safe so several
 * consumer threads can dedupe concurrently. A durable implementation is a single table keyed by
 * event id.
 */
public final class InMemoryInbox implements IInbox {

    private final Set<UUID> processed = new HashSet<>();

    @Override
    public synchronized boolean alreadyProcessed(@Nonnull UUID eventId) {
        return processed.contains(eventId);
    }

    @Override
    public synchronized void markProcessed(@Nonnull UUID eventId) {
        processed.add(eventId);
    }

    /** Number of distinct event ids recorded as processed. */
    public synchronized int size() {
        return processed.size();
    }
}
