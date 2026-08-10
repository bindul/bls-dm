# GitHub Copilot Instructions for bls-dm Project

## Project Overview

**Bowling League Stats - Data Manager (bls-dm)** is a comprehensive Java application for managing bowling league 
statistics data. The project uses modern Java technologies and follows clean architecture principles with clear 
separation of concerns across multiple modules.

- **Project Group**: `name.bindul.bls.dm`
- **Current Version**: `1.0.0-SNAPSHOT`
- **Java Version**: 25 (LTS-track, minimum JDK 23 for JavaFX 25)
- **Build Tool**: Maven 3.9+
- **Repository**: https://github.com/bindul/bls-dm

## Project Purpose

The application manages:
- Bowling league statistics and player records
- Data persistence via SQLite with Hibernate ORM
- Data export in multiple formats
- Desktop UI management via JavaFX (primary) and SWT
- Future extensibility for OCR, mobile clients, and other input technologies

## Architecture Overview

### Modular Structure

The project is organized as a Maven multi-module project with clear responsibility separation:

```
bls-dm (parent)
├── bls-dm-core         - Core domain models and business logic
├── bls-dm-store-sqlite - SQLite persistence layer (Hibernate)
├── bls-dm-export-site  - Data export functionality (JSON schemas)
├── bls-dm-ui-javafx    - Primary UI client (JavaFX)
├── bls-dm-ui-swt       - Alternative UI client (SWT)
└── bls-dm-app          - Application packaging & distribution
```

### Architectural Layers

1. **Model Layer** (`bls-dm-core/model`): Domain entities and value objects
2. **Business Logic Layer** (`bls-dm-core/impl`): Service implementations
3. **API Layer** (`bls-dm-core/api`): Service interfaces and contracts
4. **SPI Layer** (`bls-dm-core/spi`): Service Provider Interface for extensibility
5. **Persistence Layer** (`bls-dm-store-sqlite`): Hibernate/SQLite data access
6. **Export Layer** (`bls-dm-export-site`): Format conversions and export handlers
7. **UI Layers** (`bls-dm-ui-javafx`, `bls-dm-ui-swt`): Client applications

## Technology Stack

### Core Technologies

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 25 |
| **Build** | Maven | 3.9+ |
| **Dependency Injection** | No framework (manual/provider pattern) | - |
| **ORM** | Hibernate | 7.4.5.Final |
| **Database** | SQLite | 3.53.2.1 |
| **Migration** | Liquibase | 5.0.3 |
| **Testing** | JUnit 5 | 5.11.0 |

### Code Generation & Processing

| Library | Purpose | Version |
|---------|---------|---------|
| **Lombok** | Boilerplate reduction (@Getter, @Setter, @Data, etc.) | 1.18.46 |
| **MapStruct** | Type-safe bean mapping | 1.6.3 |
| **Auto-Service** | Service provider code generation | 1.1.1 |
| **Hibernate-Processor** | JPA metamodel generation | 7.4.5.Final |

### Serialization & Processing

- **Jackson** 2.22.1 - JSON processing
- **JSON Schema 2 POJO** - Schema-driven model generation
- **Commons Lang3** 3.20.0 - Utility functions

### Logging

- **Log4j 2** 2.26.1 - All Log4j components (api, core, jul bridge)

### UI Frameworks

- **JavaFX** 25.0.4 (LTS) - Primary desktop UI framework
  - **ControlsFX** 11.2.4 - Extended controls
  - **JavaFX Maven Plugin** 0.0.8
- **SWT/JFace** 3.134.0 / 3.39.100 - Alternative UI framework

### Data Specifications

- **Jakarta Data API** 1.0.1 - Standard data access specification

## Code Style & Conventions

### Naming Conventions

- **Package Naming**: Lowercase, hierarchical (e.g., `name.bindul.bls.dm.core.model`)
- **Class Naming**: PascalCase (e.g., `Frame`, `GameSession`, `PlayerStatistics`)
- **Interface Naming**: PascalCase without "I" prefix (e.g., `FrameCalculator`, not `IFrameCalculator`)
- **Method Naming**: camelCase (e.g., `calculateScore()`, `getPlayerName()`)
- **Constant Naming**: UPPER_SNAKE_CASE
- **Variable Naming**: camelCase, descriptive names

