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

import static app.bootstrap.core.cqrs.ProjectionState.Status.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProjectionStateTest {
    @Test
    void shouldDescribeSuccessiveRevisionsWithoutMutatingPreviousState() {
        final ProjectionState initial = new ProjectionState(1, 1, null);
        final ProjectionState requested = new ProjectionState(2, 1, null);
        final ProjectionState completed = new ProjectionState(2, 2, null);

        assertEquals(READY, initial.status());
        assertEquals(COMPUTING, requested.status());
        assertEquals(READY, completed.status());
        assertEquals(new ProjectionState(1, 1, null), initial);
    }

    @Test
    void shouldIgnoreObsoleteFailuresForBothComputingAndReadyRevisions() {
        assertEquals(FAILED, new ProjectionState(500, 499, 500L).status());
        assertEquals(COMPUTING, new ProjectionState(501, 499, 500L).status());
        assertEquals(READY, new ProjectionState(501, 501, 500L).status());
    }

    @Test
    void shouldGiveCurrentFailurePrecedenceOverCompletion() {
        assertEquals(FAILED, new ProjectionState(500, 500, 500L).status());
        assertEquals(READY, new ProjectionState(500, 500, null).status());
    }

    @Test
    void shouldUseExactRevisionEqualityWithoutAssumingNumericOrdering() {
        assertEquals(COMPUTING, new ProjectionState(3, 4, 5L).status());
        assertEquals(READY, new ProjectionState(Long.MIN_VALUE, Long.MIN_VALUE, null).status());
        assertEquals(FAILED, new ProjectionState(Long.MAX_VALUE, 0, Long.MAX_VALUE).status());
    }
}
