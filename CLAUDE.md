# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Book Management System Backend API** built with Kotlin, Spring Boot, jOOQ, and PostgreSQL. It implements a RESTful API following layered architecture with dependency inversion principle (DIP). The system manages many-to-many relationships between books and authors.

**Package Name**: `com.bookmanageapp.bookmanagementapi`

## Technology Stack

- **Language**: Kotlin
- **Java Version**: 17 (Amazon Corretto)
- **Framework**: Spring Boot 3.5.5
- **Database**: PostgreSQL (Docker Compose for local / Supabase for production)
- **ORM**: jOOQ 3.19.8 (type-safe SQL generation)
- **Migration**: Flyway 11.11.2
- **Testing**: JUnit 5, MockK, Testcontainers
- **Validation**: Bean Validation with custom validators
- **Container**: Docker, Docker Compose
- **Build Tool**: Gradle (Groovy)
- **Code Quality**: ktlint

## Development Setup Commands

This project is **fully implemented and working**. To get started:

### Local Development with Docker
1. **Database Setup**: `docker-compose up -d` (Start local PostgreSQL)
2. **Build**: `./gradlew build` (Includes jOOQ code generation)
3. **Run**: `./gradlew bootRun --spring.docker.compose.enabled=true`
4. **Test**: `./gradlew test` (Includes Testcontainers integration tests)

### Using Supabase (Production)
1. **Build**: `./gradlew build`
2. **Run**: `./gradlew bootRun` (Uses Supabase connection in application.properties)

### Additional Commands
- **jOOQ Code Generation**: `./gradlew jooqCodegen` (Auto-runs with build)
- **Database Migration**: `./gradlew flywayMigrate`
- **Code Formatting**: `./gradlew ktlintFormat`
- **Lint Check**: `./gradlew ktlintCheck`

## Architecture

### Layer Structure
```
┌─────────────────┐
│   Controller    │ ← REST API endpoints
├─────────────────┤
│    Service      │ ← Business logic & validation (concrete classes)
├─────────────────┤
│   Repository    │ ← Data access abstraction (interface + implementation)
├─────────────────┤
│     jOOQ        │ ← Type-safe SQL execution
├─────────────────┤
│  PostgreSQL     │ ← Data persistence
└─────────────────┘
```

### Dependency Inversion Principle (DIP)
- **Repository Layer**: Interfaces are used to abstract data access implementation
- **Service Layer**: Concrete classes (no interfaces) to avoid over-abstraction
- **Focus**: DIP is applied where it matters most - the data access layer

### Core Domain Models
- **Book**: Contains id, title, price, currency code, publication status, and list of author IDs
  - **NewBook**: For creation (without ID)
  - **Book**: For persisted entities (with ID)
- **Author**: Contains id, name, and birth date
  - **NewAuthor**: For creation (without ID)
  - **Author**: For persisted entities (with ID)
- **PublicationStatus**: Enum (UNPUBLISHED="00", PUBLISHED="01") with transition validation

### Key Business Rules
- Books must have at least one author
- Price must be >= 0 (enforced by database constraint)
- Publication status cannot change from PUBLISHED to UNPUBLISHED
- Author birth date must be in the past (validated against database time)
- Currency code support (ISO 4217 format)
- Optimistic locking with `lock_no` field

## Database Schema

- **m_books**: id, title, price, currency_code, publication_status, lock_no, created_at, updated_at, created_by, updated_by
- **m_authors**: id, name, birth_date, lock_no, created_at, updated_at, created_by, updated_by
- **t_book_authors**: book_id, author_id (many-to-many relationship table)

## API Endpoints

### Books (Implemented)
- `POST /api/books` - Create book (returns 201 with book ID)
- `PUT /api/books/{id}` - Update book (returns 204)

### Authors (Implemented)
- `POST /api/authors` - Create author (returns 201 with author ID)
- `PUT /api/authors/{id}` - Update author (returns 204)
- `GET /api/authors/{id}/books` - Get author's books

