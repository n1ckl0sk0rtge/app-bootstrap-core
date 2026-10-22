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
 * Queries a consumer's delivery progress through an inclusive event-stream staging boundary.
 *
 * <p>The adapter must account for every relevant delivery through the boundary, including gaps,
 * retries, unfinished resumable steps, and terminal failures. A maximum processed sequence is
 * insufficient: later deliveries may finish before earlier ones. The boundary must use the same
 * sequence semantics as {@link IOutboxSeqQuery}, not an unrelated inbox receive sequence. Storage
 * adapters own sequence and retention semantics and must preserve evidence needed for these counts.
 *
 * <p>These counts alone do not prove that relay fan-out has completed or downstream events caused
 * by these deliveries have finished. Applications choose which consumers and additional conditions
 * establish readiness.
 */
public interface IConsumerWatermarkQuery {

    /**
     * Returns unfinished and terminally failed delivery counts at or below {@code throughSequence}.
     */
    @Nonnull
    DrainState drainState(@Nonnull String consumer, long throughSequence);
}
