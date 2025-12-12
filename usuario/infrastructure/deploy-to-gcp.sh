#!/usr/bin/env bash

# LUNARi Services - Google Cloud Platform Deployment Script
# Usage: ./deploy-to-gcp.sh [project-id] [region]
# Example: ./deploy-to-gcp.sh lunari-microservices us-central1

set -e

# Default values
PROJECT_ID="${1:-lunari-microservices}"
REGION="${2:-us-central1}"
SERVICE_NAME="${3:-usuario-service}"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Utility functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Header
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  LUNARi - GCP Deployment${NC}"
echo -e "${GREEN}========================================${NC}\n"

log_info "Project ID: $PROJECT_ID"
log_info "Region: $REGION"
echo ""

# Check prerequisites
log_info "Checking prerequisites..."

if ! command -v gcloud &> /dev/null; then
    log_error "gcloud CLI not found. Please install Google Cloud SDK."
    exit 1
fi

if ! command -v docker &> /dev/null; then
    log_error "docker not found. Please install Docker."
    exit 1
fi

log_success "Prerequisites check passed"
echo ""

# Set project
log_info "Setting active project..."
gcloud config set project $PROJECT_ID
log_success "Project set to $PROJECT_ID"
echo ""

# Enable required APIs
log_info "Enabling required GCP APIs..."
gcloud services enable \
  cloudbuild.googleapis.com \
  run.googleapis.com \
  secretmanager.googleapis.com \
  artifactregistry.googleapis.com \
  --quiet

log_success "APIs enabled"
echo ""

# Create Artifact Registry repository if not exists
log_info "Setting up Artifact Registry..."
if ! gcloud artifacts repositories describe lunari-services --location=$REGION &> /dev/null; then
    gcloud artifacts repositories create lunari-services \
      --repository-format=docker \
      --location=$REGION \
      --description="LUNARi microservices container images"
    log_success "Artifact Registry repository created"
else
    log_info "Artifact Registry repository already exists"
fi
echo ""

# Configure Docker authentication
log_info "Configuring Docker authentication..."
gcloud auth configure-docker ${REGION}-docker.pkg.dev --quiet
log_success "Docker authentication configured"
echo ""

# Check for NeonDB secrets
log_info "Checking for NeonDB connection secrets..."
DB_SECRETS_EXIST=true
for secret in db-host-usuario db-user-usuario db-password-usuario db-name-usuario; do
    if ! gcloud secrets describe $secret &> /dev/null; then
        log_warning "Secret '$secret' not found"
        DB_SECRETS_EXIST=false
    fi
done

if [ "$DB_SECRETS_EXIST" = true ]; then
    log_success "All NeonDB secrets found"
    
    # Grant Secret Manager access to Cloud Run service account
    log_info "Granting Secret Manager access to Cloud Run service account..."
    PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format="value(projectNumber)")
    SERVICE_ACCOUNT="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"
    
    for secret in db-host-usuario db-user-usuario db-password-usuario db-name-usuario; do
        gcloud secrets add-iam-policy-binding $secret \
          --member="serviceAccount:${SERVICE_ACCOUNT}" \
          --role="roles/secretmanager.secretAccessor" \
          --quiet &> /dev/null || true
    done
    log_success "Secret Manager permissions granted"
else
    log_warning "Some NeonDB secrets are missing."
    log_warning "Create them using:"
    log_warning "  echo -n 'YOUR_NEON_HOST' | gcloud secrets create db-host-usuario --data-file=-"
    log_warning "  echo -n 'YOUR_USERNAME' | gcloud secrets create db-user-usuario --data-file=-"
    log_warning "  echo -n 'YOUR_PASSWORD' | gcloud secrets create db-password-usuario --data-file=-"
    log_warning "  echo -n 'YOUR_DB_NAME' | gcloud secrets create db-name-usuario --data-file=-"
fi
echo ""

# Build and push Usuario Service
log_info "Building Usuario Service..."
# cd ..
docker build -t ${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/usuario:latest -f Dockerfile .
log_success "Usuario Service built"

log_info "Pushing Usuario Service to Artifact Registry..."
docker push ${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/usuario:latest
log_success "Usuario Service pushed"
echo ""

# Deploy Usuario Service to Cloud Run
log_info "Deploying Usuario Service to Cloud Run..."
if [ "$DB_SECRETS_EXIST" = false ]; then
    log_warning "Skipping Cloud Run deployment - NeonDB secrets not configured"
    log_warning "Set up secrets first and run this script again"
else
    # Get database connection details from secrets to construct JDBC URL
    DB_HOST=$(gcloud secrets versions access latest --secret=db-host-usuario)
    DB_NAME=$(gcloud secrets versions access latest --secret=db-name-usuario)
    DB_URL="jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}?sslmode=require"
    
    gcloud run deploy $SERVICE_NAME \
      --image=${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/usuario:latest \
      --platform=managed \
      --region=$REGION \
      --port=8081 \
      --allow-unauthenticated \
      --set-env-vars="SPRING_PROFILES_ACTIVE=prod,DB_URL=${DB_URL}" \
      --set-secrets="DB_USERNAME=db-user-usuario:latest,DB_PASSWORD=db-password-usuario:latest" \
      --min-instances=0 \
      --max-instances=10 \
      --memory=1Gi \
      --cpu=1 \
      --timeout=300 \
      --quiet

    USUARIO_URL=$(gcloud run services describe $SERVICE_NAME --region=$REGION --format="value(status.url)")
    log_success "Usuario Service deployed: $USUARIO_URL"
fi
echo ""

# Summary
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Deployment Complete!${NC}"
echo -e "${GREEN}========================================${NC}\n"

if [ -n "$USUARIO_URL" ]; then
    echo -e "${YELLOW}Service URLs:${NC}"
    echo "  Usuario Service: $USUARIO_URL"
    echo "  Swagger UI: $USUARIO_URL/swagger-ui/index.html"
    echo ""

    echo -e "${YELLOW}Test Commands:${NC}"
    echo "  Health Check:"
    echo "    curl $USUARIO_URL/actuator/health"
    echo ""
    echo "  Register User:"
    echo "    curl -X POST $USUARIO_URL/api/v1/auth/register \\"
    echo "      -H 'Content-Type: application/json' \\"
    echo "      -d '{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"password123\"}'"
    echo ""
fi

echo -e "${YELLOW}View Logs:${NC}"
echo "  gcloud run services logs tail usuario-service --region=$REGION"
echo ""

echo -e "${YELLOW}Next Steps:${NC}"
echo "  1. Set up NeonDB databases at https://neon.tech (if not done)"
echo "  2. Store connection secrets in Secret Manager"
echo "  3. Deploy Inventario service (run this script from inventario/)"
echo "  4. Deploy Carrito service (run this script from carrito/)"
echo "  5. Configure custom domain (optional)"
echo "  6. Set up monitoring and alerts"
echo ""

echo -e "${YELLOW}Useful Commands:${NC}"
echo "  View all Cloud Run services:"
echo "    gcloud run services list --region=$REGION"
echo ""
echo "  Update service:"
echo "    gcloud run services update usuario-service --region=$REGION"
echo ""
echo "  Delete service:"
echo "    gcloud run services delete usuario-service --region=$REGION"
echo ""

log_success "Deployment script completed successfully!"
