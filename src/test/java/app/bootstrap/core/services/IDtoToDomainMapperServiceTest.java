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

import static org.junit.jupiter.api.Assertions.*;

import jakarta.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class IDtoToDomainMapperServiceTest {

    // Test DTO class
    static class UserDto {
        private final String id;
        private final String name;
        private final int age;

        public UserDto(String id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    // Test Domain class
    static class User {
        private final String id;
        private final String name;
        private final int age;

        public User(String id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    // Test mapper implementation
    static class UserMapper implements IDtoToDomainMapperService<UserDto, User> {
        @Nonnull
        @Override
        public User mapToDomain(@Nonnull UserDto dto) throws Exception {
            return new User(dto.getId(), dto.getName(), dto.getAge());
        }

        @Nonnull
        @Override
        public UserDto mapToDto(@Nonnull User domain) {
            return new UserDto(domain.getId(), domain.getName(), domain.getAge());
        }
    }

    @Test
    void shouldMapSingleDtoToDomain() throws Exception {
        // Arrange
        UserMapper mapper = new UserMapper();
        UserDto dto = new UserDto("1", "John Doe", 30);
        
        // Act
        User domain = mapper.mapToDomain(dto);
        
        // Assert
        assertEquals(dto.getId(), domain.getId());
        assertEquals(dto.getName(), domain.getName());
        assertEquals(dto.getAge(), domain.getAge());
    }

    @Test
    void shouldMapSingleDomainToDto() {
        // Arrange
        UserMapper mapper = new UserMapper();
        User domain = new User("1", "John Doe", 30);
        
        // Act
        UserDto dto = mapper.mapToDto(domain);
        
        // Assert
        assertEquals(domain.getId(), dto.getId());
        assertEquals(domain.getName(), dto.getName());
        assertEquals(domain.getAge(), dto.getAge());
    }

    @Test
    void shouldMapListOfDtosToDomains() throws Exception {
        // Arrange
        UserMapper mapper = new UserMapper();
        List<UserDto> dtos = Arrays.asList(
                new UserDto("1", "John Doe", 30),
                new UserDto("2", "Jane Smith", 25)
        );
        
        // Act
        List<User> domains = mapper.mapToDomain(dtos);
        
        // Assert
        assertEquals(dtos.size(), domains.size());
        for (int i = 0; i < dtos.size(); i++) {
            assertEquals(dtos.get(i).getId(), domains.get(i).getId());
            assertEquals(dtos.get(i).getName(), domains.get(i).getName());
            assertEquals(dtos.get(i).getAge(), domains.get(i).getAge());
        }
    }

    @Test
    void shouldMapListOfDomainsToDtos() {
        // Arrange
        UserMapper mapper = new UserMapper();
        List<User> domains = Arrays.asList(
                new User("1", "John Doe", 30),
                new User("2", "Jane Smith", 25)
        );
        
        // Act
        List<UserDto> dtos = mapper.mapToDto(domains);
        
        // Assert
        assertEquals(domains.size(), dtos.size());
        for (int i = 0; i < domains.size(); i++) {
            assertEquals(domains.get(i).getId(), dtos.get(i).getId());
            assertEquals(domains.get(i).getName(), dtos.get(i).getName());
            assertEquals(domains.get(i).getAge(), dtos.get(i).getAge());
        }
    }

    @Test
    void shouldHandleEmptyListOfDtos() throws Exception {
        // Arrange
        UserMapper mapper = new UserMapper();
        List<UserDto> emptyDtos = Arrays.asList();
        
        // Act
        List<User> domains = mapper.mapToDomain(emptyDtos);
        
        // Assert
        assertTrue(domains.isEmpty());
    }

    @Test
    void shouldHandleEmptyListOfDomains() {
        // Arrange
        UserMapper mapper = new UserMapper();
        List<User> emptyDomains = Arrays.asList();
        
        // Act
        List<UserDto> dtos = mapper.mapToDto(emptyDomains);
        
        // Assert
        assertTrue(dtos.isEmpty());
    }
}