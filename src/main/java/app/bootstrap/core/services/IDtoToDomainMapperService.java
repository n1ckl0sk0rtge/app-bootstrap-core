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
package app.bootstrap.core.services;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public interface IDtoToDomainMapperService<DTO, DOMAIN> {
    @Nonnull
    DOMAIN mapToDomain(@Nonnull DTO dto) throws Exception;

    @Nonnull
    DTO mapToDto(@Nonnull DOMAIN dto);

    @Nonnull
    default List<DOMAIN> mapToDomain(@Nonnull List<DTO> dtoList) throws Exception {
        final List<DOMAIN> domainList = new ArrayList<>();
        for (DTO dto : dtoList) {
            domainList.add(mapToDomain(dto));
        }
        return domainList;
    }

    @Nonnull
    default List<DTO> mapToDto(@Nonnull List<DOMAIN> dtoList) {
        final List<DTO> domainList = new ArrayList<>();
        for (DOMAIN dto : dtoList) {
            domainList.add(mapToDto(dto));
        }
        return domainList;
    }
}