### Using Lombok

**Mandatory Usage**:
- Use `@Getter` and `@Setter` for entity properties
- Use `@Data` for value objects when appropriate (includes `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode`, `@RequiredArgsConstructor`)
- Use `@ToString`, `@EqualsAndHashCode` for complex entities
- Use `@RequiredArgsConstructor` for dependency injection (via constructor)
- Use `@Builder` for complex object construction

**Example**:
```java
@Data
@Entity
@Table(name = "frames")
public class Frame implements BlsEntity {
    @Id
    private Long id;
    
    @Column(nullable = false)
    private int frameNumber;
    
    @ManyToOne
    private Game game;
}
```

### Using MapStruct

**Conventions**:
- Create mapper interfaces annotated with `@Mapper(componentModel = "...")`
- Use method names like `toDto()`, `toDtoList()`, `toEntity()`, `toEntityList()`
- Leverage `@Mapping` annotations for complex mappings
- Place mappers in a dedicated `mapper` package within the module

**Example**:
```java
@Mapper(componentModel = "spring")
public interface PlayerMapper {
    PlayerDTO toDto(Player player);
    
    @Mapping(source = "stats.totalPins", target = "totalPinsScored")
    Player toEntity(PlayerDTO dto);
}
```

### Code Organization

- **One public class per file** (exceptions allowed for related nested classes)
- **Import statements**:
  - Group by standard library, third-party, then project imports
  - Never use wildcard imports (`import java.util.*;`), except for static imports
- **Encoding**: UTF-8 (configured in Maven)
- **Line Length**: 120 characters max (soft limit)
- **Indentation**: 4 spaces (no tabs)
- **Braces**: K&R style (opening brace on the same line)
- **Annotations**: Place annotations on the line above the element they annotate
- **Spaces in method signatures**: One space before parentheses in method calls and definitions

## Design Patterns & Principles

### Applied Patterns

1. **Service Provider Interface (SPI)** - Used in `bls-dm-core/spi` for extensibility
2. **Repository Pattern** - Data access abstraction via Hibernate
3. **Mapper Pattern** - Entity-to-DTO conversions via MapStruct
4. **Builder Pattern** - Complex object construction (via Lombok `@Builder`)
5. **Adapter Pattern** - Export format handlers

### SOLID Principles

- **Single Responsibility**: Each module has one reason to change
- **Open/Closed**: Open for extension (SPI) but closed for modification
- **Liskov Substitution**: Services implement consistent interfaces
- **Interface Segregation**: Focused service contracts in API layer
- **Dependency Inversion**: High-level modules depend on abstractions (API)

### Clean Architecture

- **Dependency Flow**: UI → Core API → Implementation → Persistence
- **No Cyclic Dependencies**: Parent modules don't depend on child modules
- **Testability**: Core business logic independent of frameworks
- **Layered Approach**: Clear separation of concerns between domain, persistence, and presentation
- **Extensibility**: SPI allows third-party extensions without modifying core logic
- **Null Safety**: Avoid returning null. Use `java.util.Optional` for methods that may not return a value.
- **Immutability**: Favor immutability. Use Java Records for Data Transfer Objects (DTOs). Mark local variables as `final` where applicable.
- **Resource Management**: Always use try-with-resources blocks for objects implementing `AutoCloseable`.
- **Exception Handling**: Avoid catching generic `Exception`. Catch specific exceptions and wrap them in custom runtime exceptions if rethrowing.
- **Concurrency**: Prefer `java.util.concurrent` utilities and Virtual Threads (if applicable) over raw threads.

## Module Responsibilities

### bls-dm-core

**Purpose**: Domain models, business logic, and service contracts

**Contents**:
- `model/` - JPA entities implementing `BlsEntity` interface
  - `Frame.java` - Represents a single bowling frame (1-2 rolls, or up to 3 in 10th frame)
  - Game, Player, Team, and related entities
