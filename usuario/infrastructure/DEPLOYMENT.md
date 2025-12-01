# LUNARi User Service - Deployment Guide

This guide covers deploying the User Service with DynamoDB backend.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Local Development with LocalStack](#local-development-with-localstack)
- [AWS Deployment](#aws-deployment)
- [Data Migration](#data-migration)
- [Environment Configuration](#environment-configuration)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Tools

1. **Java 21** (for running the application)
2. **Maven 3.9+** (for building)
3. **Docker** and **Docker Compose** (for LocalStack)
4. **AWS CLI** (for AWS deployment)
5. **Python 3.8+** (for data migration script)

### AWS Account Setup

1. Create an AWS account if you don't have one
2. Install and configure AWS CLI:
   ```bash
   aws configure
   ```
3. Ensure you have permissions to:
   - Create DynamoDB tables
   - Create CloudFormation stacks
   - Use IAM roles (for production)

---

## Local Development with LocalStack

LocalStack provides a local AWS environment for development and testing.

### 1. Start LocalStack

```bash
cd infrastructure
docker-compose -f docker-compose.localstack.yml up -d
```

This will start:
- **LocalStack** at `http://localhost:4566`
- **DynamoDB Admin UI** at `http://localhost:8001`

The initialization script automatically creates:
- `lunari-users-dev` table with GSIs
- A test admin user

### 2. Verify Setup

Check the table was created:
```bash
aws --endpoint-url=http://localhost:4566 dynamodb list-tables
```

View data in DynamoDB Admin:
```
http://localhost:8001
```

### 3. Run the Application

```bash
cd ..
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The application will connect to LocalStack DynamoDB.

### 4. Test the API

```bash
# Health check
curl http://localhost:8080/actuator/health

# Get test user
curl http://localhost:8080/api/v1/users/00000000-0000-0000-0000-000000000001

# Get all roles
curl http://localhost:8080/api/v1/roles
```

### 5. Stop LocalStack

```bash
docker-compose -f docker-compose.localstack.yml down
```

To preserve data, keep the `localstack-data` directory.

---

## AWS Deployment

### 1. Deploy DynamoDB Table

The CloudFormation template creates a production-ready DynamoDB table.

#### Development Environment

```bash
cd infrastructure
chmod +x deploy-dynamodb.sh

# Deploy to dev (PAY_PER_REQUEST billing)
./deploy-dynamodb.sh dev
```

#### Production Environment

```bash
# Deploy to prod
./deploy-dynamodb.sh prod

# Or with custom billing mode
BILLING_MODE=PROVISIONED ./deploy-dynamodb.sh prod
```

#### Manual Deployment

```bash
aws cloudformation create-stack \
    --stack-name lunari-users-dynamodb-dev \
    --template-body file://dynamodb-table.yaml \
    --parameters \
        ParameterKey=Environment,ParameterValue=dev \
        ParameterKey=BillingMode,ParameterValue=PAY_PER_REQUEST \
    --region us-east-1
```

### 2. Verify Table Creation

```bash
# List tables
aws dynamodb list-tables --region us-east-1

# Describe table
aws dynamodb describe-table \
    --table-name lunari-users-dev \
    --region us-east-1
```

### 3. Deploy Application

#### Option A: AWS Elastic Beanstalk

```bash
# Build JAR
mvn clean package -DskipTests

# Create Elastic Beanstalk application
eb init -p java-21 lunari-user-service --region us-east-1

# Create environment
eb create lunari-users-dev \
    --envvars SPRING_PROFILES_ACTIVE=dev,AWS_REGION=us-east-1,DYNAMODB_TABLE_NAME=lunari-users-dev

# Deploy
eb deploy
```

#### Option B: AWS ECS/Fargate

Create a Dockerfile:
```dockerfile
FROM eclipse-temurin:21-jre
COPY target/usuario-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and push to ECR, then deploy using ECS.

#### Option C: EC2 Instance

```bash
# SSH into EC2 instance
ssh -i your-key.pem ec2-user@your-instance

# Install Java 21
sudo yum install java-21-amazon-corretto

# Copy JAR file
scp target/usuario-*.jar ec2-user@your-instance:~/

# Run application
java -jar usuario-*.jar --spring.profiles.active=dev
```

---

## Data Migration

Migrate existing users from PostgreSQL to DynamoDB.

### 1. Setup Migration Environment

```bash
cd infrastructure

# Install Python dependencies
pip install psycopg2-binary boto3 python-dotenv
```

### 2. Configure Environment

Create `.env` file:
```env
# PostgreSQL (source)
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=lunari_users
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password

# DynamoDB (target)
AWS_REGION=us-east-1
DYNAMODB_TABLE_NAME=lunari-users-dev

# Optional: For LocalStack
# DYNAMODB_ENDPOINT=http://localhost:4566
```

### 3. Test Migration (Dry Run)

```bash
python migrate-data.py --dry-run
```

This validates the migration without writing to DynamoDB.

### 4. Run Migration

```bash
python migrate-data.py
```

The script will:
- Connect to PostgreSQL
- Read all users
- Transform data to DynamoDB format
- Write to DynamoDB table
- Report success/failure statistics

### 5. Verify Migration

```bash
# Count items in DynamoDB
aws dynamodb scan \
    --table-name lunari-users-dev \
    --select COUNT \
    --region us-east-1

# Query by email
aws dynamodb query \
    --table-name lunari-users-dev \
    --index-name EmailIndex \
    --key-condition-expression "email = :email" \
    --expression-attribute-values '{":email":{"S":"admin@lunari.com"}}' \
    --region us-east-1
```

---

## Environment Configuration

### Environment Variables

Configure based on deployment environment:

#### Development (AWS)
```bash
export SPRING_PROFILES_ACTIVE=dev
export AWS_REGION=us-east-1
export DYNAMODB_TABLE_NAME=lunari-users-dev
```

#### Production
```bash
export SPRING_PROFILES_ACTIVE=prod
export AWS_REGION=us-east-1
export DYNAMODB_TABLE_NAME=lunari-users-prod
```

#### Local (LocalStack)
```bash
export SPRING_PROFILES_ACTIVE=local
export AWS_REGION=us-east-1
export DYNAMODB_TABLE_NAME=lunari-users-dev
```

### IAM Permissions

The application needs these DynamoDB permissions:

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
        "dynamodb:Scan"
      ],
      "Resource": [
        "arn:aws:dynamodb:us-east-1:*:table/lunari-users-*",
        "arn:aws:dynamodb:us-east-1:*:table/lunari-users-*/index/*"
      ]
    }
  ]
}
```

---

## Troubleshooting

### Issue: Cannot connect to DynamoDB

**Solution 1: Check AWS credentials**
```bash
aws sts get-caller-identity
```

**Solution 2: Verify table exists**
```bash
aws dynamodb describe-table --table-name lunari-users-dev
```

**Solution 3: Check application.properties**
Ensure correct region and table name are set.

### Issue: LocalStack not working

**Solution 1: Restart LocalStack**
```bash
docker-compose -f docker-compose.localstack.yml restart
```

**Solution 2: Check logs**
```bash
docker logs lunari-localstack
```

**Solution 3: Verify endpoint**
```bash
curl http://localhost:4566/_localstack/health
```

### Issue: Migration fails

**Solution 1: Check PostgreSQL connection**
```bash
psql -h localhost -U postgres -d lunari_users -c "SELECT COUNT(*) FROM users;"
```

**Solution 2: Verify DynamoDB table**
Ensure table exists and has correct schema.

**Solution 3: Check migration logs**
Review error messages in the script output.

### Issue: High DynamoDB costs

**Solution 1: Switch to PAY_PER_REQUEST**
Better for unpredictable workloads.

**Solution 2: Monitor usage**
```bash
aws cloudwatch get-metric-statistics \
    --namespace AWS/DynamoDB \
    --metric-name ConsumedReadCapacityUnits \
    --dimensions Name=TableName,Value=lunari-users-dev \
    --start-time 2025-01-01T00:00:00Z \
    --end-time 2025-01-31T23:59:59Z \
    --period 3600 \
    --statistics Sum
```

**Solution 3: Use LocalStack for development**
Avoid AWS costs during development.

---

## Monitoring and Maintenance

### CloudWatch Metrics

Monitor these metrics:
- `ConsumedReadCapacityUnits`
- `ConsumedWriteCapacityUnits`
- `UserErrors`
- `SystemErrors`

### Backup and Recovery

Enable Point-in-Time Recovery:
```bash
aws dynamodb update-continuous-backups \
    --table-name lunari-users-prod \
    --point-in-time-recovery-specification \
        PointInTimeRecoveryEnabled=true
```

Create on-demand backup:
```bash
aws dynamodb create-backup \
    --table-name lunari-users-prod \
    --backup-name lunari-users-backup-$(date +%Y%m%d)
```

---

## Support

For issues or questions:
- Check the [main README](../README.md)
- Review [DynamoDB Best Practices](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/best-practices.html)
- Contact: osca.munozs@duocuc.cl
