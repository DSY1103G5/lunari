# AWS Academy Setup (No IAM Role Required)

Since AWS Academy doesn't allow creating IAM roles, we'll use temporary credentials directly.

---

## Quick Setup (Manual - Recommended)

### Step 1: Get AWS Academy Credentials

1. Go to **AWS Academy Learner Lab**
2. Click **AWS Details** (top right)
3. Click **Show** next to "AWS CLI:"
4. You'll see credentials like:
   ```
   [default]
   aws_access_key_id=ASIAXXXXXXXXXXXXXXXX
   aws_secret_access_key=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   aws_session_token=VERY_LONG_TOKEN_HERE...
   ```
5. Copy ALL THREE lines

### Step 2: SSH into EC2

```bash
ssh -i ~/.ssh/your-key.pem ec2-user@your-ec2-instance.compute-1.amazonaws.com
```

### Step 3: Create AWS Credentials File

```bash
# Create .aws directory
mkdir -p ~/.aws

# Create credentials file
nano ~/.aws/credentials
```

Paste the THREE lines from AWS Academy:
```
[default]
aws_access_key_id=ASIAXXXXXXXXXXXXXXXX
aws_secret_access_key=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
aws_session_token=VERY_LONG_TOKEN_HERE...
```

Press `Ctrl+X`, then `Y`, then `Enter` to save.

### Step 4: Create Config File

```bash
nano ~/.aws/config
```

Paste:
```
[default]
region = us-east-1
output = json
```

Press `Ctrl+X`, then `Y`, then `Enter` to save.

### Step 5: Restart Application

```bash
# Stop old app
pkill -f 'java -jar'

# Start app (it will now use the credentials)
nohup java -jar app.jar > app.log 2>&1 &

# Watch logs
tail -f app.log
```

### Step 6: Test the API

```bash
# From your local machine
curl http://your-ec2-instance.compute-1.amazonaws.com:8080/api/v1/users
```

Should return the 4 seeded clients!

---

## Alternative: Automated Script

If you prefer automation:

```bash
cd infrastructure

# Run the setup script
./setup-aws-academy-credentials.sh ec2-user@your-ec2-instance.compute-1.amazonaws.com

# When prompted, paste your AWS Academy credentials
# (the 3 lines: aws_access_key_id, aws_secret_access_key, aws_session_token)
```

---

## ⚠️ Important Notes

### Credentials Expire

AWS Academy credentials expire after **4 hours**. When they expire:

1. Go back to AWS Academy Learner Lab
2. Click "Start Lab" (if stopped)
3. Get new credentials from "AWS Details"
4. Repeat Step 3 above (update `~/.aws/credentials` on EC2)
5. Restart the app

### Check if Credentials Work

```bash
# SSH into EC2
ssh ec2-user@your-ec2-instance.compute-1.amazonaws.com

# Test AWS access
aws dynamodb describe-table --table-name lunari-users-dev --region us-east-1
```

If it works, your credentials are valid!

---

## Troubleshooting

### Error: "The security token included in the request is expired"

Your AWS Academy credentials expired. Get fresh credentials:

1. AWS Academy → AWS Details → Show
2. Copy new credentials
3. Update `~/.aws/credentials` on EC2
4. Restart the app

### Error: "Unable to locate credentials"

The credentials file doesn't exist or is in the wrong location. Make sure:
- File is at `~/.aws/credentials` (not `/root/.aws/credentials`)
- You're logged in as `ec2-user` (not root)
- File contains all 3 lines (key_id, secret_key, session_token)

### Application doesn't see credentials

Make sure you:
1. Created the file as the same user running the app
2. Restarted the application after creating credentials
3. The file has correct permissions: `chmod 600 ~/.aws/credentials`

---

## Why This Works

**AWS SDK Credential Chain:**

The AWS SDK looks for credentials in this order:
1. ✅ **Environment variables** (not set)
2. ✅ **AWS credentials file** (`~/.aws/credentials`) ← We use this
3. ✅ **IAM role** (not available in AWS Academy)

Since we can't use IAM roles in AWS Academy, we use the credentials file instead.

---

**Quick recap:**
1. Get credentials from AWS Academy
2. Put them in `~/.aws/credentials` on EC2
3. Restart the app
4. Refresh credentials every 4 hours

That's it! 🎉
