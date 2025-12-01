# Local Testing with AWS DynamoDB

Test the application locally before deploying to EC2.

---

## Step 1: Configure AWS Credentials Locally

### Get AWS Academy Credentials

1. Go to **AWS Academy Learner Lab**
2. Click **AWS Details**
3. Click **Show** next to "AWS CLI:"
4. Copy the credentials

### Set Up Local Credentials

```bash
# Create AWS credentials directory (if not exists)
mkdir -p ~/.aws

# Edit credentials file
nano ~/.aws/credentials
```

Paste your AWS Academy credentials:
```
[default]
aws_access_key_id=ASIAXXXXXXXXXXXXXXXX
aws_secret_access_key=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
aws_session_token=VERY_LONG_TOKEN_HERE...
```

Save: `Ctrl+X`, `Y`, `Enter`

### Set Region

```bash
nano ~/.aws/config
```

Paste:
```
[default]
region = us-east-1
output = json
```

Save: `Ctrl+X`, `Y`, `Enter`

### Verify Credentials Work

```bash
aws dynamodb describe-table --table-name lunari-users-dev --region us-east-1
```

Should show table details without errors.

---

## Step 2: Set Environment Variables

```bash
cd /home/aframuz/code/study/duoc/3er_semestre/fullstack-i/lunari/usuario

# Set required environment variables
export AWS_REGION=us-east-1
export AWS_DYNAMODB_TABLE_NAME=lunari-users-dev
```

---

## Step 3: Run the Application Locally

```bash
# Make sure you're in the usuario directory
cd /home/aframuz/code/study/duoc/3er_semestre/fullstack-i/lunari/usuario

# Run with Maven wrapper
./mvnw spring-boot:run
```

Wait for the application to start. You should see:
```
Started LunariUserApiApplication in X.XXX seconds
```

---

## Step 4: Test the Endpoints

Open a **new terminal** and test:

### Get All Users
```bash
curl http://localhost:8080/api/v1/users | jq
```

Should return the 4 seeded clients with HATEOAS links.

### Get User by ID
```bash
curl http://localhost:8080/api/v1/users/1 | jq
```

Should return omunoz's full profile.

### Get User by Email
```bash
curl http://localhost:8080/api/v1/users/email/osca.munozs@duocuc.cl | jq
```

### Search Users
```bash
curl http://localhost:8080/api/v1/users/search?query=omunoz | jq
```

### Get Active Users
```bash
curl http://localhost:8080/api/v1/users/active | jq
```

### Access Swagger UI
Open browser: http://localhost:8080/swagger-ui/index.html

---

## Step 5: Verify Data Structure

The response should have the e-commerce client structure:

```json
{
  "id": "1",
  "username": "omunoz",
  "email": "osca.munozs@duocuc.cl",
  "personal": {
    "firstName": "Oscar",
    "lastName": "Munoz",
    "phone": "+56 9 1234 5678",
    "birthdate": "1993-04-27",
    "memberSince": "2022"
  },
  "address": {
    "addressLine1": "Av. Providencia 1234",
    "city": "Santiago",
    "country": "chile"
  },
  "preferences": {
    "favoriteCategories": ["JM", "CG", "AC"],
    "preferredPlatform": "pc",
    "notifyOffers": true
  },
  "gaming": {
    "gamerTag": "OMunoz93",
    "favoriteGenre": "rpg",
    "skillLevel": "advanced"
  },
  "stats": {
    "level": "Gold",
    "points": 7500,
    "purchases": 24,
    "reviews": 12,
    "favorites": 8
  },
  "coupons": [
    {
      "id": "COUP-001",
      "code": "BRONZE-ALX-001",
      "description": "Cupón Bronze - $1.000 de descuento",
      "type": "fixed",
      "value": 1000,
      "isUsed": false
    }
  ],
  "_links": {
    "self": { "href": "http://localhost:8080/api/v1/users/1" },
    "all": { "href": "http://localhost:8080/api/v1/users" }
  }
}
```

---

## Troubleshooting

### Error: "The security token included in the request is expired"

Your AWS Academy credentials expired. Update them:
```bash
nano ~/.aws/credentials
# Paste new credentials from AWS Academy
# Restart the app
```

### Error: "Unable to locate credentials"

AWS credentials not configured. Repeat Step 1.

### Error: "Port 8080 already in use"

Another process is using port 8080:
```bash
# Find and kill the process
lsof -ti:8080 | xargs kill -9

# Or change the port
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Application starts but hangs on requests

Check the logs in the terminal where you ran `./mvnw spring-boot:run`. Look for exceptions or connection errors.

### Error: "ResourceNotFoundException: Table not found"

The DynamoDB table doesn't exist. Create it:
```bash
cd infrastructure
./deploy-dynamodb.sh dev
```

---

## Stop the Application

In the terminal where the app is running:
- Press `Ctrl+C`

---

## Next Steps

Once local testing works:
1. ✅ Application connects to DynamoDB
2. ✅ Repository works correctly
3. ✅ Seeded data can be retrieved
4. ✅ HATEOAS links are generated
5. 🔄 Ready to deploy to EC2 with confidence!

