# Error Handling Fix - Invalid Credentials

## Problem

Invalid credentials during login were returning a 500 Internal Server Error instead of a proper 401 Unauthorized response:

**Before (Incorrect):**
```json
{
  "path": "/error",
  "error": "Internal Server Error",
  "message": "Invalid credentials",
  "status": 500
}
```

**Required Format:**
```json
{
  "success": false,
  "response": {
    "error_code": "INVALID_CREDENTIALS",
    "message": "Credenciales inválidas",
    "status": 401
  },
  "statusCode": 401
}
```

## Root Cause

The `UserServiceImpl.authenticateUser()` method was throwing generic `RuntimeException` which Spring Boot's default error handler caught and returned as 500 Internal Server Error.

## Solution Implemented

### 1. Created Custom Exception Classes

**InvalidCredentialsException.java**
```java
package cl.duoc.lunari.api.user.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
```

**AccountInactiveException.java**
```java
package cl.duoc.lunari.api.user.exception;

public class AccountInactiveException extends RuntimeException {
    public AccountInactiveException(String message) {
        super(message);
    }
}
```

### 2. Created Global Exception Handler

**GlobalExceptionHandler.java**
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleInvalidCredentials(
        InvalidCredentialsException ex) {

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error_code", "INVALID_CREDENTIALS");
        errorDetails.put("message", "Credenciales inválidas");
        errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());

        ApiResponse<Map<String, Object>> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setResponse(errorDetails);
        response.setStatusCode(HttpStatus.UNAUTHORIZED.value());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleAccountInactive(
        AccountInactiveException ex) {

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error_code", "ACCOUNT_INACTIVE");
        errorDetails.put("message", "Cuenta inactiva. Por favor contacta a soporte.");
        errorDetails.put("status", HttpStatus.FORBIDDEN.value());

        ApiResponse<Map<String, Object>> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setResponse(errorDetails);
        response.setStatusCode(HttpStatus.FORBIDDEN.value());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}
```

### 3. Updated UserServiceImpl

Modified `authenticateUser()` method to throw custom exceptions:

**UserServiceImpl.java:103** - User not found:
```java
throw new InvalidCredentialsException("Invalid credentials");
```

**UserServiceImpl.java:111** - Account inactive:
```java
throw new AccountInactiveException("Account is inactive. Please contact support.");
```

**UserServiceImpl.java:136** - Wrong password:
```java
throw new InvalidCredentialsException("Invalid credentials");
```

## Test Results

### ✅ Invalid Email

**Request:**
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"wrong@example.com","password":"wrongpassword"}'
```

**Response (401 Unauthorized):**
```json
{
  "success": false,
  "response": {
    "error_code": "INVALID_CREDENTIALS",
    "message": "Credenciales inválidas",
    "status": 401
  },
  "statusCode": 401
}
```

### ✅ Invalid Password

**Request:**
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"testuser3@example.com","password":"wrongpassword"}'
```

**Response (401 Unauthorized):**
```json
{
  "success": false,
  "response": {
    "error_code": "INVALID_CREDENTIALS",
    "message": "Credenciales inválidas",
    "status": 401
  },
  "statusCode": 401
}
```

### ✅ Valid Login (Still Works)

**Request:**
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"testuser3@example.com","password":"SecurePass123"}'
```

**Response (200 OK):**
```json
{
  "success": true,
  "response": {
    "userId": "bb839879-f303-49a6-a5e9-b0432aca2337",
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "fullName": "Test User",
    "level": "Bronze"
  },
  "statusCode": 200
}
```

## HTTP Status Codes

| Scenario | Status Code | Error Code |
|----------|------------|------------|
| Invalid email | 401 Unauthorized | INVALID_CREDENTIALS |
| Invalid password | 401 Unauthorized | INVALID_CREDENTIALS |
| Account inactive | 403 Forbidden | ACCOUNT_INACTIVE |
| Valid login | 200 OK | N/A |

## Files Created

1. `src/main/java/cl/duoc/lunari/api/user/exception/InvalidCredentialsException.java`
2. `src/main/java/cl/duoc/lunari/api/user/exception/AccountInactiveException.java`
3. `src/main/java/cl/duoc/lunari/api/user/exception/GlobalExceptionHandler.java`

## Files Modified

1. `src/main/java/cl/duoc/lunari/api/user/service/UserServiceImpl.java`
   - Added imports for custom exceptions (lines 4-5)
   - Updated `authenticateUser()` to throw custom exceptions (lines 103, 111, 136)

## Benefits

1. **Proper HTTP Status Codes**: 401 for authentication failures instead of 500
2. **Consistent Error Format**: All API errors follow the same structure
3. **Internationalization Ready**: Error messages in Spanish as requested
4. **Centralized Error Handling**: `@RestControllerAdvice` handles all exceptions globally
5. **Better Security**: Doesn't reveal whether email exists (returns same error for both cases)
6. **Extensible**: Easy to add more custom exceptions and handlers in the future

---

**Date**: 2025-12-06
**Status**: ✅ COMPLETE
