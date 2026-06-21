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
package app.bootstrap.core.cqrs;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * In-memory reference {@link IUnitOfWork} that models commit/rollback without a real database. Work
 * enlisted via {@link #onCommit(Runnable)} is applied only if the block returns normally and
 * dropped if it throws, so a test can observe that writes are atomic. Not thread-safe and
 * non-reentrant — a teaching stand-in for a JPA {@code EntityManager} or a Spring {@code
 * TransactionTemplate}.
 */
public final class InMemoryUnitOfWork implements IUnitOfWork {

    private final List<Runnable> pendingWrites = new ArrayList<>();
    private boolean active;
    private int commits;
    private int rollbacks;

    /**
     * Enlists a write to apply when (and only when) the current unit of work commits. Stands in for
     * a repository/outbox enlisting in the ambient transaction.
     */
    public void onCommit(@Nonnull Runnable write) {
        if (!active) {
            throw new IllegalStateException("no active unit of work to enlist in");
        }
        pendingWrites.add(write);
    }

    @Override
    public <R> R execute(@Nonnull Callable<R> work) throws Exception {
        if (active) {
            throw new IllegalStateException("nested unit of work is not supported");
        }
        active = true;
        pendingWrites.clear();
        try {
            final R result = work.call();
            pendingWrites.forEach(Runnable::run); // commit: apply enlisted writes
            commits++;
            return result;
        } catch (Exception e) {
            pendingWrites.clear(); // rollback: discard enlisted writes
            rollbacks++;
            throw e;
        } finally {
            active = false;
        }
    }

    /** Number of units of work that committed. */
    public int commits() {
        return commits;
    }

    /** Number of units of work that rolled back. */
    public int rollbacks() {
        return rollbacks;
    }
}
