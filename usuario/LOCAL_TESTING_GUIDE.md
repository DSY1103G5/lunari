# Local Testing Guide - Usuario Service with NeonDB

This guide walks you through testing the usuario microservice locally with a real NeonDB PostgreSQL database.

---

## Prerequisites

- Java 21 installed
- Git installed
- A web browser
- Internet connection
- cURL or Postman (for API testing)

---

## Step 1: Set Up NeonDB Account

### 1.1 Create NeonDB Account

1. Go to https://neon.tech
2. Click **Sign Up** (you can use GitHub, Google, or email)
3. Verify your email if needed
4. You'll be redirected to the NeonDB dashboard

### 1.2 Create a New Project

1. Click **Create Project** or **New Project**
2. Enter project details:
   - **Project Name:** `lunari` (or any name you prefer)
   - **Database Name:** `lunari_users`
   - **Region:** Choose the closest to you (e.g., `US East (Ohio)`)
3. Click **Create Project**

### 1.3 Get Connection Details

Once the project is created, you'll see the connection details:

1. Click on your project
2. Navigate to **Dashboard** or **Connection Details**
3. You'll see connection strings in different formats
4. **Copy the following information:**
   - **Host:** Something like `ep-cool-darkness-123456.us-east-2.aws.neon.tech`
   - **Database:** `lunari_users` (or the name you chose)
   - **User:** Usually shown as `username` (e.g., `username`)
   - **Password:** Click **Show password** to reveal it

Example connection string format:
```
postgresql://username:password@ep-cool-darkness-123456.us-east-2.aws.neon.tech/lunari_users?sslmode=require
```

**IMPORTANT:** Keep this information secure and don't share it publicly!

---

## Step 2: Create Database Schema

### 2.1 Open NeonDB SQL Editor

1. In your NeonDB dashboard, click on **SQL Editor** in the left sidebar
2. You should see a query editor interface

### 2.2 Run Migration Script

1. Open the file `migration_neondb.sql` from your local project
2. **Copy the entire contents** of the file
3. **Paste** into the NeonDB SQL Editor
4. Click **Run** or press `Ctrl+Enter` (Windows/Linux) / `Cmd+Enter` (Mac)

You should see output indicating tables and indexes were created:

```
CREATE TABLE
CREATE INDEX
CREATE INDEX
...
CREATE TRIGGER
INSERT 0 1
INSERT 0 1
INSERT 0 1
```

### 2.3 Verify Table Creation

Run this query in the SQL Editor:

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public';
```

You should see:
```
table_name
----------
users
```

Check the data:

```sql
SELECT id, username, email, is_active, is_verified, created_at
FROM users
ORDER BY created_at DESC;
```

You should see 3 sample users if you ran the full migration script.

---

## Step 3: Configure Local Environment

### 3.1 Navigate to Project Directory

```bash
cd /home/aframuz/code/study/duoc/3er_semestre/fullstack-i/lunari/usuario
```

### 3.2 Create .env File

Create a new `.env` file in the `usuario` directory:

```bash
nano .env
```

Or using any text editor you prefer.

### 3.3 Add NeonDB Credentials

Paste the following configuration, **replacing with your actual NeonDB credentials**:

```properties
# NeonDB PostgreSQL Configuration
DB_URL=jdbc:postgresql://ep-cool-darkness-123456.us-east-2.aws.neon.tech/lunari_users?sslmode=require
DB_USERNAME=your-neon-username
DB_PASSWORD=your-neon-password

# JWT Configuration
JWT_SECRET=my-super-secret-jwt-key-change-this-in-production-minimum-32-characters
JWT_EXPIRATION=86400000

# Server Configuration (optional)
SERVER_PORT=8081
```

**Important notes:**
- Replace `ep-cool-darkness-123456.us-east-2.aws.neon.tech` with your actual NeonDB host
- Replace `your-neon-username` with your database username
- Replace `your-neon-password` with your database password
- The JWT_SECRET should be at least 32 characters
- `?sslmode=require` is mandatory for NeonDB connections

**Example with real values:**
```properties
DB_URL=jdbc:postgresql://ep-ancient-forest-a5b6c7d8.us-east-2.aws.neon.tech/lunari_users?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=npg_AbC123XyZ456
JWT_SECRET=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6
JWT_EXPIRATION=86400000
```

### 3.4 Verify .env File Location

Make sure the `.env` file is in the correct location:

```bash
ls -la .env
```

Should output:
```
-rw------- 1 user user 350 Dec 6 17:10 .env
```

---

## Step 4: Build the Application

### 4.1 Clean Previous Builds

```bash
./mvnw clean
```

### 4.2 Compile the Project

```bash
./mvnw compile
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 3.5 s
```

### 4.3 Package the Application

```bash
./mvnw package -DskipTests
```

Or with tests:
```bash
./mvnw package
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 8.2 s
```

---

## Step 5: Run the Application

### 5.1 Start the Application

```bash
./mvnw spring-boot:run
```

### 5.2 Verify Startup

Watch the console output. You should see:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.4.7)

...
Started LunariUserApiApplication in 3.456 seconds
```

