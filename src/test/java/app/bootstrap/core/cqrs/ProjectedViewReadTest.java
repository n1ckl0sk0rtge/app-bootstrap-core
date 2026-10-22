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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProjectedViewReadTest {
    record Summary(String getId) implements IView<String> {}

    @Test
    void shouldPreserveFoundViewAndRejectNull() {
        final Summary view = new Summary("item-1");
        final ProjectedViewRead<Summary> read = ProjectedViewRead.found(view);

        assertSame(view, assertInstanceOf(ProjectedViewRead.Found.class, read).view());
        assertEquals(new ProjectedViewRead.Found<>(view), read);
        assertThrows(NullPointerException.class, () -> ProjectedViewRead.found(null));
        assertThrows(NullPointerException.class, () -> new ProjectedViewRead.Found<Summary>(null));
    }

    @Test
    void shouldDistinguishMissingEntityFromPendingProjection() {
        final ProjectedViewRead<Summary> missing = ProjectedViewRead.missing();
        final ProjectedViewRead<Summary> pending = ProjectedViewRead.pending();

        assertEquals(new ProjectedViewRead.Missing<Summary>(), missing);
        assertEquals(new ProjectedViewRead.Pending<Summary>(), pending);
        assertNotEquals(missing, pending);
        assertEquals("missing", describe(missing));
        assertEquals("pending", describe(pending));
        assertEquals("item-1", describe(ProjectedViewRead.found(new Summary("item-1"))));
    }

    private String describe(ProjectedViewRead<Summary> read) {
        return switch (read) {
            case ProjectedViewRead.Found<Summary> found -> found.view().getId();
            case ProjectedViewRead.Missing<Summary> ignored -> "missing";
            case ProjectedViewRead.Pending<Summary> ignored -> "pending";
        };
    }
}
