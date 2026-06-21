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
package app.bootstrap.core.cqrs;

import jakarta.annotation.Nonnull;

/**
 * The write side of a read model: projectors apply {@link IProjection projection} slices to it.
 *
 * <p>Keyed only by the id type {@code I} — like {@link IReadRepository} it never mentions the
 * persistence entity, so a use-case-layer {@link Projector} can depend on it without referencing
 * infrastructure. Many projectors may write to the same read model, each upserting its own
 * projection over a disjoint set of fields.
 *
 * <p><strong>Field ownership is shared; existence is single-owned.</strong> {@code upsert} is
 * field-scoped, additive, and commutative, so it is safe to distribute across many projectors.
 * Deletion is none of those: it is a whole-row, destructive, non-commutative operation, so it
 * cannot be distributed the same way and is therefore <em>not</em> on this port. The capability to
 * remove a row lives on {@link IDeletableProjectionStore}, which the read model's single
 * <em>lifecycle owner</em> depends on — see that type for the ownership rule. Field-contributing
 * projectors depend only on this narrower port and can never delete.
 *
 * <p>The read side lives in {@link IReadRepository}. An implementation typically realises both
 * (and, when its read model has a lifecycle owner, {@link IDeletableProjectionStore} as well).
 *
 * @param <I> the read model id type
 */
public interface IProjectionStore<I> {

    /**
     * Apply a partial write: merge the fields carried by {@code projection} into the row identified
     * by {@link IProjection#getId()}, creating the row when it does not yet exist.
     *
     * <p>This is a <em>field-scoped</em> write: only the fields the projection carries are touched;
     * fields owned by other projections are left untouched (never reset to defaults).
     */
    void upsert(@Nonnull IProjection<I> projection);
}