- `api/` - Service interfaces (contracts)
- `impl/` - Service implementations (business logic)
- `spi/` - Service Provider Interfaces for third-party extensions

**Key Interfaces**:
- `BlsEntity` - Marker interface for all domain entities

**Rules**:
- No external framework coupling (except annotations)
- No database-specific code
- No UI dependencies

### bls-dm-store-sqlite

**Purpose**: SQLite persistence layer with Hibernate ORM

**Contents**:
- Hibernate entity mappings (extensions of core models)
- Repository implementations
- Database schema definition (DDL via Liquibase)
- Query builders and data access objects

**Technology**:
- SQLite JDBC 3.53.2.1
- Hibernate 7.4.5.Final
- Liquibase 5.0.3 for schema versioning

**Rules**:
- Database schema changes must use Liquibase migration scripts
- Entities should use standard JPA annotations
- Repositories should be generic and reusable

### bls-dm-export-site

**Purpose**: Data export functionality in multiple formats

**Contents**:
- JSON schema definitions
- Export handlers for different formats (CSV, JSON, etc.)
- Data transformation utilities
- Format-specific validators

**Technology**:
- Jackson for JSON processing
- JSON Schema 2 POJO for schema-driven models

**Rules**:
- Each format should have its own handler
- Export should not modify source data
- Schema definitions should be versioned

### bls-dm-ui-javafx

**Purpose**: Primary desktop user interface using JavaFX

**Contents**:
- FXML layout files
- Controller classes
- Custom controls and styling
- Application entry point

**Technology**:
- JavaFX 25.0.4 (LTS)
- ControlsFX 11.2.4
- Maven JavaFX Plugin 0.0.8

**Rules**:
- FXML files should be in `src/main/resources`
- Controllers should delegate business logic to services
- UI should be responsive (use Task/Service for long operations)
- CSS styling in separate stylesheets

### bls-dm-ui-swt

**Purpose**: Alternative SWT-based user interface

**Technology**:
- SWT 3.134.0
- JFace 3.39.100

**Note**: Currently available for alternative UI needs; primary development focuses on JavaFX.

### bls-dm-app

**Purpose**: Application packaging and distribution

**Contents**:
- Platform-specific packaging (Linux, macOS, Windows)
- Distribution artifacts
- Assembly configurations

**Modules**:
- `bls-dm-app-linux-x86_64/`
- `bls-dm-app-macosx-aarch64/`
- `bls-dm-app-windows-x86_64/`

## Common Tasks & How-To

### Adding a New Entity

1. Create the entity class in `bls-dm-core/src/main/java/name/bindul/bls/dm/core/model/`
2. Implement `BlsEntity` interface
3. Use Lombok annotations for getters, setters, and constructors
4. Add JPA annotations for persistence (`@Entity`, `@Table`, `@Column`, etc.)
5. Create repository in `bls-dm-store-sqlite` if persistence is needed
6. Add Liquibase migration if creating new database tables

**Example**:
```java
package name.bindul.bls.dm.core.model;

@Data
@Entity
@Table(name = "frames")
public class Frame implements BlsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer frameNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Game game;
}
```

### Adding a New Service

1. Create interface in `bls-dm-core/src/main/java/name/bindul/bls/dm/core/api/`
2. Create implementation in `bls-dm-core/src/main/java/name/bindul/bls/dm/core/impl/`
3. Use `@RequiredArgsConstructor` for dependency injection
4. Keep service methods focused and cohesive

### Creating Export Handlers

1. Implement handler in `bls-dm-export-site`
2. Create JSON schema if needed
3. Register handler for format type
4. Implement transformation logic
5. Add unit tests

### Building the Project

```bash
# Build all modules
mvn clean install

# Build specific module
mvn clean install -pl bls-dm-core

# Check for dependency updates
mvn clean install -P version-check

# Build without tests
mvn clean install -DskipTests
```

## Testing

### Test Organization

- Unit tests in `src/test/java/`
- Test resources in `src/test/resources/`
- Each module should have >80% code coverage for business logic

### Testing Frameworks

