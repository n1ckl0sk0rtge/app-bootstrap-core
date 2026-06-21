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

import app.bootstrap.core.ddd.IDomainEvent;
import app.bootstrap.core.ddd.IDomainEventBus;
import app.bootstrap.core.ddd.IDomainEventListener;
import app.bootstrap.core.messaging.IEvent;
import app.bootstrap.core.messaging.IEventListener;
import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReadRepositoryTest {

    // --- Views: use-case-owned read slices, keyed by id only (no entity type) ----------------

    /** Widest view — the whole logical record. */
    record UserView(String getId, String name, String email, int age) implements IView<String> {}

    /** A narrow view: id + email only. */
    record UserContactView(String getId, String email) implements IView<String> {}

    // --- Projections: use-case-owned write slices, keyed by id only --------------------------

    /** Carries every field — used to create the read model. */
    record UserRegisteredProjection(String getId, String name, String email, int age)
            implements IProjection<String> {}

    /** A disjoint field owned by a different projector. */
    record UserNameProjection(String getId, String name) implements IProjection<String> {}

    /** Another disjoint field, owned by yet another projector. */
    record UserAgeProjection(String getId, int age) implements IProjection<String> {}

    // --- Concrete in-memory repository: realises both the read and the write port ------------
    //
    // The `Row` is the persistence shape. It is private to the adapter — it is NEVER a port type,
    // so it stands in for the JPA entity that must not cross the use-case boundary. The adapter
    // maps
    // Row <-> the use-case-owned view / projection DTOs.

    record Row(String id, String name, String email, int age) {}

    static final class InMemoryUserReadRepository extends ReadRepository<String> {

        private final Map<String, Row> store = new ConcurrentHashMap<>();

        InMemoryUserReadRepository(@Nonnull IDomainEventBus bus) {
            super(bus);
        }

        @Nonnull
        @Override
        public <V extends IView<String>> Optional<V> read(
                @Nonnull String id, @Nonnull Class<V> view) {
            return Optional.ofNullable(store.get(id)).map(row -> view.cast(project(row, view)));
        }

        @Override
        public void upsert(@Nonnull IProjection<String> projection) {
            // Field-scoped merge, create-if-absent: only the carried fields are touched.
            store.compute(projection.getId(), (id, current) -> apply(current, projection));
        }

        @Override
        public void delete(@Nonnull String id) {
            store.remove(id);
        }

        private Row apply(Row current, IProjection<String> projection) {
            String name = current == null ? null : current.name();
            String email = current == null ? null : current.email();
            int age = current == null ? 0 : current.age();
            if (projection instanceof UserRegisteredProjection p) {
                name = p.name();
                email = p.email();
                age = p.age();
            } else if (projection instanceof UserNameProjection p) {
                name = p.name();
            } else if (projection instanceof UserAgeProjection p) {
                age = p.age();
            } else {
                throw new IllegalArgumentException(
                        "unsupported projection: " + projection.getClass().getName());
            }
            return new Row(projection.getId(), name, email, age);
        }

        private IView<String> project(Row row, Class<?> view) {
            if (view == UserView.class) {
                return new UserView(row.id(), row.name(), row.email(), row.age());
            }
            if (view == UserContactView.class) {
                return new UserContactView(row.id(), row.email());
            }
            throw new IllegalArgumentException("unsupported view: " + view.getName());
        }
    }

    // --- Fixtures ----------------------------------------------------------------------------

    private InMemoryUserReadRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserReadRepository(new NoOpDomainEventBus());
    }

    private void register() {
        repository.upsert(new UserRegisteredProjection("u1", "Alice", "alice@example.com", 30));
    }

    // --- Create + partial write via projections (upsert) -------------------------------------

    @Test
    void shouldCreateReadModelOnFirstUpsert() {
        register();

        final UserView user = repository.read("u1", UserView.class).orElseThrow();
        assertEquals(new UserView("u1", "Alice", "alice@example.com", 30), user);
    }

    @Test
    void shouldUpsertOnlyProjectedFieldAndKeepTheRest() {
        register();

        repository.upsert(new UserNameProjection("u1", "Alice Updated"));

        final UserView user = repository.read("u1", UserView.class).orElseThrow();
        assertEquals("Alice Updated", user.name());
        // fields owned by other projections stay intact
        assertEquals("alice@example.com", user.email());
        assertEquals(30, user.age());
    }

    @Test
    void shouldReturnEmptyWhenModelMissing() {
        assertTrue(repository.read("missing", UserView.class).isEmpty());
    }

    // --- Many views over one read model ------------------------------------------------------

    @Test
    void shouldMaterialiseDifferentViewsOfTheSameReadModel() {
        register();

        final UserContactView contact = repository.read("u1", UserContactView.class).orElseThrow();
        final UserView full = repository.read("u1", UserView.class).orElseThrow();

        assertEquals(new UserContactView("u1", "alice@example.com"), contact);
        assertEquals("Alice", full.name());
    }

    // --- Many projectors maintaining disjoint fields of one read model -----------------------

    @Test
    void shouldLetMultipleProjectorsMaintainOneReadModel() {
        register();

        // Two independent projectors, each owning a disjoint field, share the same write port.
        final IProjector<IEvent> nameProjector =
                new Projector<String, IEvent>(new NoOpDomainEventBus(), repository) {
                    @Override
                    public void handleEvent(@Nonnull IEvent event) {
                        if (event instanceof UserRenamed e) {
                            store.upsert(new UserNameProjection(e.id(), e.name()));
                        }
                    }
                };
        final IProjector<IEvent> ageProjector =
                new Projector<String, IEvent>(new NoOpDomainEventBus(), repository) {
                    @Override
                    public void handleEvent(@Nonnull IEvent event) {
                        if (event instanceof BirthdayHad e) {
                            store.upsert(new UserAgeProjection(e.id(), e.age()));
                        }
                    }
                };

        assertDoesNotThrow(() -> nameProjector.handleEvent(new UserRenamed("u1", "Alice B")));
        assertDoesNotThrow(() -> ageProjector.handleEvent(new BirthdayHad("u1", 31)));

        final UserView user = repository.read("u1", UserView.class).orElseThrow();
        assertEquals("Alice B", user.name()); // written by nameProjector
        assertEquals(31, user.age()); // written by ageProjector
        assertEquals("alice@example.com", user.email()); // untouched by either
    }

    // --- Delete ------------------------------------------------------------------------------

    @Test
    void shouldDeleteModel() {
        register();

        repository.delete("u1");

        assertTrue(repository.read("u1", UserView.class).isEmpty());
    }

    // --- Test domain events ------------------------------------------------------------------

    record UserRenamed(String id, String name) implements IDomainEvent {
        @Override
        @Nonnull
        public UUID getEventId() {
            return UUID.fromString("00000000-0000-0000-0000-000000000001");
        }

        @Override
        @Nonnull
        public Instant getTimestamp() {
            return Instant.EPOCH;
        }
    }

    record BirthdayHad(String id, int age) implements IDomainEvent {
        @Override
        @Nonnull
        public UUID getEventId() {
            return UUID.fromString("00000000-0000-0000-0000-000000000002");
        }

        @Override
        @Nonnull
        public Instant getTimestamp() {
            return Instant.EPOCH;
        }
    }

    // --- No-op domain event bus (the bases require one) --------------------------------------

    static final class NoOpDomainEventBus implements IDomainEventBus {
        @Override
        public <E extends IEvent> void subscribe(
                @Nonnull Class<E> type, @Nonnull IEventListener<? super E> listener) {}

        @Override
        public <E extends IEvent> void unsubscribe(
                @Nonnull Class<E> type, @Nonnull IEventListener<? super E> listener) {}

        @Override
        public void subscribeAll(@Nonnull IEventListener<? super IEvent> listener) {}

        @Override
        public void unsubscribeAll(@Nonnull IEventListener<? super IEvent> listener) {}

        @Override
        public void publish(@Nonnull IEvent event) {}

        @Override
        public void subscribe(@Nonnull IDomainEventListener listener) {}

        @Override
        public void unsubscribe(@Nonnull IDomainEventListener listener) {}

        @Override
        public void publish(@Nonnull IDomainEvent event) {}
    }
}