**Important indicators:**
- ✅ `HikariPool-1 - Starting...` - Database connection pool initializing
- ✅ `HikariPool-1 - Start completed` - Connected to NeonDB
- ✅ `Started LunariUserApiApplication` - Application running
- ✅ `Tomcat started on port 8081` - Server ready

**If you see errors:**
- Check your `.env` file for correct credentials
- Verify NeonDB connection in the dashboard (project should be active)
- Check firewall/network connection

---

## Step 6: Access Swagger UI

### 6.1 Open Swagger UI

1. Open your web browser
2. Navigate to: http://localhost:8081/swagger-ui/index.html
3. You should see the Swagger API documentation interface

### 6.2 Explore Available Endpoints

You'll see these endpoint groups:

- **Authentication** - Public endpoints for registration and login
  - `POST /api/v1/auth/register` - Register new user
  - `POST /api/v1/auth/login` - Login and get JWT token

- **User Profile** - Protected endpoints for user management
  - `GET /api/v1/users/profile` - Get current user profile
  - `PUT /api/v1/users/profile` - Update profile
  - `POST /api/v1/users/points` - Add points

- **User Management** - Admin/protected endpoints
  - `GET /api/v1/users` - List all users (with pagination)
  - `GET /api/v1/users/{id}` - Get user by ID
  - `GET /api/v1/users/search` - Search users

---

## Step 7: Test the API

### Test 1: Register a New User

#### Using Swagger UI:

1. Expand `POST /api/v1/auth/register`
2. Click **Try it out**
3. Paste this JSON in the request body:

```json
{
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "SecurePass123!",
  "firstName": "Test",
  "lastName": "User",
  "phone": "+56912345678"
}
```

4. Click **Execute**
5. Check the response (should be `201 Created`)

#### Using cURL:

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "SecurePass123!",
    "firstName": "Test",
    "lastName": "User",
    "phone": "+56912345678"
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "username": "testuser",
    "email": "testuser@example.com",
    "personal": {
      "firstName": "Test",
      "lastName": "User",
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
    "isActive": true,
    "isVerified": false,
    "createdAt": "2025-12-06T17:15:30.123"
  },
  "timestamp": "2025-12-06T17:15:30.123"
}
```

---

### Test 2: Login

#### Using Swagger UI:

1. Expand `POST /api/v1/auth/login`
2. Click **Try it out**
3. Paste this JSON:

```json
{
  "identifier": "testuser@example.com",
  "password": "SecurePass123!"
}
```

4. Click **Execute**
5. **Copy the JWT token** from the response

#### Using cURL:

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser@example.com",
    "password": "SecurePass123!"
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTczMzUxMDEzMCwiZXhwIjoxNzMzNTk2NTMwfQ.abc123...",
    "type": "Bearer",
    "expiresIn": 86400000,
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "username": "testuser",
    "email": "testuser@example.com"
  },
  "timestamp": "2025-12-06T17:20:30.456"
}
```

**Save the token** - you'll need it for authenticated requests!

---

### Test 3: Get User Profile (Authenticated)

#### Using Swagger UI:

1. Click the **Authorize** button at the top of Swagger UI
2. Paste your JWT token (without "Bearer" prefix)
3. Click **Authorize**, then **Close**
4. Expand `GET /api/v1/users/profile`
5. Click **Try it out**, then **Execute**

#### Using cURL:

```bash
curl -X GET http://localhost:8081/api/v1/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

Replace `YOUR_JWT_TOKEN_HERE` with the actual token from the login response.

**Expected Response:**

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "username": "testuser",
    "email": "testuser@example.com",
    "personal": {
      "firstName": "Test",
      "lastName": "User",
      "phone": "+56912345678",
      "memberSince": "2025"
    },
    "gaming": null,
    "preferences": null,
    "stats": {
      "level": "Bronze",
      "points": 0,
      "purchases": 0,
      "reviews": 0,
      "favorites": 0
    },
    "coupons": [],
    "isActive": true,
    "isVerified": false,
    "createdAt": "2025-12-06T17:15:30.123"
  },
  "timestamp": "2025-12-06T17:25:00.789"
}
```

---

