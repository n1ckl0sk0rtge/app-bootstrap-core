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

import jakarta.annotation.Nonnull;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;

class ReadTransactionTest {
    @Test
    void shouldJoinCallerTransactionWithoutCompletingItOnSuccessOrFailure() throws Exception {
        final MemoryTransactions tx = new MemoryTransactions();
        final Transaction caller = tx.beginWrite(20);
        assertEquals(20, tx.execute(ReadMode.CURRENT_TRANSACTION, tx::read));
        final Exception failure = new Exception("original");
        assertSame(
                failure,
                assertThrows(
                        Exception.class,
                        () ->
                                tx.execute(
                                        ReadMode.CURRENT_TRANSACTION,
                                        () -> {
                                            throw failure;
                                        })));

        assertSame(caller, tx.ambient);
        assertEquals(0, tx.commits);
        assertEquals(0, tx.rollbacks);
        assertEquals(10, tx.committed);
        assertEquals(20, tx.read());
        tx.commitWrite();
        assertEquals(20, tx.committed);
    }

    @Test
    void shouldExcludePendingWritesAndKeepOneSnapshotAcrossCallback() throws Exception {
        final MemoryTransactions tx = new MemoryTransactions();
        final Transaction caller = tx.beginWrite(20);
        final int result =
                tx.execute(
                        ReadMode.SNAPSHOT,
                        () -> {
                            assertNotSame(caller, tx.ambient);
                            assertEquals(10, tx.read());
                            tx.committed =
                                    15; // Simulates another transaction committing between reads.
                            assertEquals(10, tx.execute(ReadMode.CURRENT_TRANSACTION, tx::read));
                            return tx.read();
                        });

        assertEquals(10, result);
        assertSame(caller, tx.ambient);
        assertEquals(20, tx.read());
        assertEquals(15, tx.committed);
        assertEquals(1, tx.commits); // Only the snapshot completed.
        assertEquals(0, tx.rollbacks);
    }

    @Test
    void shouldRestoreCallerAfterSnapshotFailureAndPermitCallerRollback() {
        final MemoryTransactions tx = new MemoryTransactions();
        final Transaction caller = tx.beginWrite(20);
        final Exception failure = new Exception("snapshot failed");

        assertSame(
                failure,
                assertThrows(
                        Exception.class,
                        () ->
                                tx.execute(
                                        ReadMode.SNAPSHOT,
                                        () -> {
                                            throw failure;
                                        })));
        assertSame(caller, tx.ambient);
        assertEquals(20, tx.read());
        assertEquals(0, tx.commits);
        assertEquals(1, tx.rollbacks); // Only the failed snapshot rolled back.
        tx.rollbackWrite();
        assertEquals(10, tx.read());
        assertEquals(2, tx.rollbacks);
    }

    @Test
    void shouldPreserveNullAndResultIdentityAndCompleteOwnedBoundaries() throws Exception {
        for (ReadMode mode : ReadMode.values()) {
            final MemoryTransactions tx = new MemoryTransactions();
            final Object result = new Object();
            assertSame(result, tx.execute(mode, () -> result));
            assertNull(tx.execute(mode, () -> null));
            assertNull(tx.ambient);
            assertEquals(2, tx.commits);
            assertEquals(0, tx.rollbacks);
        }
    }

    @Test
    void shouldPropagateOriginalCheckedAndUncheckedFailuresAndCleanUpOwnedBoundaries() {
        for (ReadMode mode : ReadMode.values()) {
            final MemoryTransactions tx = new MemoryTransactions();
            for (Exception failure :
                    new Exception[] {
                        new Exception("checked"), new IllegalStateException("unchecked")
                    }) {
                assertSame(
                        failure,
                        assertThrows(
                                Exception.class,
                                () ->
                                        tx.execute(
                                                mode,
                                                () -> {
                                                    throw failure;
                                                })));
                assertNull(tx.ambient);
            }
            assertEquals(0, tx.commits);
            assertEquals(2, tx.rollbacks);
        }
    }

    @Test
    void shouldRestoreSnapshotAfterNestedSnapshotAndThenRestoreCaller() throws Exception {
        final MemoryTransactions tx = new MemoryTransactions();
        final Transaction caller = tx.beginWrite(20);
        tx.execute(
                ReadMode.SNAPSHOT,
                () -> {
                    final Transaction outer = tx.ambient;
                    tx.committed = 15;
                    assertEquals(15, tx.execute(ReadMode.SNAPSHOT, tx::read));
                    assertSame(outer, tx.ambient);
                    assertEquals(10, tx.read());
                    return null;
                });
        assertSame(caller, tx.ambient);
        assertEquals(2, tx.commits);
    }

    private record Transaction(int visibleValue, boolean write) {}

    // Single-threaded teaching fixture, not a persistence adapter. Owned read boundaries capture
    // committed state once; an ambient write carries its own pending value.
    private static final class MemoryTransactions implements IReadTransaction {
        private int committed = 10;
        private Transaction ambient;
        private int commits;
        private int rollbacks;

        Transaction beginWrite(int value) {
            ambient = new Transaction(value, true);
            return ambient;
        }

        int read() {
            return ambient == null ? committed : ambient.visibleValue();
        }

        void commitWrite() {
            if (ambient == null || !ambient.write()) {
                throw new IllegalStateException("no write transaction");
            }
            committed = ambient.visibleValue();
            commits++;
            ambient = null;
        }

        void rollbackWrite() {
            rollbacks++;
            ambient = null;
        }

        @Override
        public <R> R execute(@Nonnull ReadMode mode, @Nonnull Callable<R> work) throws Exception {
            if (mode == ReadMode.CURRENT_TRANSACTION && ambient != null) {
                return work.call(); // Caller retains ownership, including on failure.
            }
            final Transaction suspended = ambient;
            ambient = new Transaction(committed, false);
            try {
                final R result = work.call();
                commits++;
                return result;
            } catch (Exception | Error failure) {
                rollbacks++;
                throw failure;
            } finally {
                ambient = suspended;
            }
        }
    }
}
