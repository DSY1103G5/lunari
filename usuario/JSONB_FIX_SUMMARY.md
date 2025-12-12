# JSONB Deserialization Fix Summary

## Problem

After migrating from DynamoDB to PostgreSQL NeonDB, the application encountered two JSONB-related errors:

### Error 1: Unrecognized Property

```json
{
  "error": "Internal Server Error",
  "message": "Could not deserialize string to java type: class cl.duoc.lunari.api.user.model.Personal",
  "cause": "Unrecognized field 'fullName' (class cl.duoc.lunari.api.user.model.Personal)"
}
```

**Root Cause**: The `Personal` and `Address` classes had computed getter methods (`getFullName()` and `getFormattedAddress()`) that Jackson was including in JSON serialization, but these properties had no corresponding fields for deserialization.

### Error 2: Type Mismatch

```
ERROR: column "address" is of type jsonb but expression is of type character varying
Hint: You will need to rewrite or cast the expression.
```

**Root Cause**: Custom `AttributeConverter` classes were not being properly recognized by Hibernate, causing it to treat JSONB columns as plain VARCHAR.

## Solutions Applied

### Fix 1: Added @JsonIgnore to Computed Properties

Added `@JsonIgnore` annotation to exclude computed getter methods from JSON serialization:

**Personal.java** (src/main/java/cl/duoc/lunari/api/user/model/Personal.java:34)
```java
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonIgnore
public String getFullName() {
    return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
}
```

**Address.java** (src/main/java/cl/duoc/lunari/api/user/model/Address.java:35)
```java
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonIgnore
public String getFormattedAddress() {
    // ... implementation
}
```

### Fix 2: Use Hibernate's Native JSON Support

Replaced custom `AttributeConverter` classes with Hibernate 6.x's built-in JSONB support using `@JdbcTypeCode`:

**User.java** (src/main/java/cl/duoc/lunari/api/user/model/User.java)
```java
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// Changed from:
@Convert(converter = JsonConverters.PersonalConverter.class)
@Column(name = "personal", columnDefinition = "jsonb")
private Personal personal;

// To:
@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "personal", columnDefinition = "jsonb")
private Personal personal;
```

Applied to all JSONB fields:
- `personal` (Personal)
- `address` (Address)
- `preferences` (ClientPreferences)
- `gaming` (Gaming)
- `stats` (ClientStats)
- `coupons` (List<Coupon>)

## Results

### ✅ Successful User Registration

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser3",
    "email": "testuser3@example.com",
    "password": "SecurePass123",
    "firstName": "Test",
    "lastName": "User",
    "phone": "+56912345678"
  }'
```

Response:
```json
{
  "success": true,
  "response": {
    "id": "bb839879-f303-49a6-a5e9-b0432aca2337",
    "username": "testuser3",
    "email": "testuser3@example.com",
    "personal": {
      "firstName": "Test",
      "lastName": "User",
      "phone": "+56912345678",
      "memberSince": "2025"
    },
    "preferences": {
      "notifyOffers": true,
      "notifyNewProducts": true
    },
    "stats": {
      "level": "Bronze",
      "points": 0
    },
    "coupons": [],
    "isActive": true,
    "isVerified": false
  }
}
```

### ✅ Successful Login

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser3@example.com",
    "password": "SecurePass123"
  }'
```

Response includes JWT token and user data:
```json
{
  "success": true,
  "response": {
    "userId": "bb839879-f303-49a6-a5e9-b0432aca2337",
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "fullName": "Test User",
    "level": "Bronze"
  }
}
```

### ✅ Data Verified in NeonDB

```sql
SELECT id, username, email, personal->>'firstName' as first_name
FROM users
WHERE username='testuser3';
```

Result:
```
id                                   | username  | email                 | first_name
-------------------------------------+-----------+-----------------------+-----------
bb839879-f303-49a6-a5e9-b0432aca2337 | testuser3 | testuser3@example.com | Test
```

## Key Learnings

1. **Lombok @Data + Computed Getters**: When using `@Data`, any getter method is automatically treated as a serializable property by Jackson. Use `@JsonIgnore` for computed/derived properties.

2. **Hibernate 6.x JSON Support**: Modern Hibernate versions (6.x) have native JSONB support via `@JdbcTypeCode(SqlTypes.JSON)`, eliminating the need for custom `AttributeConverter` classes.

3. **PostgreSQL JSONB Advantages**: JSONB columns allow querying nested JSON data using PostgreSQL operators (e.g., `personal->>'firstName'`).

## Files Modified

1. **Personal.java** - Added `@JsonIgnore` to `getFullName()`
2. **Address.java** - Added `@JsonIgnore` to `getFormattedAddress()`
3. **User.java** - Replaced `@Convert` with `@JdbcTypeCode(SqlTypes.JSON)`

## Migration Status

**✅ COMPLETE** - The usuario API service has been successfully migrated from DynamoDB to PostgreSQL NeonDB with full JSONB support for nested objects.

All endpoints are functional:
- ✅ User registration
- ✅ User login with JWT
- ✅ User profile operations
- ✅ JSONB field persistence and retrieval

---

**Date**: 2025-12-06
**Database**: PostgreSQL 17.7 (NeonDB)
**Hibernate**: 6.6.18.Final
**Spring Boot**: 3.4.7
