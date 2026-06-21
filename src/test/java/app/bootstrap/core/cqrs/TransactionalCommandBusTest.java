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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Shows the unit-of-work decorator pattern: every synchronous command runs inside an {@link
 * IUnitOfWork}, so its enlisted writes commit together on success and are discarded on failure,
 * without the handler opening a transaction itself.
 */
class TransactionalCommandBusTest {

    private record DoWork(String value) implements ICommand {}

    private record Compute(String value) implements IResultCommand<String> {}

    private record Fail() implements IResultCommand<String> {}

    private record Ping(String tag) implements ICommand {}

    private SimpleICommandBus delegate;
    private InMemoryUnitOfWork unitOfWork;
    private TransactionalCommandBus bus;

    /** A read store written to only when a unit of work commits. */
    private List<String> store;

    /** Records async handler runs (mutated on a worker thread). */
    private List<String> asyncLog;

    @BeforeEach
    void setUp() {
        delegate = new SimpleICommandBus();
        unitOfWork = new InMemoryUnitOfWork();
        bus = new TransactionalCommandBus(delegate, unitOfWork);
        store = new ArrayList<>();
        asyncLog = new CopyOnWriteArrayList<>();
    }

    @Test
    void shouldCommitFireAndForgetCommandInAUnitOfWork() throws Exception {
        bus.register(
                (ICommandHandler)
                        command -> {
                            DoWork cmd = (DoWork) command;
                            // onCommit() stands in for a real repository auto-enlisting its
                            // write in the ambient transaction (e.g. JPA save() joining the
                            // EntityManager). A production handler never touches the unit of
                            // work: the decorator opens the txn and the datasource enlists the
                            // SQL. The in-memory stand-in can't auto-enlist, so we do it here.
                            unitOfWork.onCommit(() -> store.add(cmd.value()));
                        },
                DoWork.class);

        Boolean ok = bus.sendSync(new DoWork("a"));

        assertTrue(ok);
        assertEquals(List.of("a"), store, "enlisted write applied on commit");
        assertEquals(1, unitOfWork.commits());
        assertEquals(0, unitOfWork.rollbacks());
    }

    @Test
    void shouldCommitResultCommandAndReturnItsResult() throws Exception {
        IResultCommandHandler<Compute, String> handler =
                cmd -> {
                    unitOfWork.onCommit(() -> store.add("computed:" + cmd.value()));
                    return cmd.value().toUpperCase(Locale.ROOT);
                };
        bus.register(handler, Compute.class);

        String result = bus.sendSync(new Compute("x"));

        assertEquals("X", result, "result flows back through the unit of work");
        assertEquals(List.of("computed:x"), store);
        assertEquals(1, unitOfWork.commits());
        assertEquals(0, unitOfWork.rollbacks());
    }

    @Test
    void shouldRollBackWhenHandlerThrows() {
        IResultCommandHandler<Fail, String> handler =
                cmd -> {
                    unitOfWork.onCommit(() -> store.add("should-not-persist"));
                    throw new IllegalStateException("boom");
                };
        bus.register(handler, Fail.class);

        assertThrows(IllegalStateException.class, () -> bus.sendSync(new Fail()));

        assertTrue(store.isEmpty(), "enlisted write discarded on rollback");
        assertEquals(0, unitOfWork.commits());
        assertEquals(1, unitOfWork.rollbacks());
    }

    @Test
    void shouldForwardAsyncSendWithoutWrappingInAUnitOfWork() throws Exception {
        bus.register((ICommandHandler) command -> asyncLog.add(((Ping) command).tag()), Ping.class);

        Boolean ok = bus.send(new Ping("p")).get();

        assertTrue(ok, "async command dispatched through the delegate");
        assertEquals(List.of("p"), asyncLog);
        assertEquals(
                0,
                unitOfWork.commits(),
                "async send is not wrapped — the UoW belongs on the worker thread");
        assertEquals(0, unitOfWork.rollbacks());
    }
}