### Test 4: Get All Users (with Pagination)

#### Using Swagger UI:

1. Expand `GET /api/v1/users`
2. Click **Try it out**
3. Set parameters:
   - `page`: 0
   - `size`: 10
4. Click **Execute**

#### Using cURL:

```bash
curl -X GET "http://localhost:8081/api/v1/users?page=0&size=10"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    "content": [
      {
        "id": "1",
        "username": "omunoz",
        "email": "osca.munozs@duocuc.cl",
        "personal": {
          "firstName": "Oscar",
          "lastName": "Muñoz"
        },
        "isActive": true,
        "isVerified": true
      },
      {
        "id": "2",
        "username": "mgarcia",
        "email": "maria.garcia@example.com",
        "isActive": true,
        "isVerified": false
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 4,
    "totalPages": 1
  }
}
```

---

### Test 5: Search Users

#### Using Swagger UI:

1. Expand `GET /api/v1/users/search`
2. Click **Try it out**
3. Enter search query: `Oscar`
4. Click **Execute**

#### Using cURL:

```bash
curl -X GET "http://localhost:8081/api/v1/users/search?query=Oscar&page=0&size=10"
```

---

### Test 6: Update Profile (Authenticated)

#### Using Swagger UI (with Authorization):

1. Make sure you're authorized (green padlock icon)
2. Expand `PUT /api/v1/users/profile`
3. Click **Try it out**
4. Paste this JSON:

```json
{
  "firstName": "Test Updated",
  "lastName": "User Updated",
  "bio": "This is my updated bio",
  "avatar": "https://example.com/avatar.jpg",
  "gaming": {
    "gamerTag": "TestGamer123",
    "favoriteGenre": "rpg",
    "skillLevel": "intermediate"
  }
}
```

5. Click **Execute**

#### Using cURL:

```bash
curl -X PUT http://localhost:8081/api/v1/users/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "firstName": "Test Updated",
    "lastName": "User Updated",
    "bio": "This is my updated bio",
    "gaming": {
      "gamerTag": "TestGamer123",
      "favoriteGenre": "rpg",
      "skillLevel": "intermediate"
    }
  }'
```

---

## Step 8: Verify Data in NeonDB

### 8.1 Check Database

Go back to NeonDB SQL Editor and run:

```sql
-- View all users
SELECT
    id,
    username,
    email,
    personal->>'firstName' as first_name,
    personal->>'lastName' as last_name,
    gaming->>'gamerTag' as gamer_tag,
    stats->>'level' as level,
    stats->>'points' as points,
    is_active,
    is_verified,
    created_at,
    updated_at
FROM users
ORDER BY created_at DESC;
```

You should see your newly created `testuser` in the results!

### 8.2 Query JSONB Fields

```sql
-- Search by name in JSONB
SELECT username, email, personal->>'firstName' as name
FROM users
WHERE personal->>'firstName' ILIKE '%test%';

-- Get users with gaming profiles
SELECT username, gaming->>'gamerTag' as gamertag, gaming->>'skillLevel' as skill
FROM users
WHERE gaming IS NOT NULL;

-- Get user statistics
SELECT
    username,
    stats->>'level' as level,
    stats->>'points' as points,
    stats->>'purchases' as purchases
FROM users
WHERE stats IS NOT NULL
ORDER BY (stats->>'points')::int DESC;
```

---

## Step 9: Test Edge Cases

### Test Invalid Login

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser@example.com",
    "password": "WrongPassword123!"
  }'
```

Expected: `401 Unauthorized` or error message

### Test Duplicate Registration

Try registering with the same email again - should fail.

### Test Unauthorized Access

Try accessing profile without token:

```bash
curl -X GET http://localhost:8081/api/v1/users/profile
```

Expected: `401 Unauthorized`

---

## Step 10: Monitor Application Logs

### View Real-Time Logs

The terminal where you ran `./mvnw spring-boot:run` shows logs:

```
2025-12-06 17:15:30.123 INFO  c.d.l.a.u.s.UserServiceImpl : User registered: testuser@example.com
2025-12-06 17:20:30.456 INFO  c.d.l.a.u.s.UserServiceImpl : Successful authentication for user: testuser@example.com
2025-12-06 17:25:00.789 INFO  c.d.l.a.u.c.UserProfileController : Fetching profile for user ID: a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

### Common Log Messages

- ✅ `HikariPool-1 - Start completed` - Database connected
- ✅ `User registered: <email>` - User created
- ✅ `Successful authentication for user: <email>` - Login successful
- ✅ `Updated profile for user: <id>` - Profile updated
- ⚠️ `Email already exists: <email>` - Duplicate email
- ⚠️ `Invalid password for user: <email>` - Wrong password
- ❌ `User not found with ID: <id>` - Invalid user ID

