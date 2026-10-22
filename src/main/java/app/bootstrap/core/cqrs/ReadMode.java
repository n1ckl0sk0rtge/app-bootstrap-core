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

/**
 * Transaction visibility for one complete repository read operation.
 *
 * <p>Repositories adopting {@link IWithReadMode} default to {@link #SNAPSHOT}. The persistence
 * adapter implements these semantics; core does not execute transactions or depend on a database or
 * transaction framework. Neither mode waits for projections to catch up.
 */
public enum ReadMode {
    /**
     * Reads committed data consistently for one complete operation, excluding pending writes in an
     * ambient transaction. SQL adapters can use a separate read-only {@code REPEATABLE READ}
     * transaction, suspending the ambient transaction and restoring it afterward.
     */
    SNAPSHOT,

    /**
     * Joins the existing transaction and sees its pending writes. Without an existing transaction,
     * opens an ordinary read transaction for the operation.
     */
    CURRENT_TRANSACTION
}
