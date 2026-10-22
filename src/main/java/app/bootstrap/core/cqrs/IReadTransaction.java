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
import jakarta.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * Read-side transaction boundary, separate from the write-side {@link IUnitOfWork}.
 *
 * <p>The adapter owns transaction creation, isolation, suspension, restoration, and completion. A
 * boundary must never commit or roll back a transaction owned by its caller, even when the callback
 * throws. Neither mode waits for asynchronous projections to catch up; this contract provides no
 * write-side unit of work.
 *
 * <p>Unlike {@link IWithReadMode}, which selects visibility for each repository operation, this
 * boundary spans the complete synchronous callback on the calling thread. Repositories
 * participating in that boundary should use {@link ReadMode#CURRENT_TRANSACTION} to join it rather
 * than open their own snapshots.
 */
public interface IReadTransaction {

    /**
     * Executes the complete callback with the visibility selected by {@code mode}.
     *
     * <p>{@link ReadMode#CURRENT_TRANSACTION} joins the ambient transaction, including its pending
     * writes, or creates an ordinary read transaction if none exists. Only a transaction created by
     * this boundary may be completed by it.
     *
     * <p>{@link ReadMode#SNAPSHOT} opens a separate committed-data snapshot excluding ambient
     * pending writes. Consistency spans the complete callback. The ambient transaction is suspended
     * and restored afterward, including on failure. The adapter completes only its own snapshot.
     *
     * @param mode the transaction visibility for the callback
     * @param work the synchronous read operation
     * @param <R> the callback's result type
     * @return the callback's result unchanged, including null
     * @throws Exception the original exception from the callback, without wrapping
     */
    @Nullable <R> R execute(@Nonnull ReadMode mode, @Nonnull Callable<R> work) throws Exception;
}
