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

import java.util.Date;
import java.util.UUID;

public abstract class DomainEvent implements IDomainEvent {
    @Nonnull protected final UUID eventId;
    @Nonnull protected final Date timestamp;
    @Nonnull protected final Id aggregateId;
    @Nonnull protected final Class<? extends AggregateRoot<?>> aggregateType;
    @Nullable protected final Long eventVersion;

    protected DomainEvent(
            @Nonnull Id aggregateId,
            @Nonnull Class<? extends AggregateRoot<?>> aggregateType,
            @Nullable Long eventVersion) {
        this.eventId = UUID.randomUUID();
        this.timestamp = new Date(System.currentTimeMillis());
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventVersion = eventVersion;
    }

    protected DomainEvent(@Nonnull UUID eventId,
                       @Nonnull Date timestamp,
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
    public Date getTimestamp() {
        return timestamp;
    }
}
