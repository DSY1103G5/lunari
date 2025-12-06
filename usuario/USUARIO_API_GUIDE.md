# Usuario API - Comprehensive Guide

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Getting Started](#getting-started)
4. [API Endpoints](#api-endpoints)
   - [Authentication](#authentication)
   - [User Profile](#user-profile)
   - [Points Management](#points-management)
   - [Coupon Management](#coupon-management)
5. [Data Models](#data-models)
6. [Business Logic & Workflows](#business-logic--workflows)
7. [Security](#security)
8. [Configuration](#configuration)
9. [Error Handling](#error-handling)
10. [Testing](#testing)

---

## Overview

The **Usuario API** (User Service) is a Spring Boot microservice responsible for user management, authentication, loyalty points, and coupon redemption. It is part of the LUNARi e-commerce platform and operates as an independent service using AWS DynamoDB for data persistence.

### Key Features

- User registration and authentication
- JWT-based authentication with Bearer tokens
- Loyalty points system with tiered rewards (Bronze, Silver, Gold, Platinum)
- Points redemption with level-based conversion rates
- Coupon generation and management
- User profile management (personal info, address, gaming profile, preferences)
- BCrypt password encryption with automatic migration
- Email and username-based authentication

### Technology Stack

- **Framework**: Spring Boot 3.4.7
- **Java Version**: 21
- **Database**: AWS DynamoDB (production), DynamoDB Local (development)
- **Authentication**: Spring Security + JWT (JSON Web Tokens)
- **Password Encryption**: BCrypt (strength 12)
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Build Tool**: Maven
- **Utilities**: Lombok, dotenv-java, AWS SDK DynamoDB Enhanced Client

### Base URL

- **Production**: `http://localhost:8081/api/v1`
- **Swagger UI**: `http://localhost:8081/swagger-ui`
- **API Docs**: `http://localhost:8081/api-docs`

---

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│          Controllers (REST API)          │
│  - AuthController (Public)               │
│  - UserProfileController (Protected)     │
│  - PointsController (Protected)          │
│  - CouponController (Protected)          │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│            Service Layer                 │
│  - UserService (Interface)               │
│  - UserServiceImpl (Implementation)      │
│     - User CRUD operations               │
│     - Authentication logic               │
│     - Points management                  │
│     - Coupon redemption                  │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Repository Layer                │
│  - UserRepository (Interface)            │
│  - DynamoDbUserRepositoryImpl            │
│     - DynamoDB Enhanced Client           │
│     - GSI queries (email, username)      │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         AWS DynamoDB Database            │
│  Table: lunari-users                     │
│  GSI: EmailIndex, UsernameIndex          │
└─────────────────────────────────────────┘
```

### Security Layer

```
┌─────────────────────────────────────────┐
│         JwtAuthenticationFilter          │
│  - Validates JWT tokens                  │
│  - Extracts user from token              │
│  - Populates SecurityContext             │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         CustomUserDetailsService         │
│  - Loads user by email                   │
│  - Checks user active status             │
│  - Returns Spring UserDetails            │
└─────────────────────────────────────────┘
```

---

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- AWS DynamoDB (production) or DynamoDB Local (development)
- AWS credentials (if using AWS DynamoDB)

### Environment Configuration

Create a `.env` file in `src/main/resources/` with:

```env
# AWS Configuration
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key

# DynamoDB
AWS_DYNAMODB_ENDPOINT=           # Empty for AWS, http://localhost:8000 for DynamoDB Local
DYNAMODB_TABLE_NAME=lunari-users

# JWT Configuration
JWT_SECRET=your-jwt-secret-minimum-32-characters-recommended
JWT_EXPIRATION=86400000          # 24 hours in milliseconds
```

### DynamoDB Table Setup

Create the DynamoDB table with:

**Table Name**: `lunari-users`
**Primary Key**: `id` (String)
**Global Secondary Indexes**:
- **EmailIndex**: Partition Key = `email` (String)
- **UsernameIndex**: Partition Key = `username` (String)

### Running the Service

```bash
# Navigate to usuario directory
cd usuario/

# Run with Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/lunari-user-api-0.0.1-SNAPSHOT.jar
```

The service will start on port **8081**.

### Verifying the Service

```bash
# Access Swagger UI
open http://localhost:8081/swagger-ui

# Test registration
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

---

## API Endpoints

All endpoints return responses wrapped in a standard `ApiResponse` format:

```json
{
  "success": true,
  "data": { ... },
  "message": null,
  "statusCode": 200
}
```

Error responses:

```json
{
  "success": false,
  "data": null,
  "message": "Error description",
  "statusCode": 400
}
```

---

### Authentication

All authentication endpoints are **public** (no authentication required).

#### 1. Register User

**Endpoint**: `POST /api/v1/auth/register`

**Description**: Creates a new user account with default settings.

**Request Body**:
```json
{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+56912345678"
}
```

**Request Fields**:
- `username` (String, required): Unique username
- `email` (String, required): Valid email address (must be unique)
- `password` (String, required): Minimum 8 characters
- `firstName` (String, optional): First name
- `lastName` (String, optional): Last name
- `phone` (String, optional): Phone number

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "johndoe",
    "email": "john.doe@example.com",
    "personal": {
      "firstName": "John",
      "lastName": "Doe",
      "phone": "+56912345678",
      "memberSince": "2025"
    },
    "stats": {
      "level": "Bronze",
      "points": 0,
      "purchases": 0,
      "reviews": 0,
      "favorites": 0
    },
    "preferences": {
      "notifyOffers": true,
      "notifyNewProducts": true,
      "notifyRestocks": false,
      "notifyNewsletter": true
    },
    "coupons": [],
    "isActive": true,
    "isVerified": false,
    "createdAt": "2025-01-03T10:30:00Z"
  },
  "statusCode": 201
}
```

**Default Values on Registration**:
- Level: Bronze
- Points: 0
- Purchases/Reviews/Favorites: 0
- All notifications: enabled (except restocks)
- Active: true
- Verified: false
- Member since: Current year

**Status Codes**:
- `201 CREATED`: User created successfully
- `400 BAD REQUEST`: Email already exists or validation error

---

#### 2. Login

**Endpoint**: `POST /api/v1/auth/login`

**Description**: Authenticates user and returns JWT token for API access.

**Request Body**:
```json
{
  "identifier": "john.doe@example.com",
  "password": "securePassword123"
}
```

**Request Fields**:
- `identifier` (String, required): Email or username
- `password` (String, required): User password

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "johndoe",
    "email": "john.doe@example.com",
    "fullName": "John Doe",
    "isActive": true,
    "isVerified": false,
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTcwNDI4ODAwMCwiZXhwIjoxNzA0Mzc0NDAwfQ...",
    "level": "Bronze",
    "points": 0,
    "message": "Welcome back, John!"
  },
  "statusCode": 200
}
```

**JWT Token**:
- **Format**: Bearer token
- **Expiration**: 24 hours (configurable)
- **Claims**: userId, email, username, level
- **Usage**: Include in `Authorization` header as `Bearer {token}`

**Status Codes**:
- `200 OK`: Login successful
- `401 UNAUTHORIZED`: Invalid credentials or inactive account
- `404 NOT FOUND`: User not found

---

### User Profile

All profile endpoints are **protected** and require JWT authentication.

**Authentication Header**: `Authorization: Bearer {your-jwt-token}`

#### 3. Get User Profile

**Endpoint**: `GET /api/v1/profile`

**Description**: Retrieves the authenticated user's complete profile.

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "johndoe",
    "email": "john.doe@example.com",
    "personal": {
      "firstName": "John",
      "lastName": "Doe",
      "phone": "+56912345678",
      "birthdate": "1990-05-15",
      "bio": "Gaming enthusiast and tech lover",
      "avatar": "https://example.com/avatar.jpg",
      "memberSince": "2025"
    },
    "address": {
      "addressLine1": "Av. Providencia 123",
      "addressLine2": "Apt 4B",
      "city": "Santiago",
      "region": "Región Metropolitana",
      "postalCode": "7500000",
      "country": "CL",
      "deliveryNotes": "Ring bell twice"
    },
    "gaming": {
      "gamerTag": "JohnTheGamer",
      "favoriteGenre": "rpg",
      "skillLevel": "intermediate",
      "streamingPlatforms": ["Twitch", "YouTube"],
      "favoriteGames": "The Witcher 3, Elden Ring, Baldur's Gate 3"
    },
    "preferences": {
      "favoriteCategories": ["JM", "CG"],
      "preferredPlatform": "PC",
      "gamingHours": "10-20 hours/week",
      "notifyOffers": true,
      "notifyNewProducts": true,
      "notifyRestocks": false,
      "notifyNewsletter": true
    },
    "stats": {
      "level": "Gold",
      "points": 15000,
      "purchases": 25,
      "reviews": 12,
      "favorites": 8
    },
    "coupons": [
      {
        "id": "660e8400-e29b-41d4-a716-446655440001",
        "code": "GOLD-550E8400-1234",
        "description": "Redeemed 5000 points as Gold member",
        "type": "fixed",
        "value": 100.0,
        "minPurchase": 0.0,
        "expiresAt": "2025-04-03",
        "isUsed": false
      }
    ],
    "isActive": true,
    "isVerified": true,
    "createdAt": "2025-01-03T10:30:00Z"
  },
  "statusCode": 200
}
```

**Status Codes**:
- `200 OK`: Profile retrieved successfully
- `401 UNAUTHORIZED`: Invalid or missing JWT token
- `404 NOT FOUND`: User not found

---

#### 4. Update User Profile

**Endpoint**: `PUT /api/v1/profile`

**Description**: Updates the authenticated user's profile. All fields are optional.

**Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+56987654321",
  "birthdate": "1990-05-15",
  "bio": "Updated bio text",
  "avatar": "https://example.com/new-avatar.jpg",
  "address": {
    "addressLine1": "Nueva Av. 456",
    "addressLine2": "Depto 10",
    "city": "Viña del Mar",
    "region": "Valparaíso",
    "postalCode": "2520000",
    "country": "CL",
    "deliveryNotes": "Leave at reception"
  },
  "gaming": {
    "gamerTag": "JohnPro",
    "favoriteGenre": "fps",
    "skillLevel": "advanced",
    "streamingPlatforms": ["Twitch"],
    "favoriteGames": "Counter-Strike 2, Valorant"
  },
  "preferences": {
    "favoriteCategories": ["JM", "CG", "AC"],
    "preferredPlatform": "PS5",
    "gamingHours": "20+ hours/week",
    "notifyOffers": true,
    "notifyNewProducts": false,
    "notifyRestocks": true,
    "notifyNewsletter": false
  }
}
```

**Request Fields** (all optional):
- `firstName` (String): First name
- `lastName` (String): Last name
- `phone` (String): Phone number
- `birthdate` (String): Date in YYYY-MM-DD format
- `bio` (String): User biography
- `avatar` (String): Profile image URL
- `address` (Address): Complete address object
- `gaming` (Gaming): Gaming profile object
- `preferences` (ClientPreferences): Preferences object

**Response** (200 OK):
Returns updated ProfileResponse (same format as Get Profile)

**Status Codes**:
- `200 OK`: Profile updated successfully
- `401 UNAUTHORIZED`: Invalid or missing JWT token
- `400 BAD REQUEST`: Validation error

---

### Points Management

All points endpoints are **protected** and require JWT authentication.

#### 5. Add Points to User

**Endpoint**: `POST /api/v1/points`

**Description**: Adds loyalty points to the authenticated user's account.

**Request Body**:
```json
{
  "points": 500,
  "reason": "Purchase completed - Order #12345"
}
```

**Request Fields**:
- `points` (Long, required): Number of points to add (minimum 1)
- `reason` (String, optional): Reason for adding points

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "level": "Gold",
    "points": 15500,
    "purchases": 26,
    "reviews": 12,
    "favorites": 8
  },
  "statusCode": 200
}
```

**Status Codes**:
- `200 OK`: Points added successfully
- `401 UNAUTHORIZED`: Invalid or missing JWT token
- `400 BAD REQUEST`: Invalid points value (< 1)

---

### Coupon Management

All coupon endpoints are **protected** and require JWT authentication.

#### 6. Get User Coupons

**Endpoint**: `GET /api/v1/coupons`

**Description**: Retrieves all coupons for the authenticated user.

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "code": "GOLD-550E8400-1234",
      "description": "Redeemed 5000 points as Gold member",
      "type": "fixed",
      "value": 100.0,
      "minPurchase": 0.0,
      "expiresAt": "2025-04-03",
      "isUsed": false
    },
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "code": "GOLD-550E8400-5678",
      "description": "Redeemed 10000 points as Gold member",
      "type": "fixed",
      "value": 200.0,
      "minPurchase": 0.0,
      "expiresAt": "2025-05-15",
      "isUsed": false
    }
  ],
  "statusCode": 200
}
```

**Status Codes**:
- `200 OK`: Coupons retrieved successfully
- `401 UNAUTHORIZED`: Invalid or missing JWT token

---

#### 7. Redeem Points for Coupon

**Endpoint**: `POST /api/v1/coupons/redeem`

**Description**: Converts loyalty points into a discount coupon. Conversion rate varies by user level.

**Request Body**:
```json
{
  "pointsToRedeem": 5000
}
```

**Request Fields**:
- `pointsToRedeem` (Long, required): Number of points to redeem (minimum 100)

**Conversion Rates by Level**:
- **Bronze**: 0.01 (100 points = $1.00 CLP)
- **Silver**: 0.015 (100 points = $1.50 CLP)
- **Gold**: 0.02 (100 points = $2.00 CLP)
- **Platinum**: 0.025 (100 points = $2.50 CLP)

**Example Calculation** (Gold level):
- Points to redeem: 5000
- Conversion rate: 0.02
- Coupon value: 5000 × 0.02 = $100.00 CLP

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "880e8400-e29b-41d4-a716-446655440003",
    "code": "GOLD-550E8400-9012",
    "description": "Redeemed 5000 points as Gold member",
    "type": "fixed",
    "value": 100.0,
    "minPurchase": 0.0,
    "expiresAt": "2025-04-03",
    "message": "Successfully redeemed 5000 points for a $100.0 coupon!"
  },
  "statusCode": 200
}
```

**Coupon Properties**:
- **Code Format**: `{LEVEL}-{USER_ID_8_CHARS}-{RANDOM_4_DIGITS}`
- **Expiration**: 90 days from redemption
- **Type**: Always "fixed" (fixed amount discount)
- **Min Purchase**: $0.00 (no minimum)

**Status Codes**:
- `200 OK`: Coupon created successfully
- `401 UNAUTHORIZED`: Invalid or missing JWT token
- `400 BAD REQUEST`:
  - Points to redeem < 100
  - Insufficient points balance
  - Invalid points value

---

#### 8. Remove Coupon

**Endpoint**: `DELETE /api/v1/coupons/{couponId}`

**Description**: Removes a coupon from the user's account.

**Path Parameters**:
- `couponId` (String): Coupon ID (UUID)

**Response** (204 No Content):
No response body

**Status Codes**:
- `204 NO_CONTENT`: Coupon removed successfully
- `401 UNAUTHORIZED`: Invalid or missing JWT token
- `404 NOT FOUND`: Coupon not found

---

## Data Models

### User (Primary Entity)

**DynamoDB Table**: `lunari-users`
**Primary Key**: `id` (String, UUID)
**GSI**: EmailIndex (email), UsernameIndex (username)

| Field | Type | Description |
|-------|------|-------------|
| id | String | UUID of user |
| username | String | Unique username |
| email | String | Unique email (used for authentication) |
| password | String | BCrypt hashed password |
| personal | Personal | Personal information object |
| address | Address | Shipping address object |
| preferences | ClientPreferences | User preferences |
| gaming | Gaming | Gaming profile |
| stats | ClientStats | User statistics and loyalty level |
| coupons | List&lt;Coupon&gt; | User's coupons |
| isActive | Boolean | Account active status (default: true) |
| isVerified | Boolean | Email verified status (default: false) |
| createdAt | String | ISO 8601 creation timestamp |
| updatedAt | String | ISO 8601 last update timestamp |

---

### Personal (Embedded Document)

| Field | Type | Description |
|-------|------|-------------|
| firstName | String | First name |
| lastName | String | Last name |
| phone | String | Phone number |
| birthdate | String | Date of birth (YYYY-MM-DD) |
| bio | String | User biography |
| avatar | String | Profile image URL |
| memberSince | String | Year joined (e.g., "2025") |

---

### Address (Embedded Document)

| Field | Type | Description |
|-------|------|-------------|
| addressLine1 | String | Street address and number |
| addressLine2 | String | Apartment/office (optional) |
| city | String | City |
| region | String | State/region |
| postalCode | String | Postal code |
| country | String | Country code (e.g., "CL") |
| deliveryNotes | String | Special delivery instructions |

---

### ClientStats (Embedded Document)

| Field | Type | Description |
|-------|------|-------------|
| level | String | Loyalty level (Bronze, Silver, Gold, Platinum) |
| points | Long | Current loyalty points balance |
| purchases | Integer | Total number of purchases |
| reviews | Integer | Total number of reviews written |
| favorites | Integer | Total number of favorited items |

**Loyalty Levels**:
- **Bronze**: Default level for new users
- **Silver**: Intermediate level
- **Gold**: Advanced level
- **Platinum**: Premium level

---

### ClientPreferences (Embedded Document)

| Field | Type | Description |
|-------|------|-------------|
| favoriteCategories | List&lt;String&gt; | Preferred product categories (e.g., ["JM", "CG"]) |
| preferredPlatform | String | Gaming platform preference |
| gamingHours | String | Weekly gaming hours bracket |
| notifyOffers | Boolean | Special offers notifications (default: true) |
| notifyNewProducts | Boolean | New product notifications (default: true) |
| notifyRestocks | Boolean | Restock notifications (default: false) |
| notifyNewsletter | Boolean | Newsletter subscription (default: true) |

---

### Gaming (Embedded Document)

| Field | Type | Description |
|-------|------|-------------|
| gamerTag | String | Gaming username/handle |
| favoriteGenre | String | Favorite game genre (rpg, fps, strategy, etc.) |
| skillLevel | String | Skill level (beginner, intermediate, advanced, pro) |
| streamingPlatforms | List&lt;String&gt; | Streaming platforms (Twitch, YouTube, etc.) |
| favoriteGames | String | Comma-separated list of favorite games |

---

### Coupon (Embedded Document)

| Field | Type | Description |
|-------|------|-------------|
| id | String | Unique coupon ID (UUID) |
| code | String | Coupon code (format: {LEVEL}-{USER_ID}-{RANDOM}) |
| description | String | Coupon description |
| type | String | Discount type ("fixed" or "percentage") |
| value | Double | Discount amount or percentage |
| minPurchase | Double | Minimum purchase required (CLP) |
| expiresAt | String | Expiration date (YYYY-MM-DD) |
| isUsed | Boolean | Whether coupon has been used (default: false) |

---

## Business Logic & Workflows

### User Registration Flow

1. **Receive RegisterRequest** with username, email, password
2. **Validate email uniqueness** (throws exception if exists)
3. **Generate UUID** for user ID
4. **Hash password** using BCrypt (strength 12)
5. **Initialize user** with default values:
   - Level: Bronze
   - Points: 0
   - All preferences: enabled (except restocks)
   - Status: Active=true, Verified=false
   - Member since: Current year
6. **Save to DynamoDB**
7. **Return ProfileResponse**

---

### Authentication Flow

1. **Receive LoginRequest** with identifier (email or username) and password
2. **Look up user** by email (primary) or username via GSI
3. **Verify user is active** (isActive = true)
4. **Validate password**:
   - If BCrypt hash (starts with $2a$ or $2b$): Use BCryptPasswordEncoder
   - If plain text: Compare directly and **auto-migrate to BCrypt**
5. **Generate JWT token** with claims:
   - Subject: email
   - userId: User ID
   - username: Username
   - level: User level
   - Expiration: 24 hours (default)
6. **Return LoginResponse** with token and user info

**Auto-Migration**: If a user has a plain text password, the system automatically hashes it with BCrypt upon successful login.

---

### Profile Update Flow

1. **Authenticate user** via JWT token
2. **Extract email** from JWT claims
3. **Get user** by email from DynamoDB
4. **Update fields** from UpdateProfileRequest (all optional):
   - Personal info (firstName, lastName, phone, birthdate, bio, avatar)
   - Address object
   - Gaming profile
   - Preferences
5. **Update timestamp** (updatedAt)
6. **Save to DynamoDB**
7. **Return updated ProfileResponse**

---

### Points Management Flow

**Adding Points**:
1. Authenticate user via JWT
2. Get user by email
3. Validate points to add (minimum 1)
4. Add to `user.stats.points`
5. Update timestamp
6. Save to DynamoDB
7. Return updated ClientStats

**Note**: Points are typically added by external services (e.g., Carrito service after purchase completion).

---

### Points Redemption Flow

1. **Authenticate user** via JWT
2. **Get user** by email
3. **Validate minimum** 100 points to redeem
4. **Check sufficient balance** (throws exception if insufficient)
5. **Get conversion rate** based on user level:
   - Bronze: 0.01
   - Silver: 0.015
   - Gold: 0.02
   - Platinum: 0.025
6. **Calculate coupon value**: pointsToRedeem × conversionRate
7. **Generate coupon**:
   - Unique ID (UUID)
   - Code: `{LEVEL}-{USER_ID_8_CHARS}-{RANDOM_4_DIGITS}`
   - Description: "Redeemed X points as LEVEL member"
   - Type: "fixed"
   - Value: calculated amount
   - Expiration: 90 days from today
8. **Deduct points** from user balance
9. **Add coupon** to user.coupons list
10. **Save to DynamoDB**
11. **Return CouponResponse**

---

### Coupon Management

**Get Coupons**:
- Returns all coupons from `user.coupons` list
- No filtering applied (returns used and unused)

**Remove Coupon**:
- Filters out coupon by ID from coupons list
- Saves updated user to DynamoDB
- Returns 204 No Content

---

## Security

### JWT Authentication

**Token Generation**:
- **Algorithm**: HMAC SHA-256
- **Expiration**: 24 hours (configurable via `jwt.expiration`)
- **Secret**: Configured via `JWT_SECRET` environment variable
- **Claims**:
  - `sub` (Subject): User email
  - `userId`: User ID (UUID)
  - `username`: Username
  - `level`: User loyalty level
  - `iat` (Issued At): Token creation timestamp
  - `exp` (Expiration): Token expiration timestamp

**Token Usage**:
```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  http://localhost:8081/api/v1/profile
```

---

### JwtAuthenticationFilter

**Process**:
1. **Intercepts every request** (except public endpoints)
2. **Extracts JWT** from `Authorization` header
3. **Validates token** signature and expiration
4. **Extracts claims** (email, userId, username, level)
5. **Loads user** via CustomUserDetailsService
6. **Populates SecurityContext** with authenticated user
7. **Proceeds with request**

---

### Password Security

**BCrypt Configuration**:
- **Strength**: 12 rounds
- **Algorithm**: BCrypt (Blowfish cipher)
- **Salt**: Automatically generated per password

**Auto-Migration Support**:
- System detects plain text passwords (missing BCrypt prefix)
- Automatically hashes on next successful login
- Maintains backward compatibility during migration

---

### SecurityConfig

**Public Endpoints** (No Authentication Required):
- `/api/v1/auth/login`
- `/api/v1/auth/register`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/api-docs/**`
- `/error`

**Protected Endpoints** (JWT Required):
- `/api/v1/profile/**`
- `/api/v1/points/**`
- `/api/v1/coupons/**`

**Session Management**: Stateless (no server-side sessions)

**CSRF Protection**: Disabled (API-only service)

---

## Configuration

### application.properties

**Location**: `src/main/resources/application.properties`

```properties
# Application
spring.application.name=lunari-user-api
server.port=8081

# AWS DynamoDB
aws.region=${AWS_REGION:us-east-1}
aws.dynamodb.tableName=${DYNAMODB_TABLE_NAME:lunari-users}
aws.dynamodb.endpoint=${AWS_DYNAMODB_ENDPOINT:}

# Pagination
app.pagination.defaultLimit=10
app.pagination.maxLimit=100

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

# Swagger/OpenAPI
springdoc.swagger-ui.path=/swagger-ui
springdoc.api-docs.path=/api-docs
```

---

### Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| AWS_REGION | No | us-east-1 | AWS region for DynamoDB |
| AWS_ACCESS_KEY_ID | Yes* | - | AWS access key (*not required for IAM role) |
| AWS_SECRET_ACCESS_KEY | Yes* | - | AWS secret key (*not required for IAM role) |
| AWS_DYNAMODB_ENDPOINT | No | - | DynamoDB endpoint (leave empty for AWS, use `http://localhost:8000` for local) |
| DYNAMODB_TABLE_NAME | No | lunari-users | DynamoDB table name |
| JWT_SECRET | Yes | - | JWT signing secret (minimum 32 characters recommended) |
| JWT_EXPIRATION | No | 86400000 | JWT expiration in milliseconds (24 hours) |

---

### Spring Profiles

- **default**: Uses configuration from `application.properties`
- **local**: DynamoDB Local development
- **dev**: Development environment
- **prod**: Production environment with AWS DynamoDB
- **test**: Testing with H2 in-memory database

**Activate Profile**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## Error Handling

### Custom Exceptions

| Exception | Status Code | Description |
|-----------|-------------|-------------|
| UserAlreadyExistsException | 400 | Email already registered |
| UserNotFoundException | 404 | User not found |
| InvalidCredentialsException | 401 | Invalid email/username or password |
| InsufficientPointsException | 400 | Not enough points to redeem |
| InvalidTokenException | 401 | Invalid or expired JWT token |

---

### Standard Error Response

```json
{
  "success": false,
  "data": null,
  "message": "Email already exists",
  "statusCode": 400
}
```

---

### HTTP Status Codes

- `200 OK`: Successful operation
- `201 CREATED`: Resource created successfully
- `204 NO CONTENT`: Successful deletion
- `400 BAD REQUEST`: Invalid input or business rule violation
- `401 UNAUTHORIZED`: Authentication required or failed
- `404 NOT FOUND`: Resource not found
- `500 INTERNAL SERVER ERROR`: Unexpected server error

---

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceImplTest

# Run with coverage
mvn clean test jacoco:report
```

### Test Database

Tests use H2 in-memory database configured via `application-test.properties`.

---

## Common Use Cases

### Use Case 1: Complete Registration and Login Flow

```bash
# 1. Register new user
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "gamer123",
    "email": "gamer@example.com",
    "password": "securePass123",
    "firstName": "Alex",
    "lastName": "Smith"
  }'

# 2. Login to get JWT token
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "gamer@example.com",
    "password": "securePass123"
  }'
# Save the token from response

# 3. Get user profile
curl -H "Authorization: Bearer {your-token}" \
  http://localhost:8081/api/v1/profile
```

---

### Use Case 2: Points Redemption

```bash
# 1. Add points (typically done by Carrito service)
curl -X POST http://localhost:8081/api/v1/points \
  -H "Authorization: Bearer {your-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "points": 5000,
    "reason": "Purchase completed"
  }'

# 2. Redeem points for coupon
curl -X POST http://localhost:8081/api/v1/coupons/redeem \
  -H "Authorization: Bearer {your-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "pointsToRedeem": 2000
  }'

