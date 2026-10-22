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

import jakarta.annotation.Nonnull;

/**
 * Opt-in processing of one durable event through multiple committed transactions.
 *
 * <p>The dispatcher starts at offset zero and supplies the last committed non-negative offset on
 * subsequent calls. Each step's effects and saved progress must commit atomically. The dispatcher
 * must reject a {@link InboxStepResult.Continue} that does not advance beyond the supplied offset
 * and roll back that step. {@link InboxStepResult.Complete} permits acknowledgment in the same
 * transaction as the final effects. A failed step resumes from the last committed offset.
 *
 * <p>The dispatcher owns durable progress, transaction boundaries, acknowledgment, and coordination
 * of concurrent deliveries. Consumers must tolerate duplicate delivery safely. Offsets must
 * describe a stable work order across retries; if work is derived from mutable data, the
 * adapter/consumer must preserve that order. No dispatcher, persistence, or scheduling is supplied
 * by core.
 */
public interface IResumableInboxConsumer {

    /** Whether this consumer handles the event; selection does not process it. */
    boolean supports(@Nonnull IEvent event);

    /** Processes one step from the supplied offset; failures propagate to roll back this step. */
    @Nonnull
    InboxStepResult handleNext(@Nonnull IEvent event, int nextOffset) throws Exception;
}
