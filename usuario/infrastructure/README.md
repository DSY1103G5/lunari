# LUNARi Infrastructure

This directory contains deployment scripts, configurations, and guides for deploying the LUNARi microservices to various cloud platforms.

## Directory Structure

```
infrastructure/
├── README.md                          # This file - Overview and deployment options
├── DEPLOYMENT_SUMMARY.md              # Complete deployment summary
│
├── GCP Deployment
│   ├── GCP_DEPLOYMENT_GUIDE.md        # Comprehensive GCP guide
│   ├── GCP_QUICK_START.md             # Quick start (30 min setup)
│   ├── deploy-to-gcp.sh               # Automated deployment script
│   └── NEONDB_VS_CLOUDSQL.md          # Database comparison
│
├── Custom Domain Setup
│   ├── DOMAIN_SETUP_QUICK_START.md    # Quick start (5 steps)
│   ├── LOAD_BALANCER_SETUP.md         # Detailed load balancer guide
│   ├── CUSTOM_SSL_CERTIFICATE.md      # Use your own SSL certificate
│   ├── setup-load-balancer.sh         # Automated setup script
│   └── CUSTOM_DOMAIN_SETUP.md         # Legacy approach (deprecated)
│
├── Database Setup
│   ├── setup-neondb-secrets.sh        # NeonDB secrets helper
│   └── setup-database/                # Database seeding scripts
│       ├── seed-database.sh
│       ├── seed-database.py
│       ├── seed-database.js
│       ├── migrate-data.py
│       ├── seed-data.json
│       └── requirements.txt
│
└── Other Platforms
    ├── deploy-to-ec2.sh               # AWS EC2 deployment
    ├── update-aws-credentials.sh      # AWS credentials helper
    └── k8s-usuario-deployment.yaml    # Kubernetes manifest
```

## Available Deployment Options

### 1. Google Cloud Platform (Recommended)

**Best for**: Production deployments, auto-scaling, serverless architecture

**Deployment Options**:
- **Cloud Run** (Serverless, recommended for beginners)
- **Google Kubernetes Engine (GKE)** (Advanced, full control)

**Get Started**:
- **Quick Start** (30 minutes): See [GCP_QUICK_START.md](./GCP_QUICK_START.md)
- **Full Guide** (comprehensive): See [GCP_DEPLOYMENT_GUIDE.md](./GCP_DEPLOYMENT_GUIDE.md)

**Quick Deploy**:
```bash
# Automated deployment
./deploy-to-gcp.sh your-project-id us-central1
```

**Cost Estimate**: $10-30/month for development, $100-150/month for production

---

### 2. Amazon Web Services (AWS)

**Best for**: AWS-centric organizations, EC2-based deployments

**Deployment Target**: EC2 instances

**Get Started**:
```bash
# Build JAR first
cd ../
mvn clean package -DskipTests

# Deploy to EC2
./infrastructure/deploy-to-ec2.sh ec2-user@your-ec2-host.amazonaws.com ~/.ssh/your-key.pem
```

**Prerequisites**:
- EC2 instance running with Java 21
- Security group allowing ports 8080-8082
- SSH key pair
- AWS credentials configured

**Cost Estimate**: $10-50/month depending on instance type

---

### 3. Custom Domain Setup

After deploying to GCP Cloud Run, map your custom domain (e.g., `user.mydomain.com`).

**Recommended Approach**: Application Load Balancer

**Why?**
- Works in all Cloud Run regions
- Better performance with global load balancing
- Includes CDN and DDoS protection
- Future-proof (direct domain mapping is deprecated)

**Quick Setup**:
```bash
# Option 1: With Google-managed SSL (free, automatic renewal)
./setup-load-balancer.sh lunari-microservices us-central1 usuario-service user.mydomain.com

# Option 2: With your own SSL certificate (immediately active)
./setup-load-balancer.sh \
  lunari-microservices us-central1 usuario-service user.mydomain.com \
  /path/to/cert.pem /path/to/key.pem

# Follow prompts to configure DNS
# Google-managed: Wait 10-60 min for SSL provisioning
# Custom cert: Immediately active
```

**Documentation**:
- **Quick Start**: [DOMAIN_SETUP_QUICK_START.md](./DOMAIN_SETUP_QUICK_START.md) - 5-step guide
- **Detailed Guide**: [LOAD_BALANCER_SETUP.md](./LOAD_BALANCER_SETUP.md) - Complete setup
- **Custom SSL**: [CUSTOM_SSL_CERTIFICATE.md](./CUSTOM_SSL_CERTIFICATE.md) - Use your own certificate
- **Automated Script**: `setup-load-balancer.sh` - One-command deployment

**Cost**: ~$18-25/month for load balancer + SSL (included)

---

## Quick Comparison

| Feature | GCP Cloud Run | GCP GKE | AWS EC2 |
|---------|---------------|---------|---------|
| Setup Time | 30 min | 1-2 hours | 30-60 min |
| Difficulty | Easy | Advanced | Moderate |
| Auto-scaling | Yes | Yes | Manual |
| Cost (Dev) | $10-20/mo | $50-100/mo | $20-40/mo |
| Management | Minimal | Moderate | Manual |
| Best For | Startups, MVPs | Enterprise | Traditional apps |

