# LUNARi - Google Cloud Platform Quick Start

Get your LUNARi microservices running on GCP in under 30 minutes.

## Prerequisites Checklist

- [ ] Google Cloud account with billing enabled
- [ ] `gcloud` CLI installed and configured
- [ ] Docker installed locally
- [ ] Basic familiarity with terminal/command line

## Option A: Automated Deployment (Recommended for Beginners)

### Step 1: Install Prerequisites

```bash
# Install gcloud CLI (if not already installed)
curl https://sdk.cloud.google.com | bash
exec -l $SHELL

# Login to Google Cloud
gcloud auth login

# Install Docker (Ubuntu/Debian)
sudo apt-get update
sudo apt-get install docker.io
sudo usermod -aG docker $USER
```

### Step 2: Set Up Your Project

```bash
# Set variables (customize these)
export PROJECT_ID="lunari-prod-$(date +%s)"  # Creates unique project ID
export REGION="southamerica-west1-b"  # Change to your preferred region

# Create project
gcloud projects create $PROJECT_ID --name="LUNARi Production"
gcloud config set project $PROJECT_ID

# Enable billing (must be done manually in console first)
echo "Visit: https://console.cloud.google.com/billing/linkedaccount?project=$PROJECT_ID"
echo "Press Enter after enabling billing..."
read

# Enable required APIs (takes 2-3 minutes)
gcloud services enable \
  run.googleapis.com \
  sqladmin.googleapis.com \
  artifactregistry.googleapis.com \
  secretmanager.googleapis.com
```

### Step 3: Set Up NeonDB Databases

NeonDB is a serverless PostgreSQL provider that's perfect for microservices.

```bash
# Create NeonDB accounts and databases at https://neon.tech

# You'll need to create 3 separate databases:
# 1. lunari-users (for Usuario service)
# 2. lunari-inventory (for Inventario service)
# 3. lunari-cart (for Carrito service)

# After creating each database, NeonDB provides a connection string like:
# postgres://user:password@ep-xyz.region.aws.neon.tech/dbname?sslmode=require

# Store your connection strings as variables
export DB_CONNECTION_USUARIO="postgres://user:password@host/lunari_users?sslmode=require"
export DB_CONNECTION_INVENTARIO="postgres://user:password@host/lunari_inventory?sslmode=require"
export DB_CONNECTION_CARRITO="postgres://user:password@host/lunari_cart?sslmode=require"

# Parse connection strings for individual components (for Usuario)
export DB_HOST_USUARIO=$(echo $DB_CONNECTION_USUARIO | sed -n 's/.*@\([^/]*\)\/.*/\1/p')
export DB_NAME_USUARIO="lunari_users"
export DB_USER_USUARIO=$(echo $DB_CONNECTION_USUARIO | sed -n 's/.*\/\/\([^:]*\):.*/\1/p')
export DB_PASSWORD_USUARIO=$(echo $DB_CONNECTION_USUARIO | sed -n 's/.*\/\/[^:]*:\([^@]*\)@.*/\1/p')

echo "Database setup complete!"
echo "Host: $DB_HOST_USUARIO"
```

### Step 4: Store Database Credentials in Secret Manager

```bash
# Create secrets for Usuario service
echo -n "$DB_HOST_USUARIO" | gcloud secrets create db-host-usuario --data-file=-
echo -n "$DB_USER_USUARIO" | gcloud secrets create db-user-usuario --data-file=-
echo -n "$DB_PASSWORD_USUARIO" | gcloud secrets create db-password-usuario --data-file=-
echo -n "$DB_NAME_USUARIO" | gcloud secrets create db-name-usuario --data-file=-

# Or store the full connection string
echo -n "$DB_CONNECTION_USUARIO" | gcloud secrets create db-connection-usuario --data-file=-

echo "Secrets stored in Secret Manager"

# Repeat for Inventario and Carrito services:
# - db-host-inventario, db-user-inventario, db-password-inventario, db-name-inventario
# - db-host-carrito, db-user-carrito, db-password-carrito, db-name-carrito
```

### Step 5: Deploy Usuario Service

```bash
# Navigate to usuario directory
cd /path/to/lunari/usuario

# Run deployment script
./infrastructure/deploy-to-gcp.sh $PROJECT_ID $REGION

# Script will:
# 1. Build Docker image
# 2. Push to Artifact Registry
# 3. Deploy to Cloud Run
# 4. Output service URL
```

