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
 * The write side of a read model whose rows can be <em>removed</em>: {@link IProjectionStore} plus
 * whole-row {@link #delete(Object) delete}.
 *
 * <p><strong>Who owns deletion.</strong> Field ownership is shared across many projectors (see
 * {@link IProjectionStore}), but a row's <em>existence</em> is owned by exactly one of them — the
 * <em>lifecycle owner</em>: the projector that reacts to the source aggregate's existence events
 * (e.g. {@code UserRegistered} → first {@code upsert}, {@code UserDeleted} → {@code delete}). Only
 * that projector depends on this port; field-contributing projectors depend on the narrower {@link
 * IProjectionStore} and never delete. This mirrors the write side, where one aggregate is the
 * consistency boundary for its own lifecycle.
 *
 * <p><strong>When {@code delete} is sound.</strong> Only when a single aggregate's existence equals
 * the row's existence. A <em>composite</em> read model assembled from several aggregates has no
 * single lifecycle, so "this aggregate went away" should null out that aggregate's slice with
 * another field-scoped {@code upsert} — not {@code delete} the whole row, which would destroy
 * fields owned by still-living aggregates. Read models that are append-only (audit/event logs) or
 * whose lifecycle is owned elsewhere simply do not implement this port.
 *
 * <p><strong>Beware resurrection under at-least-once delivery.</strong> A field {@code upsert} can
 * arrive <em>after</em> the {@code delete} and re-create ("resurrect") the row. Make {@code delete}
 * idempotent and, where ordering can invert, tombstone the id so a late bare {@code upsert} does
 * not insert — the same concern as the inbox tombstone in the messaging layer.
 *
 * @param <I> the read model id type
 */
public interface IDeletableProjectionStore<I> extends IProjectionStore<I> {

    /** Remove the read model identified by {@code id}, including every view of it. */
    void delete(@Nonnull I id);
}
