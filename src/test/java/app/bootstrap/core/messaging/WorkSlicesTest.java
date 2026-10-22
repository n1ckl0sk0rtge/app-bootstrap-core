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

import static org.junit.jupiter.api.Assertions.*;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WorkSlicesTest {
    @Test
    void shouldPreserveOrderAndContinueUntilFinalSlice() {
        final List<String> work = List.of("c", "a", "b", "d", "e");
        final var first = WorkSlices.slice(work, 0, 2);
        assertEquals(List.of("c", "a"), first.items());
        assertEquals(new InboxStepResult.Continue(2), first.result());

        final var middle = WorkSlices.slice(work, 2, 2);
        assertEquals(List.of("b", "d"), middle.items());
        assertEquals(new InboxStepResult.Continue(4), middle.result());

        final var last = WorkSlices.slice(work, 4, 2);
        assertEquals(List.of("e"), last.items());
        assertEquals(new InboxStepResult.Complete(), last.result());
    }

    @Test
    void shouldCompleteAtExactBoundaryAndForEmptyWork() {
        assertEquals(
                new InboxStepResult.Complete(), WorkSlices.slice(List.of(1, 2), 0, 2).result());
        final var exhausted = WorkSlices.slice(List.of(1, 2), 2, 1);
        assertTrue(exhausted.items().isEmpty());
        assertEquals(new InboxStepResult.Complete(), exhausted.result());
        assertEquals(exhausted, WorkSlices.slice(List.of(), 0, 1));
    }

    @Test
    void shouldCopySliceAndPreventStructuralChanges() {
        final List<String> work = new ArrayList<>(List.of("a", "b", "c"));
        final var slice = WorkSlices.slice(work, 0, 2);
        work.set(0, "changed");
        work.clear();

        assertEquals(List.of("a", "b"), slice.items());
        assertThrows(UnsupportedOperationException.class, () -> slice.items().add("d"));
        assertThrows(UnsupportedOperationException.class, () -> slice.items().set(0, "d"));
    }

    @Test
    void shouldRejectInvalidInputsEvenForEmptyWork() {
        assertThrows(IllegalArgumentException.class, () -> WorkSlices.slice(List.of(1), -1, 1));
        assertThrows(IllegalArgumentException.class, () -> WorkSlices.slice(List.of(1), 2, 1));
        assertThrows(IllegalArgumentException.class, () -> WorkSlices.slice(List.of(), 0, 0));
        assertThrows(IllegalArgumentException.class, () -> WorkSlices.slice(List.of(), 0, -1));
        assertThrows(NullPointerException.class, () -> WorkSlices.slice(null, 0, 1));
    }

    @Test
    void shouldAvoidOverflowWithLargeLimitAndNearMaximumOffset() {
        assertEquals(
                List.of(2, 3), WorkSlices.slice(List.of(1, 2, 3), 1, Integer.MAX_VALUE).items());
        // Virtual list exercises the int boundary without allocating billions of items.
        final List<Integer> huge =
                new AbstractList<>() {
                    @Override
                    public Integer get(int index) {
                        return index;
                    }

                    @Override
                    public int size() {
                        return Integer.MAX_VALUE;
                    }
                };

        final var slice = WorkSlices.slice(huge, Integer.MAX_VALUE - 2, Integer.MAX_VALUE);
        assertEquals(List.of(Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1), slice.items());
        assertEquals(new InboxStepResult.Complete(), slice.result());
    }
}