## Prerequisites by Platform

### Google Cloud Platform

```bash
# Install gcloud CLI
curl https://sdk.cloud.google.com | bash

# Authenticate
gcloud auth login

# Install kubectl (for GKE)
gcloud components install kubectl
```

**Required**:
- Google Cloud account with billing enabled
- Docker installed locally
- gcloud CLI configured

### AWS EC2

```bash
# Install AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Configure credentials
aws configure
```

**Required**:
- AWS account
- EC2 instance with Java 21
- SSH key pair
- Security groups configured

---

## Database Setup

All deployment options require a PostgreSQL database. **We recommend NeonDB** for its serverless architecture, cost savings, and simplicity.

### Option A: NeonDB (Recommended - Serverless PostgreSQL)

**Why NeonDB?**
- ✅ **Free tier**: 3GB storage, unlimited compute hours
- ✅ **40-60% cheaper** than Cloud SQL
- ✅ **Scales to zero** when idle (no wasted compute)
- ✅ **Instant provisioning** (< 10 seconds vs 5-10 minutes)
- ✅ **No Cloud SQL Proxy** needed (simpler deployment)
- ✅ **Database branching** for testing

See [NEONDB_VS_CLOUDSQL.md](./NEONDB_VS_CLOUDSQL.md) for detailed comparison.

**Setup Steps:**
```bash
# 1. Create account at https://neon.tech (free tier available)

# 2. Create 3 databases:
#    - lunari-users
#    - lunari-inventory
#    - lunari-cart

# 3. Get connection strings from NeonDB console
# Example: postgres://user:password@ep-xyz.region.aws.neon.tech/dbname?sslmode=require

# 4. Store credentials in Google Secret Manager
./infrastructure/setup-neondb-secrets.sh
```

### Option B: Google Cloud SQL (More Complex)

**Note**: Cloud SQL is more expensive and complex. Only use if you need VPC private networking or Google enterprise SLAs.

```bash
gcloud sql instances create lunari-db \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region=us-central1

gcloud sql databases create lunari_users --instance=lunari-db
gcloud sql databases create lunari_inventory --instance=lunari-db
gcloud sql databases create lunari_cart --instance=lunari-db
```

### Option C: AWS RDS

```bash
aws rds create-db-instance \
  --db-instance-identifier lunari-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --master-username admin \
  --master-user-password your-password \
  --allocated-storage 20
```

### Option D: Self-Managed PostgreSQL (Development Only)

```bash
# Install PostgreSQL
sudo apt-get install postgresql-15

# Create databases
sudo -u postgres psql
CREATE DATABASE lunari_users;
CREATE DATABASE lunari_inventory;
CREATE DATABASE lunari_cart;
CREATE USER lunari_app WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE lunari_users TO lunari_app;
```

**Not recommended for production** - no automatic backups, scaling, or high availability.

### Load Database Schema

```bash
# Connect to database
psql -h your-host -U lunari-app -d lunari_users

# Load schema
\i /path/to/script_creacion_tablas.sql
\i /path/to/seeds/user_seed.sql
\q
```

Or use the automated script:

```bash
cd setup-database/
./seed-database.sh --host your-host --user lunari-app --database lunari_users
```

---

## Environment Variables

Each service requires these environment variables:

```bash
# Database
DB_HOST=your-database-host
DB_PORT=5432
DB_NAME=lunari_users  # or lunari_inventory, lunari_cart
DB_USER=lunari-app
DB_PASSWORD=your-secure-password

# Profile
SPRING_PROFILES_ACTIVE=prod

# Inter-service Communication (for carrito service only)
USUARIO_SERVICE_URL=https://usuario-service-url
INVENTARIO_SERVICE_URL=https://inventario-service-url
```

### Managing Secrets

**GCP Secret Manager**:
```bash
echo -n "your-password" | gcloud secrets create db-password --data-file=-
```

**AWS Secrets Manager**:
```bash
aws secretsmanager create-secret \
  --name lunari/db-password \
  --secret-string "your-password"
```

**Local .env file** (development only):
```bash
cp .env.example .env
# Edit .env with your values
```

---

## Deployment Workflows

### Development Workflow

1. Make code changes locally
2. Test locally with H2 database:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=test
   ```
3. Build and test:
   ```bash
   mvn clean test
   ```
4. Deploy to cloud:
   ```bash
   ./infrastructure/deploy-to-gcp.sh
   ```

### Production Workflow

1. Create feature branch
2. Develop and test locally
3. Create pull request to `main`
4. Automated CI/CD runs tests
5. Merge to `main` triggers deployment
6. Monitor logs and metrics

---

## Monitoring & Logging

### Google Cloud Platform

```bash
# View logs
gcloud run services logs tail usuario-service --region=us-central1

# Stream logs
gcloud logging tail "resource.type=cloud_run_revision"

