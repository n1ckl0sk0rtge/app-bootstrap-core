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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class InMemoryEventBus implements IEventBus {

    private static final class Subscription {
        @Nullable final Class<? extends IEvent> type;
        @Nonnull final IEventListener<?> listener;

        Subscription(@Nullable Class<? extends IEvent> type, @Nonnull IEventListener<?> listener) {
            this.type = type;
            this.listener = listener;
        }

        boolean matches(@Nonnull IEvent event) {
            return type == null || type.isInstance(event);
        }
    }

    @Nonnull private final List<Subscription> subscriptions = new ArrayList<>();

    @Override
    public <E extends IEvent> void subscribe(
            @Nonnull Class<E> type, @Nonnull IEventListener<? super E> listener) {
        subscriptions.add(new Subscription(type, listener));
    }

    @Override
    public <E extends IEvent> void unsubscribe(
            @Nonnull Class<E> type, @Nonnull IEventListener<? super E> listener) {
        removeFirst(type, listener);
    }

    @Override
    public void subscribeAll(@Nonnull IEventListener<? super IEvent> listener) {
        subscriptions.add(new Subscription(null, listener));
    }

    @Override
    public void unsubscribeAll(@Nonnull IEventListener<? super IEvent> listener) {
        removeFirst(null, listener);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void publish(@Nonnull IEvent event) {
        final List<Subscription> snapshot = new ArrayList<>(subscriptions);
        for (Subscription subscription : snapshot) {
            if (!subscription.matches(event)) {
                continue;
            }
            try {
                ((IEventListener) subscription.listener).handleEvent(event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void removeFirst(
            @Nullable Class<? extends IEvent> type, @Nonnull IEventListener<?> listener) {
        final Iterator<Subscription> iterator = subscriptions.iterator();
        while (iterator.hasNext()) {
            final Subscription subscription = iterator.next();
            if (subscription.type == type && subscription.listener == listener) {
                iterator.remove();
                return;
            }
        }
    }
}
