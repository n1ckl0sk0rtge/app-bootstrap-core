/*
 * App Bootstrap Core
 * Copyright (C) 2024
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

import jakarta.annotation.Nonnull;

/**
 * A read slice (subset of fields) that a query returns, keyed by {@code I}.
 *
 * <p>A view is a use-case-owned DTO: it carries only the fields a caller needs and is never the
 * persistence entity. One logical read model can have <em>many</em> views, each materializing a
 * different part of it. Because ports reference views (which the use-case layer owns) rather than
 * the read-model persistence type, the infrastructure entity never leaks across the layer boundary.
 *
 * <p>How a view is backed — sliced out of one stored record, read from its own table, or assembled
 * from several — is an infrastructure decision the use-case layer never sees.
 *
 * @param <I> the read model id type
 */
public interface IView<I> {

    @Nonnull
    I getId();
}