- **JUnit** - Standard test framework (version configured in parent)
- **Mockito** - Mocking framework (optional)
- **Hamcrest** - Assertion matchers (optional)
- **AssertJ** - Fluent assertions (optional)
- **Testcontainers** - For integration tests requiring database containers (optional)
- **Jacoco** - Code coverage reporting (configured in parent POM)

### Test Naming Conventions

- Test classes: `<ClassUnderTest>Test.java` (e.g., `FrameCalculatorTest`)
- Test methods: `test<Scenario>_<Expected>` (e.g., `testCalculateScore_StrikeReturnsThirty`)

### Test Coverage Guidelines

- **Framework**: Write unit tests using JUnit 5 and AssertJ for fluent assertions.
- **Mocking**: Use Mockito for isolating components.
- **Coverage**: Ensure code includes assertions for edge cases, null checks, and expected exceptions (`assertThrows`).

### Test Conventions

- **Integration Tests**
  - Use `@Tag("integration")` for tests that require database access or external resources
  - Name integration test classes with `*IT.java` suffix (e.g., `FrameRepositoryIT.java`)
- **Unit Tests**
  - Use `@Tag("unit")` for pure unit tests
  - Name unit test classes with `*Test.java` suffix (e.g., `FrameCalculatorTest.java`)
- **Test Resources**
  - Place test data files (JSON, CSV, etc.) in `src/test/resources/`
  - Use `@TempDir` for temporary file creation during tests
- **Test Data Management**
  - Use `@BeforeEach` and `@AfterEach` for setup and teardown
  - Use in-memory databases (H2) for unit tests if possible
  - Use Liquibase migrations to set up test database schema
- **Test Isolation**
  - Each test should be independent and not rely on shared state
  - Use mocks for external dependencies
  - Avoid static state in tests
- **Parameterized Tests**
  - Use `@ParameterizedTest` for testing multiple input scenarios
  - Use `@CsvSource`, `@MethodSource`, or `@ValueSource` for parameterized inputs

### Test Execution

- Use Maven Surefire plugin for unit tests
- Use Maven Failsafe plugin for integration tests

## Important Directories & Files

```
/data/prj/bls/git/bls-dm/
├── .copilot-instructions.md       ← This file
├── pom.xml                         ← Parent POM with dependency management
├── README.md                       ← Project overview
├── LICENSE                         ← Apache License 2.0
├── bls-dm-core/                    ← Core domain & services
│   ├── src/main/java/name/bindul/bls/dm/core/
│   │   ├── model/                  ← Domain entities
│   │   ├── api/                    ← Service interfaces
│   │   ├── impl/                   ← Service implementations
│   │   └── spi/                    ← Service Provider Interfaces
│   └── src/test/java/              ← Unit tests
├── bls-dm-store-sqlite/            ← SQLite persistence
│   ├── src/main/java/name/bindul/bls/dm/store/
│   ├── src/main/resources/
│   │   └── db/                     ← Liquibase migrations
│   └── src/test/                   ← Integration tests
├── bls-dm-export-site/             ← Export functionality
│   ├── src/main/java/
│   ├── src/main/resources/
│   │   └── json-schema/            ← JSON schema definitions
│   └── src/test/
├── bls-dm-ui-javafx/               ← JavaFX UI
│   ├── src/main/java/
│   ├── src/main/resources/         ← FXML files
│   └── src/test/
├── bls-dm-ui-swt/                  ← SWT UI (alternative)
├── bls-dm-app/                     ← Application packaging
└── .git/                           ← Git repository
```

## Key Code Examples

### Bowling Game Scoring Concepts

**Frame Structure**:
- Frames 1-9: 1 roll (open frame) or 2 rolls (spare or regular)
- Frame 10: 2 rolls (open frame) or 3 rolls (strike/spare)
- Strike: All 10 pins knocked down on first roll
- Spare: All 10 pins knocked down in two rolls
- Open Frame: Some pins remain standing after two rolls

**Scoring**:
- Strike: 10 + next 2 rolls
- Spare: 10 + next 1 roll
- Open: Sum of pins knocked down

### Entity Relationships

