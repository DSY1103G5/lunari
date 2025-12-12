# Migration Guide: DynamoDB to PostgreSQL NeonDB

This guide explains how to migrate the usuario API service from AWS DynamoDB to PostgreSQL NeonDB.

## Overview of Changes

### 1. Dependencies (pom.xml)

**Removed:**
- AWS SDK for DynamoDB Enhanced Client
- AWS SDK DynamoDB
- AWS SDK BOM dependency management

**Added:**
- Spring Boot Starter Data JPA
- PostgreSQL JDBC Driver
- H2 Database (for testing)

### 2. Entity Models

**User.java and nested models updated:**
- Replaced `@DynamoDbBean` with `@Entity` and `@Table`
- Replaced `@DynamoDbPartitionKey` with `@Id`
- Removed all `@DynamoDbAttribute` annotations
- Added JPA annotations: `@Column`, `@JdbcTypeCode`
- Nested objects (Personal, Address, etc.) now stored as JSONB in PostgreSQL
- Changed timestamps from `String` to `LocalDateTime`
- Added `@PrePersist` and `@PreUpdate` for automatic timestamp management

### 3. Repository

**UserRepository.java:**
- Changed from custom interface to extend `JpaRepository<User, String>`
- Replaced DynamoDB pagination (tokens) with Spring Data JPA `Pageable`
- Replaced `DynamoDbPage` returns with `Page<User>`
- Added Spring Data JPA query methods (derived queries)
- Simplified implementation - Spring Data JPA provides most methods automatically

**Removed:**
- `DynamoDbUserRepositoryImpl.java` - No longer needed with JPA
- `DynamoDbPage.java` - Replaced by Spring's `Page`
- Custom pagination token handling

### 4. Configuration

**application.properties:**
- Removed AWS/DynamoDB configuration
- Added PostgreSQL datasource configuration
- Added JPA/Hibernate configuration

**Config files:**
- Removed `DynamoDbConfig.java`
- Kept `DotEnvConfig.java`, `SecurityConfig.java`, `OpenApiConfig.java`

### 5. Database Schema

**Table structure:**
- Single `users` table with JSONB columns for nested objects
- Indexes on email, username, is_active, is_verified
- GIN indexes on JSONB columns for efficient JSON queries
- Automatic trigger for `updated_at` timestamp

## Setup Instructions

### Step 1: Create NeonDB Database