# 3. View all coupons
curl -H "Authorization: Bearer {your-token}" \
  http://localhost:8081/api/v1/coupons
```

---

### Use Case 3: Profile Update

```bash
# Update profile with new address and preferences
curl -X PUT http://localhost:8081/api/v1/profile \
  -H "Authorization: Bearer {your-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "address": {
      "addressLine1": "Av. Libertador 789",
      "city": "Santiago",
      "region": "Metropolitana",
      "postalCode": "8320000",
      "country": "CL"
    },
    "gaming": {
      "gamerTag": "ProGamer123",
      "favoriteGenre": "fps",
      "skillLevel": "advanced"
    },
    "preferences": {
      "favoriteCategories": ["JM", "CG", "AC"],
      "notifyOffers": true,
      "notifyRestocks": true
    }
  }'
```

---

## Best Practices

1. **Always use HTTPS in production** to protect JWT tokens and sensitive data
2. **Store JWT tokens securely** (HttpOnly cookies or secure localStorage)
3. **Implement token refresh** for better UX (currently 24-hour expiration)
4. **Validate user input** on client-side before API calls
5. **Handle token expiration** gracefully (redirect to login)
6. **Use strong passwords** (minimum 8 characters, complexity recommended)
7. **Monitor points balance** before redemption attempts
8. **Set reasonable conversion rates** based on business model
9. **Implement coupon expiration checks** in frontend
10. **Log authentication failures** for security monitoring

---

## Troubleshooting

### Issue: JWT token expired

**Solution**: Login again to get a new token. Implement token refresh mechanism for better UX.

### Issue: Email already exists during registration

**Solution**: Use a unique email or login with existing account.

### Issue: Insufficient points for redemption

**Solution**: Check points balance via profile endpoint. Minimum 100 points required.

### Issue: Cannot connect to DynamoDB

**Solution**:
- Check AWS credentials are set correctly
- Verify DynamoDB table exists
- For local development, ensure DynamoDB Local is running on port 8000
- Verify `AWS_DYNAMODB_ENDPOINT` is set correctly

### Issue: Password auto-migration not working

**Solution**: Ensure BCryptPasswordEncoder bean is configured with strength 12.

---

## Additional Resources

- **Swagger UI**: http://localhost:8081/swagger-ui
- **API Docs (JSON)**: http://localhost:8081/api-docs
- **AWS DynamoDB Documentation**: https://docs.aws.amazon.com/dynamodb/
- **Spring Security**: https://spring.io/projects/spring-security
- **JWT.io**: https://jwt.io/ (JWT debugger)
- **Spring Boot Docs**: https://spring.io/projects/spring-boot

---

## Support & Contribution

For issues, bugs, or feature requests, please contact the development team or create an issue in the project repository.

---

**Last Updated**: January 3, 2025
**Version**: 2.0.0
**Maintainer**: LUNARi Development Team
