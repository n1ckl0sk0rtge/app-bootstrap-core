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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class ReadModeTest {

    @Test
    void shouldPreserveConsumerSpecificMethodsAfterModeSelection() {
        final ProjectId projectId = new ProjectId("p1");
        final AssetView asset = new AssetView(new AssetId("a1"), projectId);
        final AssetView other = new AssetView(new AssetId("a2"), new ProjectId("p2"));
        final List<ReadMode> observedModes = new ArrayList<>();
        final IAssetReadRepository assets =
                new TestAssetReadRepository(
                        mode -> {
                            observedModes.add(mode);
                            return List.of(asset, other);
                        });

        assertEquals(
                List.of(asset), assets.withReadMode(ReadMode.CURRENT_TRANSACTION).list(projectId));
        assertEquals(
                Optional.of(asset),
                assets.withReadMode(ReadMode.CURRENT_TRANSACTION)
                        .read(asset.getId(), AssetView.class));
        assertEquals(
                List.of(ReadMode.CURRENT_TRANSACTION, ReadMode.CURRENT_TRANSACTION), observedModes);
    }

    @Test
    void shouldDefaultToSnapshotAndSelectModesWithoutChangingExistingInstancesOrReading() {
        final List<ReadMode> observedModes = new ArrayList<>();
        final IAssetReadRepository assets =
                new TestAssetReadRepository(
                        mode -> {
                            observedModes.add(mode);
                            return List.of();
                        });

        final IAssetReadRepository current = assets.withReadMode(ReadMode.CURRENT_TRANSACTION);
        final IAssetReadRepository snapshot = current.withReadMode(ReadMode.SNAPSHOT);

        assertNotSame(assets, current);
        assertNotSame(current, snapshot);
        assertTrue(observedModes.isEmpty());

        final ProjectId projectId = new ProjectId("p1");
        assets.list(projectId);
        current.list(projectId);
        snapshot.list(projectId);
        current.list(projectId);

        // All instances still use the same dependency, each forwarding its own mode.
        assertEquals(
                List.of(
                        ReadMode.SNAPSHOT,
                        ReadMode.CURRENT_TRANSACTION,
                        ReadMode.SNAPSHOT,
                        ReadMode.CURRENT_TRANSACTION),
                observedModes);
    }

    record AssetId(String value) {}

    record ProjectId(String value) {}

    record AssetView(AssetId getId, ProjectId projectId) implements IView<AssetId> {}

    interface IAssetReadRepository
            extends IReadRepository<AssetId>, IWithReadMode<IAssetReadRepository> {
        @Nonnull
        List<AssetView> list(@Nonnull ProjectId projectId);
    }

    // Tests fluent typing and mode forwarding; transaction semantics belong to real adapters.
    static final class TestAssetReadRepository implements IAssetReadRepository {
        private final Function<ReadMode, List<AssetView>> reads;
        private final ReadMode mode;

        TestAssetReadRepository(Function<ReadMode, List<AssetView>> reads) {
            this(reads, ReadMode.SNAPSHOT);
        }

        private TestAssetReadRepository(Function<ReadMode, List<AssetView>> reads, ReadMode mode) {
            this.reads = reads;
            this.mode = Objects.requireNonNull(mode);
        }

        @Override
        @Nonnull
        public IAssetReadRepository withReadMode(@Nonnull ReadMode mode) {
            return new TestAssetReadRepository(reads, mode);
        }

        @Override
        @Nonnull
        public List<AssetView> list(@Nonnull ProjectId projectId) {
            return reads.apply(mode).stream()
                    .filter(asset -> asset.projectId().equals(projectId))
                    .toList();
        }

        @Override
        @Nonnull
        public <V extends IView<AssetId>> Optional<V> read(
                @Nonnull AssetId id, @Nonnull Class<V> view) {
            return reads.apply(mode).stream()
                    .filter(asset -> asset.getId().equals(id))
                    .findFirst()
                    .map(view::cast);
        }
    }
}
