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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class AggregateRoot<T extends Id> extends Entity<T> {
    private int version;
    private final List<IDomainEvent> uncommitedChanges =
            Collections.synchronizedList(new ArrayList<>());

    protected AggregateRoot(@Nonnull T id, @Nonnull List<IDomainEvent> domainEvents) {
        super(id);
        this.uncommitedChanges.addAll(domainEvents);
    }

    protected AggregateRoot(@Nonnull T id, int version, @Nonnull List<IDomainEvent> domainEvents) {
        super(id);
        this.version = version;
        this.uncommitedChanges.addAll(domainEvents);
    }

    public final int getVersion() {
        return version;
    }

    public final int getNextVersion() {
        return version + uncommitedChanges.size();
    }

    public final void markChangesAsCommitted() {
        version = getNextVersion();
        uncommitedChanges.clear();
    }

    public final boolean hasUncommitedChanges() {
        return !uncommitedChanges.isEmpty();
    }

    @Nonnull
    public final List<IDomainEvent> getUncommittedChanges() {
        return Collections.unmodifiableList(uncommitedChanges);
    }

    protected final void apply(@Nonnull final IDomainEvent event) {
        uncommitedChanges.add(event);
    }

    // commit uncommited events
    public final void commit(@Nonnull Consumer<List<IDomainEvent>> publish) {
        publish.accept(Collections.unmodifiableList(uncommitedChanges));
        version++;
        uncommitedChanges.clear();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
