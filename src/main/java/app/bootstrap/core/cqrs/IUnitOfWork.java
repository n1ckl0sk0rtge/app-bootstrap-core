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
import java.util.concurrent.Callable;

/**
 * The atomic transaction boundary for a write: runs a block of work so that <em>every</em> state
 * change inside it commits together, or none of it does.
 *
 * <p>A {@link CommandHandler} must persist an aggregate <em>and</em> stage its events in {@link
 * app.bootstrap.core.messaging.IOutbox} as one atomic step (see {@link CommandHandler} for why the
 * dual write is fatal otherwise). When a single command legitimately changes more than one
 * aggregate — or also records a {@link ITrackedCommand command-tracking} transition — all of those
 * writes must share one boundary too. {@code IUnitOfWork} is that boundary expressed as a type, so
 * it can be applied uniformly (e.g. by a bus decorator) instead of relying on every handler to
 * remember to open a transaction.
 *
 * <pre>{@code
 * uow.execute(() -> {
 *     repository.save(order);          // state change A   ┐
 *     ownerRepo.save(owner);           // state change B   │ all commit together,
 *     order.commit(outbox::add);       // staged events    │ or all roll back
 *     owner.commit(outbox::add);       // staged events    ┘
 *     return null;
 * });
 * }</pre>
 *
 * <p><strong>Semantics.</strong> {@link #execute} runs {@code work}, commits if it returns
 * normally, and rolls back if it throws — the original exception propagates after rollback. The
 * work runs on the calling thread, so the boundary is thread-bound: it wraps <em>synchronous</em>
 * dispatch ({@code sendSync}) cleanly, but asynchronous {@code send} must enter the unit of work on
 * the worker thread that actually executes the handler, not on the thread that enqueues it.
 *
 * <p><strong>Not for queries.</strong> Queries do not mutate, so there is nothing to commit or roll
 * back — a query bus needs at most a <em>read-only</em> transaction for snapshot consistency, which
 * is a separate concern from this write-side unit of work.
 *
 * <p>The library ships no implementation: a real one delegates to the surrounding infrastructure —
 * a JPA {@code EntityManager} (itself a unit of work), a Spring {@code @Transactional} / {@code
 * TransactionTemplate}, or a JTA transaction. It is deliberately framework-agnostic so the
 * application layer can depend on the boundary without depending on the transaction manager.
 */
public interface IUnitOfWork {

    /**
     * Runs {@code work} inside a single transaction: commits and returns its result if it completes
     * normally, or rolls back and rethrows if it throws.
     *
     * @param work the unit of work to run atomically; its return value is passed back to the caller
     * @param <R> the result type produced by the work
     * @return whatever {@code work} returned
     * @throws Exception if {@code work} throws; the transaction is rolled back first and the
     *     original exception propagates
     */
    <R> R execute(@Nonnull Callable<R> work) throws Exception;
}
