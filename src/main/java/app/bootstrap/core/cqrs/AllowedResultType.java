/*
 * IBM QSMO
 * Copyright (C) 2026 IBM
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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Opt-out marker for the G4c arch rule (commands must return {@code Void}, a primitive wrapper, or
 * an {@link app.bootstrap.core.ddd.Id} subtype). A command annotated with this type is exempt from
 * the rule's class set.
 *
 * <p>Use sparingly — the default is that commands return identifiers, not DTOs. Every annotation
 * site must explain itself via {@link #reason()}; the rationale travels with the code and surfaces
 * in code review.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AllowedResultType {

    /** Why this command's result type is exempt from the G4c shape rule. */
    @Nonnull
    String reason();
}
