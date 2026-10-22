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
import java.util.List;

/**
 * Opt-in listener for a batch of events in delivery order, not timestamp order.
 *
 * <p>The dispatcher must run batch effects and acknowledgment in one transaction. A failed batch
 * rolls back as a whole; a dispatcher may then retry events individually to isolate a failing
 * event. Consumers must handle duplicate delivery safely. This contract adds no dispatch or retry
 * runtime and does not change {@link IEventListener}.
 */
public interface IBatchEventListener {

    /** Applies the ordered batch; failure must propagate so the dispatcher can roll it back. */
    void handleEvents(@Nonnull List<IEvent> events) throws Exception;
}
