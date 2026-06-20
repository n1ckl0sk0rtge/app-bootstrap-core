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
package app.bootstrap.core.ddd;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent implements IDomainEvent {
    @Nonnull protected final UUID eventId;
    @Nonnull protected final Instant timestamp;
    @Nonnull protected final Id aggregateId;
    @Nonnull protected final Class<? extends AggregateRoot<?>> aggregateType;
    @Nullable protected final Long eventVersion;

    protected DomainEvent(
            @Nonnull Id aggregateId,
            @Nonnull Class<? extends AggregateRoot<?>> aggregateType,
            @Nullable Long eventVersion) {
        this(aggregateId, aggregateType, eventVersion, Clock.systemUTC());
    }

    protected DomainEvent(
            @Nonnull Id aggregateId, @Nonnull Class<? extends AggregateRoot<?>> aggregateType) {
        this(aggregateId, aggregateType, null, Clock.systemUTC());
    }

    /**
     * Emits a new event, taking its {@link #getTimestamp() timestamp} from the given clock. Inject
     * a fixed {@link Clock} in tests to make the timestamp deterministic; production code uses the
     * no-clock constructors, which default to {@link Clock#systemUTC()}. Pass {@code null} for
     * {@code eventVersion} when the event carries no version.
     */
    protected DomainEvent(
            @Nonnull Id aggregateId,
            @Nonnull Class<? extends AggregateRoot<?>> aggregateType,
            @Nullable Long eventVersion,
            @Nonnull Clock clock) {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now(clock);
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventVersion = eventVersion;
    }

    protected DomainEvent(
            @Nonnull UUID eventId,
            @Nonnull Instant timestamp,
            @Nonnull Id aggregateId,
            @Nonnull Class<? extends AggregateRoot<?>> aggregateType,
            @Nullable Long eventVersion) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventVersion = eventVersion;
    }

    @Override
    @Nonnull
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    @Nonnull
    public UUID getEventId() {
        return eventId;
    }

    @Nonnull
    public Id getAggregateId() {
        return aggregateId;
    }

    @Nonnull
    public Class<? extends AggregateRoot<?>> getAggregateType() {
        return aggregateType;
    }

    @Nullable public Long getEventVersion() {
        return eventVersion;
    }
}
