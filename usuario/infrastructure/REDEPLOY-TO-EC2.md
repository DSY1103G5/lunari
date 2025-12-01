# Redeploy to EC2 - Complete Guide

Now that the app works locally, let's deploy it to EC2.

---

## Prerequisites

- ✅ Application works locally
- ✅ DynamoDB table exists (`lunari-users-dev`)
- ✅ 4 clients seeded in database
- ✅ EC2 instance running
- ✅ Security Group allows port 8080

---

## Step 1: Configure AWS Credentials on EC2

### Get AWS Academy Credentials

1. Go to **AWS Academy Learner Lab**
2. Click **AWS Details**
3. Click **Show** next to "AWS CLI:"
4. Copy the 3 lines of credentials

### SSH into EC2 and Create Credentials File

```bash
# SSH into your EC2 instance (replace with your actual host)
ssh -i ~/.ssh/your-key.pem ec2-user@ec2-XX-XX-XX-XX.compute-1.amazonaws.com
```

Once inside EC2:

```bash
# Create AWS credentials directory
mkdir -p ~/.aws

# Create credentials file
cat > ~/.aws/credentials << 'EOF'
[default]
aws_access_key_id=ASIAXXXXXXXXXXXXXXXX
aws_secret_access_key=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
aws_session_token=PASTE_YOUR_VERY_LONG_TOKEN_HERE
EOF
```

**Replace the values** with your actual AWS Academy credentials!

```bash
# Create config file
cat > ~/.aws/config << 'EOF'
[default]
region = us-east-1
output = json
EOF
```

### Verify Credentials Work

```bash
# Test AWS access from EC2
aws dynamodb describe-table --table-name lunari-users-dev --region us-east-1
```

If you see table details, credentials are working! ✅

---

## Step 2: Upload the JAR to EC2

### From Your Local Machine (WSL)

```bash
# Navigate to project root
cd /home/aframuz/code/study/duoc/3er_semestre/fullstack-i/lunari/usuario

# Upload JAR to EC2 (replace with your EC2 host and key path)
scp -i ~/.ssh/your-key.pem \
  target/lunari-user-api-0.0.1-SNAPSHOT.jar \
  ec2-user@ec2-XX-XX-XX-XX.compute-1.amazonaws.com:~/app.jar
```

---

## Step 3: Run the Application on EC2

### SSH into EC2 (if not already)

```bash
ssh -i ~/.ssh/your-key.pem ec2-user@ec2-XX-XX-XX-XX.compute-1.amazonaws.com
```

### Stop Old Application (if running)

```bash
# Kill any running Java processes
pkill -f 'java -jar' || true

# Verify it stopped
ps aux | grep java
```

### Start Application with Dev Profile

```bash
# Run with dev profile (uses lunari-users-dev table)
nohup java -jar app.jar --spring.profiles.active=dev > app.log 2>&1 &

# Check the process started
ps aux | grep java
```

### Monitor Logs

```bash
# Watch logs in real-time
tail -f app.log

# Look for this message:
# Started LunariUserApiApplication in X.XXX seconds
```

Press `Ctrl+C` to stop watching logs (app keeps running).

---

## Step 4: Test the API

### From EC2 (Internal Test)

```bash
# Test locally on EC2
curl http://localhost:8080/api/v1/users | jq

# Should return the 4 seeded clients
```

### From Your Local Machine (External Test)

```bash
# Replace with your EC2 public DNS
curl http://ec2-XX-XX-XX-XX.compute-1.amazonaws.com:8080/api/v1/users | jq
```

### Test Specific Endpoints

```bash
# Get user by ID
curl http://your-ec2-host:8080/api/v1/users/1 | jq

# Get user by email
curl http://your-ec2-host:8080/api/v1/users/email/osca.munozs@duocuc.cl | jq

# Search users
curl http://your-ec2-host:8080/api/v1/users/search?query=omunoz | jq

# Get active users
curl http://your-ec2-host:8080/api/v1/users/active | jq
```

### Access Swagger UI

Open browser: `http://your-ec2-host:8080/swagger-ui/index.html`

---

## Step 5: Verify Everything Works