## Development Approach

This project follows **Test-Driven Development (TDD)** based on t_wada's approach:

1. Write failing test first
2. Write minimal code to pass test
3. Refactor while keeping tests green

### Test Strategy (Implemented)
- **Unit Tests**: Service layer with MockK for mocking dependencies
- **Integration Tests**: Repository layer and Controller layer with Testcontainers for real PostgreSQL
- **Test Structure**: AAA pattern (Arrange-Act-Assert) with Given/When/Then comments
- **Custom Validator Tests**: Birth date and publication status validation
- **Test Profiles**: Separate application-test.properties and application-test-testcontainers.properties

## Key Dependencies

### jOOQ Configuration (Implemented)
- jOOQ gradle plugin 3.19.8 for code generation
- KotlinGenerator for Kotlin-optimized code
- Generated POJOs as Kotlin data classes
- Code generation depends on Flyway migration
- Output directory: `src/main/generated`
- Package: `com.bookmanageapp.jooq`

### Flyway Migration (Implemented)
- Schema versioning in `src/main/resources/db/migration`
- V1_0_0_20250920000001__create_books.sql
- V1_0_0_20250920000002__create_authors.sql
- V1_0_0_20250920000003__create_book_authors.sql
- Baseline-on-migrate enabled
- PostgreSQL-specific SQL with CHECK constraints and comments

## Error Handling

### Exception Handling Strategy
- **Repository Layer**: Does NOT catch exceptions - lets database and jOOQ exceptions propagate naturally to the service layer
- **Service Layer**: Handles and transforms exceptions as needed for business logic
- **Controller Layer**: Global exception handler provides consistent HTTP error responses

### Custom Exception Hierarchy (Implemented)
- `BookManagementException` (base sealed class)
- `NotFoundException` (for book/author not found)
- `InvalidRequestException` (for business rule violations)
- `OptimisticLockException` (for concurrent modification)
- `GlobalExceptionHandler` with proper HTTP status mapping

### Exception Flow
```
Repository Layer → Service Layer → Controller Layer → Global Exception Handler
   (propagate)      (handle/transform)    (HTTP response)
```

## Implementation Status

**✅ FULLY IMPLEMENTED**: This codebase contains a complete, working Book Management API with:

### Implemented Features
- ✅ Complete domain models with separation of New/Persisted entities
- ✅ Repository interfaces with jOOQ-based implementations
- ✅ Service layer with business logic and validation
- ✅ REST Controllers with proper HTTP status codes
- ✅ Database schema with Flyway migrations
- ✅ jOOQ code generation setup
- ✅ Custom validation for birth dates and publication status
- ✅ Optimistic locking implementation
- ✅ Global exception handling
- ✅ Integration tests with Testcontainers
- ✅ Currency code support
- ✅ Database time validation (not application time)

### Key Implementation Details
- Database validation uses `SELECT CURRENT_DATE` for birth date checking
- Publication status transitions are enforced in domain logic
- Duplicate author IDs are automatically deduplicated
- Generated jOOQ code excludes flyway_schema_history table

## Configuration Files (Implemented)

- `application.properties`: Database connection (Supabase), Flyway, jOOQ, Jackson settings
- `docker-compose.yml`: Local PostgreSQL container (postgres:15)
- `build.gradle`: Complete dependencies, jOOQ plugin, Flyway configuration
- `application-test.properties`: Test database configuration
- `application-test-testcontainers.properties`: Testcontainers specific settings

### Database Connection Options
1. **Local Development**: Docker Compose PostgreSQL (localhost:5432)
2. **Production**: Supabase PostgreSQL (configured in application.properties)
3. **Testing**: Testcontainers with real PostgreSQL instances

### Custom Validation Features
- `@ValidBirthDate`: Validates birth date against database current date
- `@ValidPublicationStatusCode`: Validates publication status enum codes
- `TimeProvider` interface with `DatabaseTimeProvider` implementation