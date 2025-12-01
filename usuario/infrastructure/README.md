# Infrastructure and Deployment Resources

This directory contains all the infrastructure and deployment resources for the LUNARi User Service with DynamoDB.

## Directory Contents

### CloudFormation Templates
- **`dynamodb-table.yaml`** - CloudFormation template for DynamoDB table creation
  - Creates table with EmailIndex and RoleActiveIndex GSIs
  - Configurable billing mode (PAY_PER_REQUEST or PROVISIONED)
  - Includes encryption, streaming, and point-in-time recovery

### Deployment Scripts
- **`deploy-dynamodb.sh`** - Automated CloudFormation deployment script
  - Validates template
  - Creates or updates stack
  - Supports multiple environments (dev, staging, prod)

- **`quickstart.sh`** - Quick start script for local development
  - Starts LocalStack
  - Builds application
  - Launches application with local profile

### LocalStack Setup
- **`docker-compose.localstack.yml`** - Docker Compose for local AWS environment
  - LocalStack container for DynamoDB
  - DynamoDB Admin UI for data visualization

- **`init-localstack.sh`** - LocalStack initialization script
  - Creates DynamoDB table automatically
  - Sets up test data
  - Configures GSIs

### Data Migration
- **`migrate-data.py`** - Python script for PostgreSQL to DynamoDB migration
  - Reads from PostgreSQL
  - Transforms data to DynamoDB format
  - Writes to DynamoDB with error handling
  - Supports dry-run mode

- **`requirements.txt`** - Python dependencies for migration script
- **`.env.example`** - Example environment configuration for migration

### Documentation
- **`DEPLOYMENT.md`** - Comprehensive deployment guide
  - Local development setup
  - AWS deployment instructions
  - Data migration guide
  - Troubleshooting tips

## Quick Start

### 1. Local Development

```bash
# Start LocalStack and application
./quickstart.sh
```

Access:
- API: http://localhost:8080
- DynamoDB Admin: http://localhost:8001
- Swagger UI: http://localhost:8080/swagger-ui.html

### 2. Deploy to AWS

```bash
# Deploy DynamoDB table to dev environment
./deploy-dynamodb.sh dev

# Deploy to production
./deploy-dynamodb.sh prod
```

### 3. Migrate Data

```bash
# Install dependencies
pip install -r requirements.txt

# Copy and configure environment
cp .env.example .env
# Edit .env with your database credentials

# Test migration (dry run)
python migrate-data.py --dry-run

# Run migration
python migrate-data.py
```

## File Permissions

All `.sh` scripts are executable. If you need to make them executable:

```bash
chmod +x *.sh
```

## Environment Variables

### For Application

| Variable | Description | Default |
|----------|-------------|---------|
| `AWS_REGION` | AWS region | us-east-1 |
| `DYNAMODB_TABLE_NAME` | DynamoDB table name | lunari-users-dev |
| `SPRING_PROFILES_ACTIVE` | Spring profile | local |

### For Migration

| Variable | Description |
|----------|-------------|
| `POSTGRES_HOST` | PostgreSQL host |
| `POSTGRES_PORT` | PostgreSQL port |
| `POSTGRES_DB` | PostgreSQL database |
| `POSTGRES_USER` | PostgreSQL user |
| `POSTGRES_PASSWORD` | PostgreSQL password |
| `AWS_REGION` | AWS region |
| `DYNAMODB_TABLE_NAME` | Target DynamoDB table |
| `DYNAMODB_ENDPOINT` | Optional: LocalStack endpoint |

## Resources

- **DynamoDB Best Practices**: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/best-practices.html
- **CloudFormation User Guide**: https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/
- **LocalStack Documentation**: https://docs.localstack.cloud/
- **Boto3 Documentation**: https://boto3.amazonaws.com/v1/documentation/api/latest/index.html

## Support

For detailed deployment instructions, see [DEPLOYMENT.md](./DEPLOYMENT.md).

For application documentation, see the main [README.md](../README.md).
