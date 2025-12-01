# EC2 Deployment Guide - Quick Reference

## The Issue You Had (Now Fixed!)

**Problem**: The app required a `.env` file which doesn't exist on EC2.

**Solution**: Updated the code to make `.env` optional (line 14 in `LunariUserApiApplication.java`):
```java
Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
```

Now the app works both locally (with `.env`) and on AWS (with environment variables/IAM roles).

---

## Deployment Steps

### Step 1: Upload the Fixed JAR to EC2

**Option A: Use the deployment script**
```bash
cd usuario
./deploy-to-ec2.sh
# Follow the prompts
```

**Option B: Manual upload**
```bash
cd usuario
scp -i /path/to/your-key.pem \
    target/lunari-user-api-0.0.1-SNAPSHOT.jar \
    ec2-user@<YOUR-EC2-IP>:~/
```

### Step 2: Setup EC2 Instance

**Connect to EC2:**
```bash
ssh -i /path/to/your-key.pem ec2-user@<YOUR-EC2-IP>
```

**Install Java 21:**
```bash
sudo yum install java-21-amazon-corretto-headless -y
java -version  # Verify installation
```

### Step 3: Configure IAM Role for DynamoDB Access

Your EC2 instance needs permission to access DynamoDB.

**Via AWS Console:**
1. Go to **EC2** → **Instances** → Select your instance
2. **Actions** → **Security** → **Modify IAM Role**
3. Attach a role with `AmazonDynamoDBFullAccess` policy
   - Or create a custom policy with these permissions:
     ```json
     {
       "Version": "2012-10-17",
       "Statement": [{
         "Effect": "Allow",
         "Action": [
           "dynamodb:PutItem",
           "dynamodb:GetItem",
           "dynamodb:UpdateItem",
           "dynamodb:DeleteItem",
           "dynamodb:Query",
           "dynamodb:Scan"
         ],
         "Resource": [
           "arn:aws:dynamodb:us-east-1:*:table/lunari-users-dev",
           "arn:aws:dynamodb:us-east-1:*:table/lunari-users-dev/index/*"
         ]
       }]
     }
     ```

**Why?** With IAM roles, your app automatically gets AWS credentials without needing to configure access keys!

### Step 4: Run the Application

**On your EC2 instance:**

**Option A: Run in foreground (for testing)**
```bash
java -jar lunari-user-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=dev \
  --aws.region=us-east-1 \
  --aws.dynamodb.tableName=lunari-users-dev
```

Press `Ctrl+C` to stop.

**Option B: Run in background (recommended)**
```bash
nohup java -jar lunari-user-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=dev \
  --aws.region=us-east-1 \
  --aws.dynamodb.tableName=lunari-users-dev \
  > app.log 2>&1 &

# Get the process ID
echo $!
```

### Step 5: Test Your Deployment

**From your local machine:**
```bash
# Health check
curl http://<YOUR-EC2-IP>:8080/actuator/health

# Should return: {"status":"UP"}

# Get API root
curl http://<YOUR-EC2-IP>:8080/api/v1

# View Swagger UI in browser
http://<YOUR-EC2-IP>:8080/swagger-ui/index.html
```

---

## Managing Your Application

### View Logs
```bash
# Real-time logs
tail -f app.log

# Last 100 lines
tail -n 100 app.log

# Search for errors
grep -i error app.log
```

### Stop the Application
```bash
# Find the process
ps aux | grep lunari-user-api

# Kill it (replace PID with actual process ID)
kill <PID>

# Force kill if needed
kill -9 <PID>
```

### Restart the Application
```bash
# Stop it
kill $(ps aux | grep lunari-user-api | grep -v grep | awk '{print $2}')

# Start it again
nohup java -jar lunari-user-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=dev > app.log 2>&1 &
```

### Auto-start on Boot (Optional)

Create a systemd service:

```bash
sudo nano /etc/systemd/system/lunari-user-api.service
```

Add this content:
```ini
[Unit]
Description=LUNARi User API Service
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user
ExecStart=/usr/bin/java -jar /home/ec2-user/lunari-user-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev --aws.region=us-east-1 --aws.dynamodb.tableName=lunari-users-dev
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable lunari-user-api
sudo systemctl start lunari-user-api

# Check status
sudo systemctl status lunari-user-api

# View logs
sudo journalctl -u lunari-user-api -f
```

---

## Security Checklist

- [ ] EC2 Security Group allows port 8080 from your IP or specific IPs only
- [ ] IAM role attached to EC2 with minimal DynamoDB permissions
- [ ] SSH port (22) restricted to your IP only
- [ ] Consider using Application Load Balancer for HTTPS
- [ ] Set up CloudWatch alarms for high CPU/memory usage
- [ ] Regular security updates: `sudo yum update -y`

---

## Troubleshooting

### "Connection refused"
- Check security group allows port 8080
- Verify app is running: `ps aux | grep java`
- Check logs: `tail -f app.log`

### "No permissions for DynamoDB"
- Verify IAM role is attached to EC2 instance
- Check role has DynamoDB permissions
- Try: `aws dynamodb list-tables --region us-east-1` from EC2

### "Table not found"
- Verify DynamoDB table exists: `aws dynamodb list-tables`
- Check table name matches: `lunari-users-dev`
- Verify region is correct: `us-east-1`

### High memory usage
- Monitor with: `top` or `htop`
- Consider increasing instance size (t2.small, t2.medium)
- Or add swap space (for t2.micro)

---

## Cost Optimization

**Estimated Monthly Costs:**
- **t2.micro**: Free tier eligible (12 months), then ~$8/month
- **DynamoDB**: $0.01-$5/month (pay-per-request)
- **Data transfer**: Minimal for testing

**To reduce costs:**
- Use t2.micro instance
- Stop instance when not in use (storage still charged)
- Use DynamoDB on-demand billing (default)
- Set up billing alerts in AWS Console

---

## Next Steps

1. ✅ Fixed the `.env` issue
2. ✅ Rebuilt the JAR
3. 🔄 Upload to EC2 (use `./deploy-to-ec2.sh`)
4. 🔄 Configure IAM role
5. 🔄 Run the app
6. 🔄 Test endpoints

Your app is ready to deploy! 🚀
