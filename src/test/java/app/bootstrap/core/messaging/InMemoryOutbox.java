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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory reference {@link IOutbox} that models the delete-after-publish strategy: a staged event
 * is held until acknowledged, then dropped. Insertion order is preserved so {@link
 * #fetchUnpublished(int)} returns events oldest first. Thread-safe so a relay thread can drain
 * while writers stage.
 */
public final class InMemoryOutbox implements IOutbox {

    private final Map<UUID, IEvent> unpublished = new LinkedHashMap<>();

    @Override
    public synchronized void add(@Nonnull List<? extends IEvent> events) {
        for (final IEvent event : events) {
            unpublished.put(event.getEventId(), event);
        }
    }

    @Nonnull
    @Override
    public synchronized List<IEvent> fetchUnpublished(int limit) {
        return unpublished.values().stream().limit(limit).toList();
    }

    @Override
    public synchronized void markPublished(@Nonnull List<UUID> eventIds) {
        eventIds.forEach(unpublished::remove);
    }

    /** Number of staged events not yet acknowledged. */
    public synchronized int size() {
        return unpublished.size();
    }
}
