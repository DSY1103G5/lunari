# Fix: Expired Security Token Error

## Problem

```
The security token included in the request is expired
(Service: DynamoDb, Status Code: 400)
```

This happens because your EC2 instance doesn't have an IAM role with DynamoDB permissions.

---

## Solution: Attach IAM Role to EC2

### Option 1: Using the Setup Script (Recommended)

```bash
cd infrastructure

# Find your EC2 Instance ID from AWS Console or:
aws ec2 describe-instances \
  --filters "Name=instance-state-name,Values=running" \
  --query 'Reservations[*].Instances[*].[InstanceId,Tags[?Key==`Name`].Value|[0]]' \
  --output table

# Run the setup script
./setup-ec2-iam-role.sh i-XXXXXXXXXXXXX dev

# SSH into EC2 and restart the app
ssh -i ~/.ssh/your-key.pem ec2-user@your-ec2-instance.compute-1.amazonaws.com

# Restart the application
pkill -f 'java -jar'
AWS_REGION=us-east-1 AWS_DYNAMODB_TABLE_NAME=lunari-users-dev nohup java -jar app.jar > app.log 2>&1 &

# Check logs
tail -f app.log
```

---

### Option 2: Using AWS Console (Manual)

1. **Create IAM Role:**
   - Go to IAM Console → Roles → Create Role
   - Select **EC2** as trusted entity
   - Click **Next**

2. **Create Custom Policy:**
   - Click **Create Policy**
   - Select **JSON** tab
   - Paste the contents of `infrastructure/ec2-dynamodb-policy.json`:
     ```json
     {
         "Version": "2012-10-17",
         "Statement": [
             {
                 "Effect": "Allow",
                 "Action": [
                     "dynamodb:PutItem",
                     "dynamodb:GetItem",
                     "dynamodb:UpdateItem",
                     "dynamodb:DeleteItem",
                     "dynamodb:Query",
                     "dynamodb:Scan",
                     "dynamodb:BatchGetItem",
                     "dynamodb:BatchWriteItem",
                     "dynamodb:DescribeTable"
                 ],
                 "Resource": [
                     "arn:aws:dynamodb:us-east-1:*:table/lunari-users-*",
                     "arn:aws:dynamodb:us-east-1:*:table/lunari-users-*/index/*"
                 ]
             }
         ]
     }
     ```
   - Name it: `LunariDynamoDBAccess-dev`
   - Click **Create Policy**

3. **Attach Policy to Role:**
   - Go back to role creation
   - Search for and select `LunariDynamoDBAccess-dev`
   - Name the role: `LunariUserServiceEC2Role-dev`
   - Click **Create Role**

4. **Attach Role to EC2 Instance:**
   - Go to EC2 Console
   - Select your instance
   - Actions → Security → Modify IAM Role
   - Select `LunariUserServiceEC2Role-dev`
   - Click **Update IAM Role**

5. **Restart Application:**
   - SSH into EC2
   - Restart the app:
     ```bash
     pkill -f 'java -jar'
     AWS_REGION=us-east-1 AWS_DYNAMODB_TABLE_NAME=lunari-users-dev nohup java -jar app.jar > app.log 2>&1 &
     ```

---

## Verify It Works

```bash
# Test the API
curl http://your-ec2-instance.compute-1.amazonaws.com:8080/api/v1/users

# Should return the 4 seeded clients:
# - omunoz (osca.munozs@duocuc.cl)
# - amillan (ang.millan@duocuc.cl)
# - obadilla (obadilla@duoc.cl)
# - estudiante_duoc (pedro.duoc@duoc.cl)
```

---

## How IAM Roles Work

**Before (Expired Token Error):**
- EC2 instance tries to use AWS credentials from environment/config
- Those credentials expire after a few hours
- DynamoDB rejects requests with "expired token" error

**After (IAM Role):**
- EC2 automatically gets temporary credentials from AWS
- Credentials auto-refresh every hour
- No manual credential management needed
- More secure (no hardcoded credentials)

---

## Files Created

- `infrastructure/ec2-dynamodb-policy.json` - IAM policy for DynamoDB access
- `infrastructure/setup-ec2-iam-role.sh` - Automated setup script
- `infrastructure/FIX-EXPIRED-TOKEN.md` - This guide

---

## Troubleshooting

### Error: "User is not authorized to perform: iam:CreateRole"

Your AWS CLI user needs IAM permissions. Run the script with an admin account:

```bash
aws configure --profile admin
AWS_PROFILE=admin ./setup-ec2-iam-role.sh i-XXXXXXXXXXXXX dev
```

### Error: "Access Denied" after attaching role

Wait 30-60 seconds for IAM changes to propagate, then restart the app.

### Application still shows expired token error

1. Verify role is attached:
   ```bash
   aws ec2 describe-instances --instance-ids i-XXXXXXXXXXXXX \
     --query 'Reservations[0].Instances[0].IamInstanceProfile'
   ```

2. SSH into EC2 and verify instance metadata:
   ```bash
   curl http://169.254.169.254/latest/meta-data/iam/security-credentials/
   ```

3. Make sure you restarted the application after attaching the role

---

**Problem solved!** Your EC2 instance can now access DynamoDB without credential issues.
