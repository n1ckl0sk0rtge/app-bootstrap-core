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
import java.util.OptionalLong;

/**
 * Stores event-stream staging boundaries under opaque bookmark keys.
 *
 * <p>Capturing a frontier must occur in the same transaction as staging the events it bookmarks,
 * including querying their assigned sequences with {@link IOutboxSeqQuery}. Sequences represent
 * staging order, not event timestamps. Storage adapters own retention, sequence semantics, and the
 * policy for repeated recordings of a key.
 *
 * <p>A frontier alone proves neither relay fan-out completion nor completion of downstream events
 * caused by its deliveries. Applications choose consumers and conditions establishing readiness;
 * {@link IConsumerWatermarkQuery} provides bounded delivery progress for that decision.
 */
public interface IFrontierStore {

    /** Records a staging sequence in the transaction that stages the bookmarked events. */
    void record(@Nonnull FrontierKey key, long sequence);

    /**
     * Returns the stored boundary, or empty if no bookmark is available under the retention policy.
     */
    @Nonnull
    OptionalLong read(@Nonnull FrontierKey key);
}