1. Go to [Neon.tech](https://neon.tech) and sign in
2. Create a new project or use an existing one
3. Create a database named `lunari_users` (or use default)
4. Note your connection details from the dashboard

### Step 2: Run Migration Script

1. Open NeonDB SQL Editor in your dashboard
2. Copy the entire content of `migration_neondb.sql`
3. Paste and execute in the SQL Editor
4. Verify the table was created:
   ```sql
   SELECT * FROM users;
   ```

### Step 3: Configure Environment Variables

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` and fill in your NeonDB credentials:
   ```properties
   DB_URL=jdbc:postgresql://your-neon-host.neon.tech/lunari_users?sslmode=require
   DB_USERNAME=your-username
   DB_PASSWORD=your-password
   JWT_SECRET=your-secure-secret-key
   ```

### Step 4: Build and Run

1. Clean and build the project:
   ```bash
   mvn clean package
   ```

2. Run the application:
   ```bash
   mvn spring-boot:run
   ```

3. Verify the application started successfully and check logs for database connection

### Step 5: Test the API

1. Access Swagger UI:
   ```
   http://localhost:8081/swagger-ui/index.html
   ```

2. Test basic endpoints:
   - GET `/api/v1/users` - List all users
   - GET `/api/v1/users/{id}` - Get user by ID
   - POST `/api/v1/users` - Create new user

## Key Differences in Usage

### Pagination

**Before (DynamoDB):**
```java
DynamoDbPage<User> users = userRepository.findAll(10, paginationToken);
String nextToken = users.getNextToken();
```

**After (PostgreSQL with JPA):**
```java
Page<User> users = userRepository.findAll(PageRequest.of(0, 10));
boolean hasNext = users.hasNext();
int totalPages = users.getTotalPages();
```

### Querying

**Before (DynamoDB):**
```java
DynamoDbPage<User> activeUsers = userRepository.findByActive(true, 10, null);
```

**After (PostgreSQL with JPA):**
```java
Page<User> activeUsers = userRepository.findByIsActive(true, PageRequest.of(0, 10));
```

### Search

**Before (DynamoDB):**
```java
DynamoDbPage<User> results = userRepository.searchUsers("john", 10, null);
```

**After (PostgreSQL with JPA):**
```java
Page<User> results = userRepository.searchUsers("john", PageRequest.of(0, 10));
```

## Data Migration (Optional)

If you have existing data in DynamoDB that needs to be migrated:

1. Export data from DynamoDB to JSON
2. Transform the data structure:
   - Convert timestamp strings to ISO format
   - Ensure nested objects match the JSONB structure
   - Generate UUIDs for `id` field if needed

3. Create a migration script to insert data:
   ```sql
   INSERT INTO users (id, username, email, password, personal, ...)
   VALUES ('uuid', 'username', 'email@example.com', 'hashedpass',
           '{"firstName": "John", "lastName": "Doe"}'::jsonb, ...);
   ```

## Testing

### Unit Tests

Tests should still work with minimal changes since the service layer interface remains the same. Update test data to use `LocalDateTime` instead of String timestamps.

### Integration Tests

Update integration tests to use H2 in-memory database (already configured in test scope):

```properties
# src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

## Rollback Plan

If you need to rollback to DynamoDB:

1. Checkout the previous commit before migration
2. Restore `pom.xml`, models, repository, and config files
3. Ensure DynamoDB table and GSI indexes exist
4. Update environment variables back to AWS credentials

## Performance Considerations

### PostgreSQL Advantages:
- True ACID transactions
- Complex queries with JOINs
- Better support for text search
- More mature ecosystem
- Lower latency for small datasets

### Indexing Strategy:
- Primary key on `id`
- Unique indexes on `email`, `username`
- GIN indexes on JSONB columns for JSON querying
- Composite indexes for frequently filtered columns

### Query Optimization:
```sql
-- Efficient JSONB queries
SELECT * FROM users WHERE personal->>'firstName' = 'John';

-- Use GIN index for contains queries
SELECT * FROM users WHERE personal @> '{"firstName": "John"}'::jsonb;
```

## Troubleshooting

### Connection Issues

**Error:** `Connection refused`
- Check DB_URL is correct
- Verify NeonDB project is active (not suspended)
- Ensure SSL mode is set: `?sslmode=require`

**Error:** `Authentication failed`
- Verify DB_USERNAME and DB_PASSWORD
- Check credentials in NeonDB dashboard
- Ensure no extra spaces in .env file

### Hibernate Issues

**Error:** `Table "users" not found`
- Run migration_neondb.sql in NeonDB SQL Editor
- Check database name in connection URL
- Verify spring.jpa.hibernate.ddl-auto setting

**Error:** `Column "xxx" not found`
- Ensure migration script ran successfully
- Check column names match entity annotations
- Verify table schema: `\d users` in psql

### JSONB Issues

**Error:** `column "personal" is of type jsonb but expression is of type varchar`
- Ensure columns are created with type `jsonb`
- Check migration script ran completely
- Try recreating the table

## Support

For issues or questions:
- Check application logs for detailed error messages
- Review NeonDB dashboard for connection status
- Verify environment variables are loaded correctly

## Additional Resources

- [NeonDB Documentation](https://neon.tech/docs)
- [Spring Data JPA Reference](https://spring.io/projects/spring-data-jpa)
- [PostgreSQL JSONB Documentation](https://www.postgresql.org/docs/current/datatype-json.html)
