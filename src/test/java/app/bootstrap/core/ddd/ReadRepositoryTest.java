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
package app.bootstrap.core.ddd;

import static org.junit.jupiter.api.Assertions.*;

import app.bootstrap.core.cqrs.IEvent;
import app.bootstrap.core.cqrs.IEventListener;
import app.bootstrap.core.cqrs.IProjection;
import app.bootstrap.core.cqrs.IReadModel;
import app.bootstrap.core.cqrs.IView;
import jakarta.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReadRepositoryTest {

    // --- Test read model and its slices -------------------------------------

    record UserReadModel(String getId, String name, String email, int age)
            implements IReadModel<String> {}

    /** Partial WRITE slice: only the name. */
    record UserNameProjection(String getId, String name)
            implements IProjection<String, UserReadModel> {}

    /** Partial READ slice: id + email only. */
    record UserContactView(String getId, String email) implements IView<String, UserReadModel> {}

    // --- Concrete in-memory repository under test ----------------------------

    static final class InMemoryUserReadRepository extends ReadRepository<String, UserReadModel> {

        private final Map<String, UserReadModel> store = new ConcurrentHashMap<>();

        InMemoryUserReadRepository(@Nonnull IDomainEventBus bus) {
            super(bus);
        }

        @Nonnull
        @Override
        public Optional<UserReadModel> read(@Nonnull String id) {
            return Optional.ofNullable(store.get(id));
        }

        @Nonnull
        @Override
        public <V extends IView<String, UserReadModel>> Optional<V> read(
                @Nonnull String id, @Nonnull Class<V> view) {
            return read(id).map(model -> view.cast(projectView(model, view)));
        }

        @Override
        public void save(@Nonnull UserReadModel readModel) {
            store.put(readModel.getId(), readModel);
        }

        @Override
        public void upsert(@Nonnull IProjection<String, UserReadModel> projection) {
            final UserReadModel current =
                    read(projection.getId())
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "read model not found: " + projection.getId()));
            store.put(projection.getId(), merge(current, projection));
        }

        @Override
        public void delete(@Nonnull String id) {
            store.remove(id);
        }

        private UserReadModel merge(
                UserReadModel current, IProjection<String, UserReadModel> projection) {
            if (projection instanceof UserNameProjection p) {
                return new UserReadModel(current.getId(), p.name(), current.email(), current.age());
            }
            throw new IllegalArgumentException(
                    "unsupported projection: " + projection.getClass().getName());
        }

        private IView<String, UserReadModel> projectView(UserReadModel model, Class<?> view) {
            if (view == UserContactView.class) {
                return new UserContactView(model.getId(), model.email());
            }
            throw new IllegalArgumentException("unsupported view: " + view.getName());
        }
    }

    // --- Fixtures ------------------------------------------------------------

    private InMemoryUserReadRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserReadRepository(new NoOpDomainEventBus());
    }

    private static UserReadModel alice() {
        return new UserReadModel("u1", "Alice", "alice@example.com", 30);
    }

    // --- Full read / save ----------------------------------------------------

    @Test
    void shouldSaveAndReadFullModel() {
        repository.save(alice());

        final Optional<UserReadModel> found = repository.read("u1");

        assertTrue(found.isPresent());
        assertEquals(alice(), found.get());
    }

    @Test
    void shouldReturnEmptyWhenModelMissing() {
        assertTrue(repository.read("missing").isEmpty());
    }

    // --- Partial write via projection (upsert) -------------------------------

    @Test
    void shouldUpsertOnlyProjectedFieldAndKeepTheRest() {
        repository.save(alice());

        repository.upsert(new UserNameProjection("u1", "Alice Updated"));

        final UserReadModel updated = repository.read("u1").orElseThrow();
        assertEquals("Alice Updated", updated.name());
        // untouched fields stay intact
        assertEquals("alice@example.com", updated.email());
        assertEquals(30, updated.age());
    }

    @Test
    void shouldRejectUpsertWhenModelDoesNotExist() {
        assertThrows(
                IllegalStateException.class,
                () -> repository.upsert(new UserNameProjection("ghost", "Nobody")));
    }

    // --- Partial read via view -----------------------------------------------

    @Test
    void shouldReadViewSubset() {
        repository.save(alice());

        final Optional<UserContactView> view = repository.read("u1", UserContactView.class);

        assertTrue(view.isPresent());
        assertEquals("u1", view.get().getId());
        assertEquals("alice@example.com", view.get().email());
    }

    @Test
    void shouldReturnEmptyViewWhenModelMissing() {
        assertTrue(repository.read("missing", UserContactView.class).isEmpty());
    }

    // --- Delete --------------------------------------------------------------

    @Test
    void shouldDeleteModel() {
        repository.save(alice());

        repository.delete("u1");

        assertTrue(repository.read("u1").isEmpty());
    }

    // --- No-op domain event bus (the base requires one) ----------------------

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
