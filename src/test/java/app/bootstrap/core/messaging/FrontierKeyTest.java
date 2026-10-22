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

import java.util.Map;
import org.junit.jupiter.api.Test;

class FrontierKeyTest {
    @Test
    void shouldNamespaceEqualIdsAndUseBothComponentsForLookup() {
        final FrontierKey first = new FrontierKey("catalog-refresh", "run-1");
        final FrontierKey second = new FrontierKey("summary-refresh", "run-1");
        final Map<FrontierKey, Long> bookmarks = Map.of(first, 12L, second, 29L);

        assertEquals(12L, bookmarks.get(new FrontierKey("catalog-refresh", "run-1")));
        assertEquals(29L, bookmarks.get(new FrontierKey("summary-refresh", "run-1")));
        assertNotEquals(first, second);
        assertNotEquals(first, new FrontierKey("catalog-refresh", "run-2"));
    }

    @Test
    void shouldTreatStringsAsOpaqueWithoutDelimiterCollisionsOrNormalization() {
        assertNotEquals(new FrontierKey("a:b", "c"), new FrontierKey("a", "b:c"));
        assertNotEquals(new FrontierKey("scope", "id"), new FrontierKey("Scope", "id"));
        assertNotEquals(new FrontierKey("scope", "id"), new FrontierKey("scope ", "id"));
        assertEquals("", new FrontierKey("", "").id());
    }

    @Test
    void shouldRejectNullComponents() {
        assertThrows(NullPointerException.class, () -> new FrontierKey(null, "id"));
        assertThrows(NullPointerException.class, () -> new FrontierKey("scope", null));
    }
}
