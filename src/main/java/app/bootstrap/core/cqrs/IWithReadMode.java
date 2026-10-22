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

/**
 * Opt-in capability for selecting a repository's {@link ReadMode read mode}.
 *
 * <p>Adopting repositories default to {@link ReadMode#SNAPSHOT}. Implementations return a new
 * immutable repository instance sharing the same dependencies and using the selected mode for
 * subsequent read operations. Existing instances remain unchanged.
 *
 * <p>Bind {@code R} to the consumer's repository interface to preserve its methods in fluent calls:
 *
 * <pre>{@code
 * public interface IAssetReadRepository
 *         extends IReadRepository<AssetId>, IWithReadMode<IAssetReadRepository> {
 *     @Nonnull
 *     List<AssetView> list(@Nonnull ProjectId projectId);
 * }
 *
 * assets.withReadMode(ReadMode.CURRENT_TRANSACTION).list(projectId);
 * }</pre>
 *
 * <p>This capability is independent of {@link IReadRepository}; existing repositories need not
 * adopt it. Transaction execution belongs in infrastructure. Core provides no default
 * implementation that ignores the selected mode.
 *
 * @param <R> the consumer-specific repository type returned by mode selection
 */
public interface IWithReadMode<R> {

    /**
     * Returns a new immutable repository configured to honor {@code mode}, leaving existing
     * repository instances unchanged.
     *
     * <p>Selection itself does not open a transaction, group multiple operations into one snapshot,
     * or wait for projections. The adapter applies the mode when each read operation executes. Use
     * {@link IReadTransaction} for a boundary spanning multiple reads, and select {@link
     * ReadMode#CURRENT_TRANSACTION} on participating repositories to join that boundary.
     *
     * @param mode the transaction visibility for subsequent read operations
     * @return a repository sharing the same dependencies and using the selected mode
     */
    @Nonnull
    R withReadMode(@Nonnull ReadMode mode);
}
