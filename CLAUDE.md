# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LUNARi is a microservices-based platform for empowering e-commerce entrepreneurs. The system consists of three Spring Boot microservices that communicate via REST APIs:

- **usuario** (User Service) - Port 8081 - Manages users, roles, authentication with full HATEOAS implementation
- **inventario** (Inventory Service) - Port 8081 - Manages services catalog and resources
- **carrito** (Cart Service) - Port 8082 - Manages shopping carts with inter-service communication

## Common Commands

### Running Services

Each microservice must be run in a separate terminal:

```bash
# Terminal 1 - User Service
cd usuario/
mvn spring-boot:run

# Terminal 2 - Inventory Service
cd inventario/
mvn spring-boot:run

# Terminal 3 - Cart Service
cd carrito/
mvn spring-boot:run
```

### Testing

```bash
# Run all tests for a microservice
cd usuario/
mvn test

# Run specific test class
mvn test -Dtest=UserServiceImplTest

# Run with coverage report
mvn clean test jacoco:report

# Run tests with H2 database (automatically used via test profile)
mvn test -Dspring.profiles.active=test
```

### Building

```bash
# Build without tests
mvn clean package -DskipTests

# Build with tests
mvn clean package

# Clean build artifacts
mvn clean
```

### Database Setup

1. Execute `script_creacion_tablas.sql` to create database schema
2. Optionally load seed data from `seeds/` directory (user_seed.sql, inventory_seed.sql, cart_seed.sql)
3. Configure `.env` files in each microservice's `src/main/resources/` directory with:
   - DB_HOST
   - DB_PORT
   - DB_NAME
   - DB_USER
   - DB_PASSWORD

## Architecture

### Layered Architecture

Each microservice follows standard Spring Boot layered architecture:

```
Controller → Service → Repository → Database
     ↓          ↓
    DTO    →  Model (JPA Entity)
     ↓
 Assembler (HATEOAS, usuario only)
```

- **Controllers**: REST endpoints, request/response handling
- **Services**: Business logic and orchestration
- **Repositories**: JPA data access using Spring Data JPA
- **Models**: JPA entities with database mappings
- **DTOs**: Data transfer objects for external API communication
- **Assemblers**: HATEOAS link generation (usuario microservice only)

### Inter-Service Communication

The **carrito** service communicates with other services using RestTemplate clients:

- `UsuarioServiceClient` - Validates user existence via `http://localhost:8081/api/v1/users/{id}`
- `InventarioServiceClient` - Validates inventory items

These clients are located in `carrito/src/main/java/cl/duoc/lunari/api/cart/service/client/` and require the target services to be running.

### Configuration Management

- **Environment variables**: Loaded via DotEnv from `.env` files in `src/main/resources/`
- **Spring profiles**: `application.properties` for main config, `application-test.properties` for testing
- **Database**: PostgreSQL in production, H2 in-memory for tests

### HATEOAS Implementation (Usuario Only)

The usuario microservice implements full HATEOAS with dynamic links:

- `UserModelAssembler` - Converts User entities to hypermedia representations
- `PagedUserModelAssembler` - Handles paginated responses with navigation links
- Links are conditional based on user state (active/inactive, verified/unverified)
- Links include: self, all, activate, deactivate, verify, company-users, role-users

Example links in responses:
```json
{
  "id": "uuid",
  "nombre": "User Name",
  "_links": {
    "self": { "href": "/api/v1/users/{id}" },
    "activate": { "href": "/api/v1/users/{id}/activate" },
    "verify": { "href": "/api/v1/users/{id}/verify" }
  }
}
```

## API Documentation

Each microservice exposes Swagger UI and OpenAPI documentation:

- **Usuario**: http://localhost:8081/swagger-ui/index.html
- **Inventario**: http://localhost:8081/swagger-ui/index.html
- **Carrito**: http://localhost:8082/swagger-ui/index.html

OpenAPI JSON specs available at `/v3/api-docs` endpoint for each service.

## Testing Strategy

### Unit Testing Stack

- **JUnit 5**: Test framework
- **Mockito**: Mocking dependencies in service/controller tests
- **H2 Database**: In-memory database for repository tests
- **MockMvc**: Controller testing without running web server
- **Spring Boot Test**: Integration testing support

### Test Coverage (Usuario Service)

The usuario service has comprehensive test coverage:

- `UserRepositoryTest` - JPA repository methods with H2
- `UserTest` - Entity validation and business logic
- `UserServiceImplTest` - Service layer with mocked repositories
- `UserControllerTest` - REST endpoints with MockMvc
- `UserModelAssemblerTest` - HATEOAS link generation

### Running Tests

Tests automatically use H2 in-memory database via `@DataJpaTest` or test profile configuration. No manual database setup needed for tests.

## Key Package Structure

```
src/main/java/cl/duoc/lunari/api/{service}/
├── config/          # Spring configuration (DotEnv, OpenAPI, RestTemplate)
├── controller/      # REST controllers with @RestController
├── service/         # Business logic implementations
│   └── client/      # Inter-service REST clients (carrito only)
├── repository/      # Spring Data JPA repositories
├── model/           # JPA entities with @Entity
├── dto/             # Data transfer objects
└── assembler/       # HATEOAS assemblers (usuario only)
```

## Development Workflow

1. Work on `development` branch, create PRs to `main`
2. Each microservice is independent but follows same architectural patterns
3. Usuario service on port 8081, Inventario on 8081 (note: README shows same port, verify actual config)
4. Carrito service depends on both Usuario and Inventario being available
5. All services use PostgreSQL for production, H2 for testing

## Technology Stack

- **Spring Boot**: 3.4.7
- **Java**: 21
- **Spring Data JPA**: ORM with Hibernate
- **Spring HATEOAS**: Hypermedia support (usuario only)
- **PostgreSQL**: Production database
- **H2**: Test database
- **Lombok**: Reduce boilerplate code
- **dotenv-java**: Environment variable management
- **SpringDoc OpenAPI**: API documentation (Swagger UI)
- **Maven**: Build and dependency management

## Port Configuration

Verify actual ports in each service's application.properties:
- Usuario: Configured as 8081 (per application.properties)
- Check inventario and carrito application.properties for their configured ports
