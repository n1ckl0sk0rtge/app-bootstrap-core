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

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MessageContractsTest {

    /** An integration event is an {@link IEvent} flagged for cross-boundary publication. */
    record OrderPlacedIntegrationEvent(UUID getEventId, Instant getTimestamp, String orderId)
            implements IIntegrationEvent {}

    /** A correlated event carrying its lineage ids alongside the base event fields. */
    record CorrelatedEvent(
            UUID getEventId, Instant getTimestamp, UUID getCorrelationId, UUID getCausationId)
            implements IEvent, ICorrelated {}

    @Test
    void integrationEventIsAnIEvent() {
        IEvent event = new OrderPlacedIntegrationEvent(UUID.randomUUID(), Instant.now(), "order-1");

        assertInstanceOf(IIntegrationEvent.class, event);
        assertNotNull(event.getEventId());
    }

    @Test
    void rootMessageHasNoCausationAndCorrelatesToItself() {
        UUID rootId = UUID.randomUUID();
        // Convention for a flow-starting message: correlationId == own id, causationId == null.
        CorrelatedEvent root = new CorrelatedEvent(rootId, Instant.now(), rootId, null);

        assertEquals(rootId, root.getCorrelationId());
        assertNull(root.getCausationId());
    }

    @Test
    void childCopiesCorrelationAndPointsCausationAtParent() {
        UUID rootId = UUID.randomUUID();
        CorrelatedEvent root = new CorrelatedEvent(rootId, Instant.now(), rootId, null);

        // Deriving a child: keep the correlation id, set causation to the parent's own id.
        CorrelatedEvent child =
                new CorrelatedEvent(
                        UUID.randomUUID(),
                        Instant.now(),
                        root.getCorrelationId(),
                        root.getEventId());

        assertEquals(root.getCorrelationId(), child.getCorrelationId());
        assertEquals(root.getEventId(), child.getCausationId());
    }
}
