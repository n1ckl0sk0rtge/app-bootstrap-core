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

/**
 * Counts of relevant deliveries through a boundary. The counts are disjoint: unprocessed includes
 * pending, retryable, and partially completed resumable deliveries; failed counts terminal
 * failures. Both must be zero for those deliveries to have drained successfully.
 *
 * <p>This value does not prove relay fan-out completion or completion of downstream caused events.
 *
 * @param unprocessed unfinished deliveries, excluding terminal failures
 * @param failed terminally failed deliveries
 */
public record DrainState(long unprocessed, long failed) {
    public DrainState {
        if (unprocessed < 0 || failed < 0) {
            throw new IllegalArgumentException("delivery counts must be non-negative");
        }
    }
}