---

## Troubleshooting

### Issue 1: "Connection refused" or "Connection timeout"

**Cause:** Can't connect to NeonDB

**Solutions:**
1. Check your internet connection
2. Verify NeonDB project is active (not suspended) in dashboard
3. Check `.env` file has correct host and credentials
4. Ensure `?sslmode=require` is in the connection URL
5. Try copying connection string again from NeonDB dashboard

### Issue 2: "Authentication failed for user"

**Cause:** Wrong database credentials

**Solutions:**
1. Double-check username and password in `.env`
2. Regenerate password in NeonDB dashboard if needed
3. Make sure there are no extra spaces in `.env` file
4. Verify you're using the correct database name

### Issue 3: "Table 'users' doesn't exist"

**Cause:** Migration script not run

**Solutions:**
1. Go to NeonDB SQL Editor
2. Run the entire `migration_neondb.sql` script
3. Verify table exists: `SELECT * FROM users;`

### Issue 4: "Port 8081 is already in use"

**Cause:** Another process using port 8081

**Solutions:**
1. Check if usuario service is already running
2. Kill the process: `lsof -ti:8081 | xargs kill -9`
3. Or change port in `.env`: `SERVER_PORT=8082`

### Issue 5: "JWT token expired"

**Cause:** Token validity period passed (default 24 hours)

**Solutions:**
1. Login again to get a new token
2. The token expires after `JWT_EXPIRATION` milliseconds

### Issue 6: Build fails with compilation errors

**Cause:** Code not updated properly

**Solutions:**
1. Pull latest changes: `git pull`
2. Clean build: `./mvnw clean compile`
3. Check Java version: `java -version` (should be 21)

---

## Performance Testing

### Test Response Time

```bash
time curl -X GET http://localhost:8081/api/v1/users?page=0&size=10
```

Should respond in < 200ms for local NeonDB connection.

### Test Concurrent Requests

```bash
# Run 10 concurrent login requests
for i in {1..10}; do
  curl -X POST http://localhost:8081/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"identifier":"testuser@example.com","password":"SecurePass123!"}' &
done
wait
```

---

## Cleanup

### Stop the Application

Press `Ctrl+C` in the terminal running Spring Boot

### Clear Sample Data (Optional)

If you want to remove test data:

```sql
-- In NeonDB SQL Editor
DELETE FROM users WHERE email = 'testuser@example.com';
```

### Keep Database Running

NeonDB free tier projects stay active. No need to stop anything.

---

## Next Steps

### Production Deployment

When ready for production:

1. Update `.env` with production NeonDB instance
2. Change `JWT_SECRET` to a secure random value
3. Set `spring.jpa.hibernate.ddl-auto=validate` (not `update`)
4. Enable HTTPS
5. Configure proper CORS settings
6. Set up monitoring and logging

### Connect Other Services

Once usuario service works:

1. Start inventario service (port 8081)
2. Start carrito service (port 8082)
3. Update inter-service URLs to `localhost:8081` and `localhost:8082`

---

## Useful Commands Reference

```bash
# Build and run
./mvnw clean package
./mvnw spring-boot:run

# Build without tests
./mvnw clean package -DskipTests

# Run tests only
./mvnw test

# Check if port is in use
lsof -i :8081

# Kill process on port
lsof -ti:8081 | xargs kill -9

# View logs in real-time (if running in background)
tail -f logs/application.log
```

---

## Success Checklist

- [ ] NeonDB account created
- [ ] Database and table created (migration script executed)
- [ ] `.env` file configured with correct credentials
- [ ] Application compiles successfully
- [ ] Application starts without errors
- [ ] Can access Swagger UI at http://localhost:8081/swagger-ui/index.html
- [ ] Can register a new user
- [ ] Can login and receive JWT token
- [ ] Can access authenticated endpoints with token
- [ ] Can view users in NeonDB SQL Editor
- [ ] Timestamps are automatically managed
- [ ] JSONB fields (personal, gaming, stats) work correctly

---

## Summary

You now have the usuario service running locally with a real NeonDB PostgreSQL database!

**What we tested:**
✅ Database connection to NeonDB
✅ User registration with BCrypt password hashing
✅ JWT authentication and token generation
✅ CRUD operations with JPA
✅ JSONB storage for nested objects
✅ Automatic timestamp management
✅ Pagination and search
✅ Input validation

**Ready for:**
- Local development
- Integration with other microservices
- API testing and development
- Production deployment (with proper security configurations)

Enjoy developing with LUNARi! 🌙
