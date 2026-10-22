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
import java.util.Objects;

/** Pure slicing of an ordered, stable work list for {@link IResumableInboxConsumer}. */
public final class WorkSlices {
    private WorkSlices() {}

    /**
     * Copies at most {@code limit} items starting at {@code nextOffset}, preserving order.
     *
     * <p>The result is Complete when the end is reached, including an empty list or an offset equal
     * to the size. Otherwise Continue points immediately after the slice. End computation is safe
     * even for {@link Integer#MAX_VALUE} limits. The input must not change during this call.
     *
     * @param items the work list, with no null elements in the selected slice
     * @param nextOffset an offset from zero through the list size, inclusive
     * @param limit an explicit positive maximum slice size
     * @param <T> the item type
     * @return an immutable copy of the slice and its step result
     * @throws IllegalArgumentException if the offset is outside the list or the limit is not
     *     positive
     * @throws NullPointerException if the list or a selected element is null
     */
    @Nonnull
    public static <T> Slice<T> slice(@Nonnull List<T> items, int nextOffset, int limit) {
        Objects.requireNonNull(items, "items");
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        if (nextOffset < 0 || nextOffset > items.size()) {
            throw new IllegalArgumentException("offset must be between zero and list size");
        }
        final int end = nextOffset + Math.min(limit, items.size() - nextOffset);
        final InboxStepResult result =
                end == items.size()
                        ? new InboxStepResult.Complete()
                        : new InboxStepResult.Continue(end);
        return new Slice<>(items.subList(nextOffset, end), result);
    }

    /** Immutable list structure and step result; the items themselves are not deep-copied. */
    public record Slice<T>(@Nonnull List<T> items, @Nonnull InboxStepResult result) {
        public Slice {
            items = List.copyOf(items);
            Objects.requireNonNull(result, "result");
        }
    }
}
