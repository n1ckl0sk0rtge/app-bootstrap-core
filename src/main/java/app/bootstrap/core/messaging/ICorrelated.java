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

import jakarta.annotation.Nullable;
import java.util.UUID;

/**
 * Opt-in mixin that lets a message (an {@link IEvent}, or a command / query) carry the two ids that
 * reconstruct <em>why</em> it exists: a correlation id and a causation id.
 *
 * <p>A single user action typically fans out into a chain of work — a command produces events, an
 * event triggers a follow-up command, a process manager advances and emits more commands. Without
 * shared ids that chain is invisible once the messages cross a bus; with them you can stitch the
 * whole flow back together for tracing, debugging, auditing, and saga correlation.
 *
 * <ul>
 *   <li><strong>Correlation id</strong> — constant for every message in the same end-to-end flow.
 *       It is stamped once on the first message and copied unchanged onto everything that descends
 *       from it, so "show me everything that happened because of this request" is a single lookup.
 *   <li><strong>Causation id</strong> — the id of the <em>immediate</em> message that caused this
 *       one (its parent). It reconstructs the precise edges of the causality tree, not just the
 *       flow it belongs to.
 * </ul>
 *
 * <p>The convention when deriving a child message {@code c} from a parent {@code p}: {@code
 * c.correlationId = p.correlationId} and {@code c.causationId = p}'s own id. A <em>root</em>
 * message (one that starts a flow) has no parent: its causation id is {@code null} and its
 * correlation id is conventionally set to its own id. Both accessors are nullable so a message that
 * was never enriched (or a root, for causation) is representable.
 *
 * <p>This is intentionally a small, optional interface rather than a field on {@link IEvent} or
 * {@code ICommand}: most messages do not need correlation, and forcing the ids onto every event
 * would bloat the core contracts. Implement it only on the messages whose lineage you want to
 * track.
 */
public interface ICorrelated {

    /**
     * The id shared by every message in the same end-to-end flow, or {@code null} if this message
     * was not enriched with one.
     */
    @Nullable UUID getCorrelationId();

    /**
     * The id of the immediate message that caused this one, or {@code null} if this message is the
     * root of its flow (it has no parent).
     */
    @Nullable UUID getCausationId();
}
