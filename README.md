# app-bootstrap-core

A lightweight Java framework enabling Clean Architecture, CQRS, and DDD tactical patterns.

---

## Overview

**app-bootstrap-core** is a modular Java framework designed to help developers rapidly bootstrap 
applications following modern architectural principles:
- **Clean Architecture**
- **CQRS (Command Query Responsibility Segregation)**
- **DDD (Domain-Driven Design) Tactical Patterns**

With a focus on simplicity, extensibility, and testability, this framework provides the
essential building blocks for scalable, maintainable Java applications.

---

## Features

- **Clean Architecture**: Enforces separation of concerns and independence of frameworks.
- **CQRS Support**: Easily separate read and write operations for better scalability and clarity.
- **DDD Tactical Patterns**: Built-in support for aggregates, repositories, value objects, and more.
- **Lightweight & Modular**: Minimal dependencies, easy to integrate into existing projects.
- **Testability**: Encourages writing testable, decoupled code.

---

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.x

### Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.n1ckl0sk0rtge</groupId>
    <artifactId>app-bootstrap-core</artifactId>
    <version>0.1.18</version>
</dependency>

```

### Basic Usage

1. **Define Your Domain Model**  
   Use DDD tactical patterns to define Entities, Value Objects, and Aggregates.

2. **Implement CQRS Handlers**  
   Separate your command and query logic using the framework's abstractions.

3. **Configure Application Layers**  
   Organize your codebase into domain, application, and infrastructure layers.
