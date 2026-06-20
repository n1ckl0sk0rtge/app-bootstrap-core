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
package app.bootstrap.core.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryEventBusTest {

    static class BaseEvent implements IEvent {
        private final UUID id = UUID.randomUUID();
        private final Date timestamp = new Date();

        @Nonnull
        @Override
        public UUID getEventId() {
            return id;
        }

        @Nonnull
        @Override
        public Date getTimestamp() {
            return timestamp;
        }
    }

    static class ChildEvent extends BaseEvent {}

    static class OtherEvent extends BaseEvent {}

    @Test
    void typedSubscribeReceivesMatchingEvent() {
        final InMemoryEventBus bus = new InMemoryEventBus();
        final List<IEvent> received = new ArrayList<>();
        bus.subscribe(ChildEvent.class, received::add);

        final ChildEvent event = new ChildEvent();
        bus.publish(event);

        assertEquals(1, received.size());
        assertSame(event, received.get(0));
    }

    @Test
    void typedSubscribeIgnoresNonMatchingEvent() {
        final InMemoryEventBus bus = new InMemoryEventBus();
        final List<IEvent> received = new ArrayList<>();
        bus.subscribe(ChildEvent.class, received::add);

        bus.publish(new OtherEvent());

        assertTrue(received.isEmpty());
    }

    @Test
    void typedSubscribeReceivesSubclassEvents() {
        final InMemoryEventBus bus = new InMemoryEventBus();
        final List<IEvent> received = new ArrayList<>();
        bus.subscribe(BaseEvent.class, received::add);

        bus.publish(new ChildEvent());
        bus.publish(new OtherEvent());

        assertEquals(2, received.size());
    }

    @Test
    void subscribeAllReceivesEveryEvent() {
        final InMemoryEventBus bus = new InMemoryEventBus();
        final List<IEvent> received = new ArrayList<>();
        bus.subscribeAll(received::add);

        bus.publish(new ChildEvent());
        bus.publish(new OtherEvent());

        assertEquals(2, received.size());
    }

    @Test
    void sameListenerSubscribedTwiceReceivesTwice() {
        final InMemoryEventBus bus = new InMemoryEventBus();
        final List<IEvent> received = new ArrayList<>();
        final IEventListener<ChildEvent> listener = received::add;
        bus.subscribe(ChildEvent.class, listener);
        bus.subscribe(ChildEvent.class, listener);

        bus.publish(new ChildEvent());

        assertEquals(2, received.size());
    }

    @Test
    void unsubscribeRemovesOnlyOneRegistration() {
        final InMemoryEventBus bus = new InMemoryEventBus();
        final List<IEvent> received = new ArrayList<>();
        final IEventListener<ChildEvent> listener = received::add;
        bus.subscribe(ChildEvent.class, listener);
        bus.subscribe(ChildEvent.class, listener);

        bus.unsubscribe(ChildEvent.class, listener);
        bus.publish(new ChildEvent());

        assertEquals(1, received.size());
    }

    @Test
    void unsubscribeAllRemovesCatchAllOnly() {
        final InMemoryEventBus bus = new InMemoryEventBus();
        final List<IEvent> typedReceived = new ArrayList<>();
        final List<IEvent> allReceived = new ArrayList<>();
        final IEventListener<IEvent> catchAll = allReceived::add;
        bus.subscribe(ChildEvent.class, typedReceived::add);
        bus.subscribeAll(catchAll);

        bus.unsubscribeAll(catchAll);
        bus.publish(new ChildEvent());

        assertEquals(1, typedReceived.size());
        assertTrue(allReceived.isEmpty());
    }

    @Test
    void deliveryFollowsRegistrationOrder() {
        final InMemoryEventBus bus = new InMemoryEventBus();
        final List<String> order = new ArrayList<>();
        bus.subscribe(ChildEvent.class, e -> order.add("typed-1"));
        bus.subscribeAll(e -> order.add("all"));
        bus.subscribe(ChildEvent.class, e -> order.add("typed-2"));

        bus.publish(new ChildEvent());

        assertEquals(List.of("typed-1", "all", "typed-2"), order);
    }

    @Test
    void listenerExceptionStopsDeliveryAndIsRethrown() {
        final InMemoryEventBus bus = new InMemoryEventBus();
        final List<String> order = new ArrayList<>();
        bus.subscribe(ChildEvent.class, e -> order.add("first"));
        bus.subscribe(
                ChildEvent.class,
                e -> {
                    throw new IllegalStateException("boom");
                });
        bus.subscribe(ChildEvent.class, e -> order.add("third"));

        final RuntimeException thrown =
                assertThrows(RuntimeException.class, () -> bus.publish(new ChildEvent()));

        assertEquals("boom", thrown.getCause().getMessage());
        assertEquals(List.of("first"), order);
    }

    @Test
    void nestedPublishCompletesBeforeOuterContinues() {
        final InMemoryEventBus bus = new InMemoryEventBus();
        final List<String> order = new ArrayList<>();
        bus.subscribe(
                ChildEvent.class,
                e -> {
                    order.add("outer-start");
                    bus.publish(new OtherEvent());
                    order.add("outer-end");
                });
        bus.subscribe(OtherEvent.class, e -> order.add("nested"));
        bus.subscribe(ChildEvent.class, e -> order.add("after-outer"));

        bus.publish(new ChildEvent());

        assertEquals(List.of("outer-start", "nested", "outer-end", "after-outer"), order);
    }

    @Test
    void subscribeDuringDispatchDoesNotAffectCurrentPublish() {
        final InMemoryEventBus bus = new InMemoryEventBus();
        final List<String> received = new ArrayList<>();
        bus.subscribe(
                ChildEvent.class, e -> bus.subscribe(ChildEvent.class, e2 -> received.add("late")));

        bus.publish(new ChildEvent());
        assertTrue(received.isEmpty());

        bus.publish(new ChildEvent());
        assertEquals(List.of("late"), received);
    }
}