You should see:

✅ **4 Clients:**
- omunoz (osca.munozs@duocuc.cl) - Gold level, 7500 points
- amillan (ang.millan@duocuc.cl) - Platinum level, 15420 points
- obadilla (obadilla@duoc.cl) - Silver level, 3200 points
- estudiante_duoc (pedro.duoc@duoc.cl) - Bronze level, 850 points

✅ **Full E-commerce Data:**
- Personal info (firstName, lastName, phone, birthdate)
- Address (addressLine1, city, country)
- Preferences (favoriteCategories, preferredPlatform)
- Gaming profile (gamerTag, favoriteGenre, skillLevel)
- Stats (level, points, purchases, reviews)
- Coupons (active and used coupons)

✅ **HATEOAS Links:**
- `_links.self` - Link to user resource
- `_links.all` - Link to all users

---

## Troubleshooting

### Error: "The security token included in the request is expired"

AWS Academy credentials expired (they last ~4 hours).

**Fix:**
1. Get new credentials from AWS Academy
2. Update `~/.aws/credentials` on EC2
3. Restart app: `pkill -f 'java -jar' && nohup java -jar app.jar --spring.profiles.active=dev > app.log 2>&1 &`

### Error: "Requested resource not found"

App is using wrong table name.

**Fix:**
Make sure you're running with `--spring.profiles.active=dev`:
```bash
pkill -f 'java -jar'
nohup java -jar app.jar --spring.profiles.active=dev > app.log 2>&1 &
```

### Error: "Connection refused" or timeout

**Check Security Group:**
- EC2 Console → Security Groups
- Find your instance's security group
- Inbound rules must allow:
  - Port 8080 from 0.0.0.0/0 (or your IP)
  - Port 22 from your IP (SSH)

### Application won't start

**Check logs:**
```bash
tail -100 app.log
```

Look for error messages about:
- Missing credentials → Update `~/.aws/credentials`
- Port already in use → Kill old process: `pkill -f 'java -jar'`
- Table not found → Check you're using dev profile

---

## Quick Reference Commands

### Upload New JAR (from local)
```bash
scp -i ~/.ssh/your-key.pem target/lunari-user-api-0.0.1-SNAPSHOT.jar \
  ec2-user@ec2-XX-XX-XX-XX.compute-1.amazonaws.com:~/app.jar
```

### Restart App (on EC2)
```bash
pkill -f 'java -jar'
nohup java -jar app.jar --spring.profiles.active=dev > app.log 2>&1 &
tail -f app.log
```

### Check App Status
```bash
ps aux | grep java
curl http://localhost:8080/api/v1/users
```

### View Logs
```bash
tail -f app.log           # Real-time
tail -100 app.log         # Last 100 lines
grep ERROR app.log        # Only errors
```

---

## One-Liner Deployment Script

Create this script on your **local machine**:

```bash
#!/bin/bash
# deploy.sh - Quick redeploy to EC2

EC2_HOST="ec2-user@ec2-XX-XX-XX-XX.compute-1.amazonaws.com"
SSH_KEY="~/.ssh/your-key.pem"

echo "Uploading JAR..."
scp -i $SSH_KEY target/lunari-user-api-0.0.1-SNAPSHOT.jar $EC2_HOST:~/app.jar

echo "Restarting application..."
ssh -i $SSH_KEY $EC2_HOST "pkill -f 'java -jar' || true; nohup java -jar app.jar --spring.profiles.active=dev > app.log 2>&1 &"

echo "Waiting for startup..."
sleep 10

echo "Testing API..."
ssh -i $SSH_KEY $EC2_HOST "curl -s http://localhost:8080/api/v1/users | head -20"

echo "✅ Deployment complete!"
echo "Access: http://ec2-XX-XX-XX-XX.compute-1.amazonaws.com:8080/api/v1/users"
```

Usage:
```bash
chmod +x deploy.sh
./deploy.sh
```

---

**🎉 Deployment Complete!**

Your LUNARi User Service is now running on EC2 with:
- Full e-commerce client data
- DynamoDB backend
- HATEOAS REST API
- Swagger documentation