# View metrics
gcloud monitoring time-series list --filter='metric.type="run.googleapis.com/request_count"'
```

**Console Links**:
- Logs: https://console.cloud.google.com/logs
- Metrics: https://console.cloud.google.com/monitoring
- Cloud Run: https://console.cloud.google.com/run

### AWS EC2

```bash
# View logs on EC2
ssh -i your-key.pem ec2-user@your-host 'tail -f app.log'

# CloudWatch (if configured)
aws logs tail /aws/ec2/lunari --follow
```

---

## Troubleshooting

### Service Won't Start

**Check logs**:
```bash
# GCP
gcloud run services logs tail usuario-service --region=us-central1

# AWS
ssh -i key.pem ec2-user@host 'tail -50 app.log'
```

**Common issues**:
1. Database connection failed - verify DB_HOST and credentials
2. Port already in use - check for existing processes
3. Missing environment variables - verify all secrets are set
4. Out of memory - increase memory allocation

### Database Connection Failed

**Test connection**:
```bash
# NeonDB (preferred)
psql "postgres://user:password@host/dbname?sslmode=require"

# Or with components
psql -h DB_HOST -U DB_USER -d DB_NAME

# Cloud SQL (if using)
gcloud sql connect lunari-db --user=lunari-app
```

**Check**:
- Database instance is running
- Firewall rules allow connection
- Credentials are correct
- Connection string format is correct

### High Costs

**Optimize costs**:
```bash
# GCP - Scale to zero
gcloud run services update usuario-service --min-instances=0

# Reduce memory
gcloud run services update usuario-service --memory=512Mi

# Stop database when not in use (dev only)
gcloud sql instances patch lunari-db --activation-policy=NEVER
```

---

## CI/CD Setup

### Google Cloud Build

Create `cloudbuild.yaml` in project root:

```yaml
steps:
  - name: 'gcr.io/cloud-builders/docker'
    args: ['build', '-t', 'gcr.io/$PROJECT_ID/usuario', './usuario']

  - name: 'gcr.io/cloud-builders/docker'
    args: ['push', 'gcr.io/$PROJECT_ID/usuario']

  - name: 'gcr.io/google.com/cloudsdktool/cloud-sdk'
    entrypoint: gcloud
    args: ['run', 'deploy', 'usuario-service', '--image', 'gcr.io/$PROJECT_ID/usuario', '--region', 'us-central1']
```

Connect to GitHub:
```bash
gcloud builds triggers create github \
  --repo-name=lunari \
  --branch-pattern="^main$" \
  --build-config=cloudbuild.yaml
```

### GitHub Actions

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to GCP
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: google-github-actions/setup-gcloud@v0
      - run: gcloud builds submit --config cloudbuild.yaml
```

---

## Security Best Practices

1. **Never commit secrets** - Use Secret Manager or environment variables
2. **Use IAM roles** - Grant minimum necessary permissions
3. **Enable HTTPS** - Cloud Run/Load Balancer provides this automatically
4. **Restrict database access** - Use private IPs and firewall rules
5. **Regular updates** - Keep dependencies and base images updated
6. **Audit logs** - Enable Cloud Audit Logs or CloudTrail
7. **Vulnerability scanning** - Use Cloud Security Command Center

---

## Support & Resources

### Documentation

**Deployment Guides:**
- [GCP Deployment Guide](./GCP_DEPLOYMENT_GUIDE.md) - Complete GCP deployment guide
- [GCP Quick Start](./GCP_QUICK_START.md) - 30-minute quick start
- [Deployment Summary](./DEPLOYMENT_SUMMARY.md) - Complete deployment overview

**Custom Domain Setup:**
- [Domain Setup Quick Start](./DOMAIN_SETUP_QUICK_START.md) - 5-step domain mapping
- [Load Balancer Setup](./LOAD_BALANCER_SETUP.md) - Detailed LB configuration
- [Custom SSL Certificate](./CUSTOM_SSL_CERTIFICATE.md) - Use your own SSL certificate
- Run: `./setup-load-balancer.sh` - Automated setup

**Database:**
- [NeonDB vs Cloud SQL](./NEONDB_VS_CLOUDSQL.md) - Database comparison
- Run: `./setup-neondb-secrets.sh` - Configure secrets

**Project:**
- [Project README](../README.md) - Main project documentation

### External Resources
- **Google Cloud**: https://cloud.google.com/docs
- **AWS**: https://docs.aws.amazon.com
- **Spring Boot**: https://spring.io/projects/spring-boot
- **PostgreSQL**: https://www.postgresql.org/docs

### Getting Help
- **GCP Support**: https://cloud.google.com/support
- **AWS Support**: https://aws.amazon.com/support
- **Stack Overflow**: Tag your questions with `google-cloud-platform`, `spring-boot`

---

## Contributing

To add new deployment options:

1. Create deployment guide as `PLATFORM_DEPLOYMENT_GUIDE.md`
2. Create deployment script as `deploy-to-platform.sh`
3. Update this README with new option
4. Test deployment thoroughly
5. Document costs and prerequisites

---

**Last Updated**: December 2025
**Maintained by**: LUNARi Development Team