### Step 6: Load Database Schema

```bash
# Connect to NeonDB using psql
psql "$DB_CONNECTION_USUARIO"

# At the PostgreSQL prompt, run:
\i /path/to/lunari/script_creacion_tablas.sql
\i /path/to/lunari/seeds/user_seed.sql
\q

# Or load from the migration file
psql "$DB_CONNECTION_USUARIO" < /path/to/lunari/migration_neondb.sql
```

### Step 7: Test Your Deployment

```bash
# Get service URL
export USUARIO_URL=$(gcloud run services describe usuario-service \
  --region=$REGION --format="value(status.url)")

# Test health endpoint
curl $USUARIO_URL/actuator/health

# Test registration
curl -X POST $USUARIO_URL/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'

# View Swagger UI
echo "Open in browser: $USUARIO_URL/swagger-ui/index.html"
```

## Option B: Manual Step-by-Step Deployment

### 1. Create Dockerfile

Create `usuario/Dockerfile`:

```dockerfile
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/lunari-user-api-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Build Container

```bash
cd usuario/
docker build -t usuario-service:v1 .
```

### 3. Push to Artifact Registry

```bash
# Configure Docker auth
gcloud auth configure-docker us-central1-docker.pkg.dev

# Create repository
gcloud artifacts repositories create lunari-services \
  --repository-format=docker \
  --location=us-central1

# Tag and push
docker tag usuario-service:v1 \
  us-central1-docker.pkg.dev/$PROJECT_ID/lunari-services/usuario:v1

docker push us-central1-docker.pkg.dev/$PROJECT_ID/lunari-services/usuario:v1
```

### 4. Deploy to Cloud Run

```bash
# Deploy with NeonDB connection
gcloud run deploy usuario-service \
  --image=us-central1-docker.pkg.dev/$PROJECT_ID/lunari-services/usuario:v1 \
  --region=us-central1 \
  --platform=managed \
  --port=8081 \
  --allow-unauthenticated \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod" \
  --set-secrets="DB_HOST=db-host-usuario:latest,DB_PORT=5432,DB_NAME=db-name-usuario:latest,DB_USER=db-user-usuario:latest,DB_PASSWORD=db-password-usuario:latest" \
  --memory=1Gi \
  --cpu=1 \
  --timeout=300
```

## Common Issues & Solutions

### Issue: "Permission denied" when running Docker

**Solution:**
```bash
sudo usermod -aG docker $USER
newgrp docker  # Or logout and login again
```

### Issue: "Service failed to start"

**Solution:**
```bash
# Check logs
gcloud run services logs tail usuario-service --region=us-central1

# Common causes:
# 1. Database not accessible - check Cloud SQL instance is running
# 2. Wrong environment variables - verify secrets
# 3. Port mismatch - ensure app runs on port 8081
```

### Issue: "Database connection failed"

**Solution:**
```bash
# Test NeonDB connection manually
psql "$DB_CONNECTION_USUARIO"

# Verify connection string format
echo $DB_CONNECTION_USUARIO

# Check secrets are properly set
gcloud secrets versions access latest --secret=db-host-usuario
gcloud secrets versions access latest --secret=db-password-usuario

# Ensure SSL mode is enabled (required by NeonDB)
# Connection string should include: ?sslmode=require
```

### Issue: High costs

**Solution:**
```bash
# Scale to zero when idle
gcloud run services update usuario-service \
  --region=us-central1 \
  --min-instances=0

# NeonDB automatically scales to zero when idle - no manual action needed!
# Free tier: 3GB storage, unlimited compute hours
```

## Deploying Other Services

### Deploy Inventario Service

1. Create `inventario/Dockerfile` (same structure as usuario)
2. Update port to 8081 in Dockerfile
3. Update JAR name pattern in Dockerfile
4. Run deployment:

```bash
cd inventario/
docker build -t us-central1-docker.pkg.dev/$PROJECT_ID/lunari-services/inventario:v1 .
docker push us-central1-docker.pkg.dev/$PROJECT_ID/lunari-services/inventario:v1

gcloud run deploy inventario-service \
  --image=us-central1-docker.pkg.dev/$PROJECT_ID/lunari-services/inventario:v1 \
  --region=us-central1 \
  --port=8081 \
  --allow-unauthenticated \
  --set-secrets="DB_HOST=db-host-inventario:latest,DB_PORT=5432,DB_NAME=db-name-inventario:latest,DB_USER=db-user-inventario:latest,DB_PASSWORD=db-password-inventario:latest" \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod" \
  --memory=1Gi \
  --cpu=1
