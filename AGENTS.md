# Repository Guidelines

## Project Structure & Module Organization

This Java 21 library provides Clean Architecture, CQRS, and DDD contracts. Production code lives in `src/main/java/app/bootstrap/core/`:

- `ddd/`: aggregates, entities, identifiers, repositories, and domain events.
- `cqrs/`: commands, queries, handlers, projections, and transaction contracts.
- `messaging/`: event buses, listeners, inboxes, and outboxes.

Tests mirror these packages under `src/test/java/` and include in-memory reference implementations. `docs/USAGE.md` explains the architecture and usage; `README.md` provides the overview. Build configuration lives in `pom.xml`, JVM options in `.mvn/jvm.config`, and CI workflows in `.github/workflows/`. Generated artifacts belong in `target/`.

## Build, Test, and Development Commands

Use JDK 21 and Maven 3.x. Run commands from the repository root; this checkout has no `mvnw` wrapper.

- `mvn compile`: compile production code and run validation.
- `mvn test`: compile and run the test suite.
- `mvn -Dtest=AggregateRootTest test`: run a specific test class.
- `mvn -B package --file pom.xml`: run the CI build and produce JARs.
- `mvn spotless:apply`: format Java sources and normalize imports and license headers.
- `mvn clean verify -Dgpg.skip=true`: perform a clean local verification without artifact signing.

Spotless **applies changes** during Maven's `validate` phase, followed by Checkstyle. Review the diff after builds. This repository is a library; there is no application startup command.

## Coding Style & Naming Conventions

Use Google Java Format's AOSP style with four-space indentation, enforced through Spotless. Follow lowercase package names, `UpperCamelCase` types, and `lowerCamelCase` members. Existing interfaces use an `I` prefix, such as `ICommandBus`. Preserve Jakarta nullability annotations and Apache license headers. Compilation runs Error Prone; NullAway is configured with `AnnotatedPackages=com.ibm`.

Keep infrastructure implementations outside the core contracts. Update Javadocs and `docs/USAGE.md` when public APIs change.

## Testing Guidelines

Use JUnit Jupiter 6 with JUnit assertions or AssertJ. Name new test classes `*Test` and use descriptive methods such as `shouldTrackUncommittedChanges`. Cover contract behavior, failure cases, and transaction or messaging boundaries with in-memory fixtures. No numerical coverage threshold is configured.

## Commit & Pull Request Guidelines

Follow the history's short, imperative subjects: `Add ...`, `Remove ...`, or `Parameterize ...`; documentation commits sometimes use `Docs:`. Keep commits focused. In pull requests, explain the behavior change, link relevant issues, report validation commands and results, and identify public API compatibility impacts. Include updated usage examples when contracts change.
