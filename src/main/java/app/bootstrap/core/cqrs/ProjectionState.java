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

/**
 * Immutable freshness state suitable for inclusion in application-layer {@link IView views}.
 *
 * <p>The adapter owns revision allocation, ordering, persistence, and updates. This value only
 * describes observed state; reading it does not wait for projection work to finish. Revisions are
 * opaque here: no numeric ordering or allocation policy is enforced.
 *
 * @param requiredRevision the revision currently requested
 * @param completedRevision the revision whose computation completed
 * @param failedRevision the revision whose computation failed, or null if none
 */
public record ProjectionState(
        long requiredRevision, long completedRevision, @Nullable Long failedRevision) {

    /** Freshness of the currently required revision. */
    public enum Status {
        READY,
        COMPUTING,
        FAILED
    }

    /**
     * Returns FAILED for a failure at the required revision, even if that revision also completed;
     * otherwise READY for an exact completion match, or COMPUTING. An obsolete failure is ignored.
     */
    @Nonnull
    public Status status() {
        if (failedRevision != null && failedRevision.longValue() == requiredRevision) {
            return Status.FAILED;
        }
        return completedRevision == requiredRevision ? Status.READY : Status.COMPUTING;
    }
}
