# LUNARi Microservices - Google Cloud Platform Deployment Guide

This guide covers deploying all three LUNARi microservices (Usuario, Inventario, Carrito) to Google Cloud Platform.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Architecture Overview](#architecture-overview)
3. [Option 1: Cloud Run (Recommended for Beginners)](#option-1-cloud-run-recommended)
4. [Option 2: Google Kubernetes Engine (GKE)](#option-2-gke-for-production)
5. [Database Setup (NeonDB)](#database-setup)
6. [Environment Variables & Secrets](#environment-variables--secrets)
7. [Monitoring & Logging](#monitoring--logging)
8. [Cost Optimization](#cost-optimization)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### 1. Install Required Tools

```bash
# Install Google Cloud SDK
curl https://sdk.cloud.google.com | bash
exec -l $SHELL

# Initialize gcloud
gcloud init

# Install additional components
gcloud components install kubectl
gcloud components update

# Verify installation
gcloud version
```

### 2. Set Up GCP Project

```bash
# Set your project ID
export PROJECT_ID="lunari-microservices"
export REGION="us-central1"

# Create new project (or use existing)
gcloud projects create $PROJECT_ID --name="LUNARi Microservices"

# Set active project
gcloud config set project $PROJECT_ID

# Enable required APIs
gcloud services enable \
  cloudbuild.googleapis.com \
  run.googleapis.com \
  secretmanager.googleapis.com \
  container.googleapis.com \
  artifactregistry.googleapis.com
```

### 3. Set Up Billing

Ensure billing is enabled for your project:
```bash
gcloud beta billing projects describe $PROJECT_ID
```

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     Google Cloud Platform                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Usuario    │  │  Inventario  │  │   Carrito    │      │
│  │  Service     │  │   Service    │  │   Service    │      │
│  │  (Port 8081) │  │  (Port 8081) │  │  (Port 8082) │      │
│  │  Cloud Run   │  │  Cloud Run   │  │  Cloud Run   │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                  │                  │              │
│         └──────────────────┴──────────────────┘              │
│                            │                                 │
│                            │ External Connection             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │          Secret Manager (Environment Vars)            │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │       Cloud Logging & Monitoring (Ops Suite)          │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                             │
                    External Connection
                             │
┌─────────────────────────────▼───────────────────────────────┐
│                  NeonDB (Serverless PostgreSQL)              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │lunari_users  │  │lunari_inventory││ lunari_cart  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

---

## Database Setup

### Create NeonDB Databases

NeonDB is a serverless PostgreSQL provider that's perfect for microservices deployment. It's **significantly cheaper** than Cloud SQL and scales to zero automatically.

**Why NeonDB?**
- ✅ Serverless PostgreSQL (scales to zero when idle)
- ✅ Free tier: 3GB storage, unlimited compute hours
- ✅ No cold starts (unlike Cloud SQL)
- ✅ Instant provisioning (vs 5-10 minutes for Cloud SQL)
- ✅ 40-60% cheaper than Cloud SQL
- ✅ Full PostgreSQL compatibility

### Step 1: Create NeonDB Accounts

```bash
# Visit https://neon.tech and create an account (free tier available)

# Create 3 separate databases for your microservices:
# 1. lunari-users
# 2. lunari-inventory
# 3. lunari-cart
```

### Step 2: Get Connection Strings

After creating each database, NeonDB provides a connection string:

```
postgres://user:password@ep-xyz.region.aws.neon.tech/dbname?sslmode=require
```

**Example:**
```bash
# Usuario Service
export DB_CONNECTION_USUARIO="postgres://neondb_owner:abc123@ep-cool-moon-123.us-east-1.aws.neon.tech/lunari_users?sslmode=require"

# Inventario Service
export DB_CONNECTION_INVENTARIO="postgres://neondb_owner:xyz789@ep-quiet-star-456.us-east-1.aws.neon.tech/lunari_inventory?sslmode=require"

# Carrito Service
export DB_CONNECTION_CARRITO="postgres://neondb_owner:def456@ep-bold-sun-789.us-east-1.aws.neon.tech/lunari_cart?sslmode=require"
```

### Step 3: Parse Connection Details

```bash
# Extract individual components for Usuario service
export DB_HOST_USUARIO=$(echo $DB_CONNECTION_USUARIO | sed -n 's/.*@\([^/]*\)\/.*/\1/p')
export DB_NAME_USUARIO="lunari_users"
export DB_USER_USUARIO=$(echo $DB_CONNECTION_USUARIO | sed -n 's/.*\/\/\([^:]*\):.*/\1/p')
export DB_PASSWORD_USUARIO=$(echo $DB_CONNECTION_USUARIO | sed -n 's/.*\/\/[^:]*:\([^@]*\)@.*/\1/p')

echo "Host: $DB_HOST_USUARIO"
echo "Database: $DB_NAME_USUARIO"
echo "User: $DB_USER_USUARIO"
```

### Step 4: Load Database Schema

```bash
# Connect using psql
psql "$DB_CONNECTION_USUARIO"

# Run your SQL scripts
\i /path/to/script_creacion_tablas.sql
\i /path/to/seeds/user_seed.sql
\q

# Or load directly from file
psql "$DB_CONNECTION_USUARIO" < /path/to/migration_neondb.sql

# Repeat for other databases
psql "$DB_CONNECTION_INVENTARIO" < /path/to/inventory_schema.sql
psql "$DB_CONNECTION_CARRITO" < /path/to/cart_schema.sql
```

---

## Option 1: Cloud Run (Recommended)

Cloud Run is serverless, automatically scales, and is cost-effective for microservices.

### Step 1: Create Dockerfiles

**Create `usuario/Dockerfile`:**

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/lunari-user-api-*.jar app.jar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Create similar Dockerfiles for `inventario/Dockerfile` and `carrito/Dockerfile`**, adjusting:
- JAR name pattern
- Port numbers (8081 for inventario, 8082 for carrito)

### Step 2: Build and Push Container Images

```bash
# Enable Artifact Registry API
gcloud services enable artifactregistry.googleapis.com

# Create repository
gcloud artifacts repositories create lunari-services \
  --repository-format=docker \
  --location=$REGION \
  --description="LUNARi microservices container images"

# Configure Docker authentication
gcloud auth configure-docker ${REGION}-docker.pkg.dev

# Build and push each service
cd usuario/
docker build -t ${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/usuario:latest .
docker push ${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/usuario:latest

cd ../inventario/
docker build -t ${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/inventario:latest .
docker push ${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/inventario:latest

cd ../carrito/
docker build -t ${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/carrito:latest .
docker push ${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/carrito:latest
```

### Step 3: Store Secrets in Secret Manager

```bash
# Create secrets for Usuario service
echo -n "$DB_HOST_USUARIO" | gcloud secrets create db-host-usuario --data-file=-
echo -n "$DB_USER_USUARIO" | gcloud secrets create db-user-usuario --data-file=-
echo -n "$DB_PASSWORD_USUARIO" | gcloud secrets create db-password-usuario --data-file=-
echo -n "$DB_NAME_USUARIO" | gcloud secrets create db-name-usuario --data-file=-

# Create secrets for Inventario service
echo -n "$DB_HOST_INVENTARIO" | gcloud secrets create db-host-inventario --data-file=-
echo -n "$DB_USER_INVENTARIO" | gcloud secrets create db-user-inventario --data-file=-
echo -n "$DB_PASSWORD_INVENTARIO" | gcloud secrets create db-password-inventario --data-file=-
echo -n "lunari_inventory" | gcloud secrets create db-name-inventario --data-file=-

# Create secrets for Carrito service
echo -n "$DB_HOST_CARRITO" | gcloud secrets create db-host-carrito --data-file=-
echo -n "$DB_USER_CARRITO" | gcloud secrets create db-user-carrito --data-file=-
echo -n "$DB_PASSWORD_CARRITO" | gcloud secrets create db-password-carrito --data-file=-
echo -n "lunari_cart" | gcloud secrets create db-name-carrito --data-file=-

# Note: DB_PORT is 5432 for all services (standard PostgreSQL port)
```

### Step 4: Deploy to Cloud Run

```bash
# Deploy Usuario Service with NeonDB connection
gcloud run deploy usuario-service \
  --image=${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/usuario:latest \
  --platform=managed \
  --region=$REGION \
  --port=8081 \
  --allow-unauthenticated \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod" \
  --set-secrets="DB_HOST=db-host-usuario:latest,DB_USER=db-user-usuario:latest,DB_PASSWORD=db-password-usuario:latest,DB_NAME=db-name-usuario:latest" \
  --min-instances=0 \
  --max-instances=10 \
  --memory=1Gi \
  --cpu=1 \
  --timeout=300

# Note: No --add-cloudsql-instances needed! NeonDB connects directly over the internet with SSL

# Get the service URL
export USUARIO_URL=$(gcloud run services describe usuario-service --region=$REGION --format="value(status.url)")
echo "Usuario Service URL: $USUARIO_URL"

# Deploy Inventario Service
gcloud run deploy inventario-service \
  --image=${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/inventario:latest \
  --platform=managed \
  --region=$REGION \
  --port=8081 \
  --allow-unauthenticated \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod" \
  --set-secrets="DB_HOST=db-host-inventario:latest,DB_USER=db-user-inventario:latest,DB_PASSWORD=db-password-inventario:latest,DB_NAME=db-name-inventario:latest" \
  --min-instances=0 \
  --max-instances=10 \
  --memory=1Gi \
  --cpu=1

export INVENTARIO_URL=$(gcloud run services describe inventario-service --region=$REGION --format="value(status.url)")
echo "Inventario Service URL: $INVENTARIO_URL"

# Deploy Carrito Service (with inter-service communication)
gcloud run deploy carrito-service \
  --image=${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/carrito:latest \
  --platform=managed \
  --region=$REGION \
  --port=8082 \
  --allow-unauthenticated \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod,USUARIO_SERVICE_URL=$USUARIO_URL,INVENTARIO_SERVICE_URL=$INVENTARIO_URL" \
  --set-secrets="DB_HOST=db-host-carrito:latest,DB_USER=db-user-carrito:latest,DB_PASSWORD=db-password-carrito:latest,DB_NAME=db-name-carrito:latest" \
  --min-instances=0 \
  --max-instances=10 \
  --memory=1Gi \
  --cpu=1

export CARRITO_URL=$(gcloud run services describe carrito-service --region=$REGION --format="value(status.url)")
echo "Carrito Service URL: $CARRITO_URL"
```

### Step 5: Test Deployment

```bash
# Test Usuario Service
curl $USUARIO_URL/actuator/health

# Test Swagger UI
echo "Visit: $USUARIO_URL/swagger-ui/index.html"

# Test API endpoints
curl -X POST $USUARIO_URL/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'
```

---

## Option 2: GKE (For Production)

Google Kubernetes Engine provides more control and is better for complex deployments.

### Step 1: Create GKE Cluster

```bash
# Create autopilot cluster (recommended - managed)
gcloud container clusters create-auto lunari-cluster \
  --region=$REGION \
  --release-channel=stable

# Or create standard cluster (more control)
gcloud container clusters create lunari-cluster \
  --region=$REGION \
  --num-nodes=2 \
  --machine-type=e2-medium \
  --enable-autoscaling \
  --min-nodes=1 \
  --max-nodes=5 \
  --enable-autorepair \
  --enable-autoupgrade

# Get cluster credentials
gcloud container clusters get-credentials lunari-cluster --region=$REGION
```

### Step 2: Create Kubernetes Manifests

**Create `infrastructure/k8s/usuario-deployment.yaml`:**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: usuario-config
data:
  SPRING_PROFILES_ACTIVE: "prod"
  DB_PORT: "5432"

---
apiVersion: v1
kind: Secret
metadata:
  name: usuario-secrets
type: Opaque
stringData:
  DB_HOST: "127.0.0.1"  # Cloud SQL Proxy sidecar
  DB_NAME: "lunari_users"
  DB_USER: "lunari-app"
  DB_PASSWORD: "YOUR_PASSWORD"  # Use external secrets in production

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: usuario-deployment
spec:
  replicas: 2
  selector:
    matchLabels:
      app: usuario
  template:
    metadata:
      labels:
        app: usuario
    spec:
      serviceAccountName: cloudsql-proxy-sa
      containers:
      - name: usuario
        image: us-central1-docker.pkg.dev/PROJECT_ID/lunari-services/usuario:latest
        ports:
        - containerPort: 8081
        envFrom:
        - configMapRef:
            name: usuario-config
        - secretRef:
            name: usuario-secrets
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 5

      # Cloud SQL Proxy sidecar
      - name: cloud-sql-proxy
        image: gcr.io/cloud-sql-connectors/cloud-sql-proxy:latest
        args:
          - "--structured-logs"
          - "--port=5432"
          - "PROJECT_ID:REGION:lunari-db"
        securityContext:
          runAsNonRoot: true
        resources:
          requests:
            memory: "256Mi"
            cpu: "100m"

---
apiVersion: v1
kind: Service
metadata:
  name: usuario-service
spec:
  type: LoadBalancer
  selector:
    app: usuario
  ports:
  - port: 80
    targetPort: 8081
```

**Create similar files for inventario and carrito services.**

### Step 3: Set Up Service Account for Cloud SQL

```bash
# Create service account
gcloud iam service-accounts create cloudsql-proxy-sa \
  --display-name="Cloud SQL Proxy Service Account"

# Grant Cloud SQL Client role
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:cloudsql-proxy-sa@${PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/cloudsql.client"

# Bind to Kubernetes service account
gcloud iam service-accounts add-iam-policy-binding \
  cloudsql-proxy-sa@${PROJECT_ID}.iam.gserviceaccount.com \
  --role roles/iam.workloadIdentityUser \
  --member "serviceAccount:${PROJECT_ID}.svc.id.goog[default/cloudsql-proxy-sa]"

# Create Kubernetes service account
kubectl create serviceaccount cloudsql-proxy-sa
kubectl annotate serviceaccount cloudsql-proxy-sa \
  iam.gke.io/gcp-service-account=cloudsql-proxy-sa@${PROJECT_ID}.iam.gserviceaccount.com
```

### Step 4: Deploy to GKE

```bash
# Apply manifests
kubectl apply -f infrastructure/k8s/usuario-deployment.yaml
kubectl apply -f infrastructure/k8s/inventario-deployment.yaml
kubectl apply -f infrastructure/k8s/carrito-deployment.yaml

# Check deployment status
kubectl get pods
kubectl get services

# Get external IP
kubectl get service usuario-service
```

---

## Environment Variables & Secrets

### Using Secret Manager with Cloud Run

```bash
# Create application secrets
gcloud secrets create usuario-env --data-file=usuario/.env.prod
gcloud secrets create inventario-env --data-file=inventario/.env.prod
gcloud secrets create carrito-env --data-file=carrito/.env.prod

# Grant Cloud Run access
gcloud secrets add-iam-policy-binding usuario-env \
  --member="serviceAccount:${PROJECT_ID}@appspot.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

### Update Cloud Run with Secrets

```bash
gcloud run services update usuario-service \
  --region=$REGION \
  --update-secrets=/etc/secrets/env=usuario-env:latest
```

---

## Monitoring & Logging

### Enable Cloud Monitoring

```bash
# View logs
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=usuario-service" \
  --limit=50 \
  --format=json

# Stream logs in real-time
gcloud run services logs tail usuario-service --region=$REGION

# Create log-based metrics
gcloud logging metrics create usuario_errors \
  --description="Count of errors in usuario service" \
  --log-filter='resource.type="cloud_run_revision"
    resource.labels.service_name="usuario-service"
    severity>=ERROR'
```

### Set Up Alerts

```bash
# Create alerting policy for high error rate
gcloud alpha monitoring policies create \
  --notification-channels=CHANNEL_ID \
  --display-name="Usuario Service High Error Rate" \
  --condition-display-name="Error rate > 10%" \
  --condition-threshold-value=10 \
  --condition-threshold-duration=300s
```

### Access Metrics

Visit Cloud Console:
- **Logs**: https://console.cloud.google.com/logs
- **Metrics**: https://console.cloud.google.com/monitoring
- **Trace**: https://console.cloud.google.com/traces

---

## Cost Optimization

### 1. Cloud Run Cost Savings

```bash
# Set minimum instances to 0 (scale to zero when idle)
gcloud run services update usuario-service \
  --region=$REGION \
  --min-instances=0

# Use smaller memory allocation
gcloud run services update usuario-service \
  --region=$REGION \
  --memory=512Mi
```

### 2. Cloud SQL Cost Savings

```bash
# Use smaller tier for development
gcloud sql instances patch lunari-db \
  --tier=db-f1-micro

# Enable automatic backups with retention
gcloud sql instances patch lunari-db \
  --backup-start-time=03:00 \
  --retained-backups-count=7

# Stop instance when not in use (dev only)
gcloud sql instances patch lunari-db --activation-policy=NEVER
gcloud sql instances patch lunari-db --activation-policy=ALWAYS  # to restart
```

### 3. Estimated Monthly Costs

**Cloud Run (Free tier + usage):**
- Free: 2M requests, 360K GB-seconds, 180K vCPU-seconds
- Beyond free: ~$0.40 per million requests
- Estimated: $5-50/month for moderate traffic

**Cloud SQL (db-f1-micro):**
- ~$7.50/month for shared-core instance
- ~$26/month for db-g1-small (1 vCPU)

**Total estimated: $15-80/month** (depending on traffic)

---

## CI/CD with Cloud Build

Create `cloudbuild.yaml` in project root:

```yaml
steps:
  # Build Usuario Service
  - name: 'gcr.io/cloud-builders/docker'
    args: ['build', '-t', '${_REGION}-docker.pkg.dev/$PROJECT_ID/lunari-services/usuario:$SHORT_SHA', './usuario']

  - name: 'gcr.io/cloud-builders/docker'
    args: ['push', '${_REGION}-docker.pkg.dev/$PROJECT_ID/lunari-services/usuario:$SHORT_SHA']

  # Deploy to Cloud Run
  - name: 'gcr.io/google.com/cloudsdktool/cloud-sdk'
    entrypoint: gcloud
    args:
      - 'run'
      - 'deploy'
      - 'usuario-service'
      - '--image=${_REGION}-docker.pkg.dev/$PROJECT_ID/lunari-services/usuario:$SHORT_SHA'
      - '--region=${_REGION}'
      - '--platform=managed'

substitutions:
  _REGION: us-central1

options:
  logging: CLOUD_LOGGING_ONLY
```

Create trigger:

```bash
gcloud builds triggers create github \
  --name="deploy-lunari-services" \
  --repo-name="lunari" \
  --repo-owner="YOUR_GITHUB_USERNAME" \
  --branch-pattern="^main$" \
  --build-config="cloudbuild.yaml"
```

---

## Troubleshooting

### Service Won't Start

```bash
# Check logs
gcloud run services logs tail usuario-service --region=$REGION

# Check service configuration
gcloud run services describe usuario-service --region=$REGION

# Common issues:
# 1. Database connection failed - check Cloud SQL instance is running
gcloud sql instances describe lunari-db

# 2. Missing environment variables - verify secrets
gcloud secrets versions access latest --secret="db-password"

# 3. Port mismatch - ensure EXPOSE matches server.port in application.properties
```

### Database Connection Issues

```bash
# Test Cloud SQL connection
gcloud sql connect lunari-db --user=lunari-app --database=lunari_users

# Check Cloud SQL proxy logs in GKE
kubectl logs -l app=usuario -c cloud-sql-proxy

# Verify service account permissions
gcloud projects get-iam-policy $PROJECT_ID \
  --flatten="bindings[].members" \
  --filter="bindings.members:cloudsql-proxy-sa@${PROJECT_ID}.iam.gserviceaccount.com"
```

### Inter-Service Communication

```bash
# Test service connectivity
curl $USUARIO_URL/actuator/health

# Check if Carrito can reach Usuario (from Cloud Run console shell)
curl $USUARIO_URL/api/v1/users/test-user-id

# Enable VPC connector for private communication
gcloud compute networks vpc-access connectors create lunari-connector \
  --region=$REGION \
  --range=10.8.0.0/28

# Update services to use connector
gcloud run services update carrito-service \
  --vpc-connector=lunari-connector \
  --region=$REGION
```

### Performance Issues

```bash
# Increase memory/CPU
gcloud run services update usuario-service \
  --region=$REGION \
  --memory=2Gi \
  --cpu=2

# Increase min instances (reduce cold starts)
gcloud run services update usuario-service \
  --region=$REGION \
  --min-instances=1

# Check metrics
gcloud monitoring time-series list \
  --filter='metric.type="run.googleapis.com/request_count"'
```

---

## Quick Deploy Script

Create `infrastructure/deploy-to-gcp.sh`:

```bash
#!/bin/bash
set -e

PROJECT_ID="${1:-lunari-microservices}"
REGION="${2:-us-central1}"

echo "Deploying LUNARi to GCP..."
echo "Project: $PROJECT_ID"
echo "Region: $REGION"

# Build and push images
for service in usuario inventario carrito; do
  echo "Building $service..."
  cd $service/
  docker build -t ${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/${service}:latest .
  docker push ${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/${service}:latest
  cd ..
done

# Deploy services
echo "Deploying services to Cloud Run..."
gcloud run deploy usuario-service \
  --image=${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/usuario:latest \
  --region=$REGION \
  --quiet

echo "Deployment complete!"
echo "View services: https://console.cloud.google.com/run?project=$PROJECT_ID"
```

---

## Additional Resources

- **Cloud Run Documentation**: https://cloud.google.com/run/docs
- **Cloud SQL Documentation**: https://cloud.google.com/sql/docs
- **GKE Documentation**: https://cloud.google.com/kubernetes-engine/docs
- **Secret Manager**: https://cloud.google.com/secret-manager/docs
- **Cloud Build**: https://cloud.google.com/build/docs
- **Pricing Calculator**: https://cloud.google.com/products/calculator

---

## Next Steps

1. Set up custom domain with Cloud Load Balancing
2. Configure Cloud CDN for static assets
3. Implement Cloud Armor for DDoS protection
4. Set up Cloud Identity-Aware Proxy (IAP) for authentication
5. Configure Cloud NAT for private outbound connections
6. Implement automated backups and disaster recovery
7. Set up staging and production environments
8. Configure horizontal pod autoscaling (GKE)

---

**Last Updated**: December 2025
**Maintained by**: LUNARi Development Team