```

### Deploy Carrito Service

```bash
# Get service URLs first
export USUARIO_URL=$(gcloud run services describe usuario-service --region=us-central1 --format="value(status.url)")
export INVENTARIO_URL=$(gcloud run services describe inventario-service --region=us-central1 --format="value(status.url)")

# Deploy with inter-service URLs
cd carrito/
docker build -t us-central1-docker.pkg.dev/$PROJECT_ID/lunari-services/carrito:v1 .
docker push us-central1-docker.pkg.dev/$PROJECT_ID/lunari-services/carrito:v1

gcloud run deploy carrito-service \
  --image=us-central1-docker.pkg.dev/$PROJECT_ID/lunari-services/carrito:v1 \
  --region=us-central1 \
  --port=8082 \
  --allow-unauthenticated \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod,USUARIO_SERVICE_URL=$USUARIO_URL,INVENTARIO_SERVICE_URL=$INVENTARIO_URL" \
  --set-secrets="DB_HOST=db-host-carrito:latest,DB_PORT=5432,DB_NAME=db-name-carrito:latest,DB_USER=db-user-carrito:latest,DB_PASSWORD=db-password-carrito:latest" \
  --memory=1Gi \
  --cpu=1
```

## Monitoring Your Services

### View Logs

```bash
# Real-time logs
gcloud run services logs tail usuario-service --region=us-central1

# Filter for errors
gcloud logging read "resource.type=cloud_run_revision AND severity>=ERROR" \
  --limit=50 --format=json
```

### Check Service Status

```bash
# List all services
gcloud run services list --region=us-central1

# Get service details
gcloud run services describe usuario-service --region=us-central1
```

### View Metrics

Visit Cloud Console:
- Logs: https://console.cloud.google.com/logs
- Metrics: https://console.cloud.google.com/run
- SQL: https://console.cloud.google.com/sql

## Cleanup (To Avoid Charges)

```bash
# Delete Cloud Run services
gcloud run services delete usuario-service --region=us-central1 --quiet
gcloud run services delete inventario-service --region=us-central1 --quiet
gcloud run services delete carrito-service --region=us-central1 --quiet

# Delete NeonDB databases manually at https://console.neon.tech

# Delete container images
gcloud artifacts repositories delete lunari-services --location=us-central1 --quiet

# Delete secrets
gcloud secrets delete db-host-usuario --quiet
gcloud secrets delete db-user-usuario --quiet
gcloud secrets delete db-password-usuario --quiet
gcloud secrets delete db-name-usuario --quiet
# Repeat for inventario and carrito secrets

# Delete project (removes everything from GCP)
gcloud projects delete $PROJECT_ID
```

## Cost Estimates

### Free Tier (First 2M requests/month)
- Cloud Run: FREE for 2M requests
- NeonDB Free Tier: FREE (3GB storage, unlimited compute)
- Artifact Registry: $0.10/GB stored
- **Total: ~$0-5/month** (essentially free!)

### Paid Usage (Beyond free tier)
- Cloud Run: ~$0.40 per million requests
- NeonDB Pro: $19/month (10GB storage + autoscaling)
- Additional storage: ~$0.10/GB
- **Estimate: $20-40/month for moderate traffic**

### Production Setup
- NeonDB Pro: $19-69/month (depending on storage needs)
- Cloud Run with min instances: ~$20-50/month
- Load Balancer: ~$18/month
- **Total: ~$60-140/month** (40% cheaper than Cloud SQL!)

## Next Steps

1. **Set up CI/CD**: Use Cloud Build for automatic deployments
2. **Custom Domain**: Configure custom domain with Cloud Load Balancing
3. **Monitoring**: Set up alerts for errors and high latency
4. **Security**: Implement authentication with Cloud IAP
5. **Backups**: Configure automated database backups

## Getting Help

- **GCP Documentation**: https://cloud.google.com/docs
- **Cloud Run Guide**: https://cloud.google.com/run/docs/quickstarts
- **Support**: https://cloud.google.com/support

---

**Estimated time to deploy**: 20-30 minutes
**Difficulty**: Beginner to Intermediate
**Cost**: ~$10-30/month for development