```
Game
├── Player
├── Frame (multiple per game)
│   ├── Roll (1-2 per frame, 3 in 10th if strike/spare)
│   └── Score (calculated based on rolls and subsequent frames)
├── GameSession
└── LeagueStatistics
```

## Performance Considerations

- Use lazy loading (`fetch = FetchType.LAZY`) for relationships
- Implement pagination for large result sets
- Cache frequently accessed data (e.g., league standings)
- Use batch operations for bulk inserts/updates
- Monitor SQLite query performance

## Security Considerations

- Validate all user inputs
- Use parameterized queries (Hibernate handles this)
- Escape data in exports (especially CSV)
- Enforce authorization checks in service layer
- Log security-relevant events

## Future Enhancements & Extensibility

### Planned Features

- **OCR Integration**: Document scanning for data entry
- **Mobile Clients**: Web/mobile applications consuming REST API
- **Analytics Engine**: Advanced statistics and trend analysis
- **Real-time Updates**: WebSocket support for live scoring
- **Multi-language Support**: Internationalization (i18n)
- **Cloud Integration**: Optional cloud backup and sync

### Extensibility Points

1. **SPI in bls-dm-core**: Implement service provider interfaces for custom logic
2. **Export Handlers**: Add new format handlers in bls-dm-export-site
3. **UI Modules**: Create new UI implementations following the javafx/swt patterns
4. **Data Input Plugins**: OCR and other input technologies can be plugged in

### Creating Custom Implementations

Use the SPI pattern:
```java
// Define SPI interface in bls-dm-core
package name.bindul.bls.dm.core.spi;

public interface ScoreCalculator {
    int calculateScore(List<Roll> rolls, int frameNumber);
}

// Implement in your module
package com.example.custom;

public class CustomScoreCalculator implements ScoreCalculator {
    // Implementation
}

// Register using Auto-Service
@AutoService(ScoreCalculator.class)
public class CustomScoreCalculatorProvider extends CustomScoreCalculator {
}
```

## Development Environment Setup

### Prerequisites

- JDK 25 (or 23+)
- Maven 3.9+
- Git
- IDE: IntelliJ IDEA, Eclipse, or VS Code with Java extensions

### Initial Setup

```bash
git clone https://github.com/bindul/bls-dm.git
cd bls-dm
mvn clean install
```

### IDE Configuration

- **Eclipse M2E**: Configured via maven-lifecycle-mapping plugin
- **IntelliJ**: Uses Maven by default
- **VS Code**: Install Extension Pack for Java

### Debugging

- Enable remote debugging on port 5005
- Use IDE debuggers for step-through debugging
- Log4j configuration controls logging levels

## Documentation Standards

- **Code Comments**: Explain "why", not "what" (code shows what)
- **JavaDoc**: Provide complete Javadoc definitions for all public APIs, including `@param`, `@return`, and `@throws`.
- **Class Comments**: Include purpose and usage examples
- **README Files**: Each module should have a brief README in its root
- **Changelog**: Maintain a `CHANGELOG.md` for version history

## Continuous Integration

- Maven enforces JDK 25 via toolchains plugin
- All modules must build without warnings
- Tests must pass before merging
- Code coverage requirements for core modules

## License

All code is licensed under the Apache License 2.0.

```
Copyright (c) 2026. Bindul Bhowmik

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

## Quick Reference Commands

```bash
# Clean and build
mvn clean install

# Run specific test
mvn test -Dtest=FrameCalculatorTest

# Skip tests
mvn install -DskipTests

# Update dependencies
mvn versions:display-dependency-updates

# Build JavaFX app
mvn clean install -pl bls-dm-ui-javafx

# Check for vulnerabilities
mvn dependency-check:check

# Format code
mvn spotless:apply  # if configured

# Generate site documentation
mvn site:site
```

## Getting Help

- Check the README.md files in each module
- Review existing entities and services for patterns
- Use Git log to understand design decisions
- Consult JavaDoc for API contracts
- Create issues for feature requests and bugs

---

**Last Updated**: August 7, 2026
**Project Version**: 1.0.0-SNAPSHOT
**Java Version**: 25 (LTS)
