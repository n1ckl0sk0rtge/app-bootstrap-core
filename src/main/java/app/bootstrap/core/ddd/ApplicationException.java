/*
 * App Bootstrap Core
 * Copyright (C) 2025
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
package app.bootstrap.core.ddd;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class ApplicationException extends Exception {
    @Nonnull protected final String errorCode;
    @Nullable protected final transient Object context;

    protected ApplicationException(@Nonnull String message, @Nonnull String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.context = null;
    }

    protected ApplicationException(
            @Nonnull String message, @Nonnull String errorCode, @Nonnull Object context) {
        super(message);
        this.errorCode = errorCode;
        this.context = context;
    }

    @Nonnull
    public String getErrorCode() {
        return errorCode;
    }

    @Nullable public Object getContext() {
        return context;
    }
}
