#!/usr/bin/env bash

# LUNARi Carrito Service - Quick Redeploy Script
# This script rebuilds and redeploys the service with updated code (including CORS changes)
# Usage: ./redeploy.sh [project-id] [region] [cors-origins]
# Example: ./redeploy.sh lunari-microservices us-central1 "https://dsy-1104-millan-munoz.vercel.app"

set -e

# Default values
PROJECT_ID="${1:-lunari-microservices}"
REGION="${2:-us-central1}"
SERVICE_NAME="carrito-service"
CORS_ORIGINS="${3:-https://dsy-1104-millan-munoz.vercel.app,http://localhost:3000,http://localhost:5173}"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Header
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Carrito Service - Redeploy${NC}"
echo -e "${GREEN}========================================${NC}\n"

log_info "Project: $PROJECT_ID"
log_info "Region: $REGION"
log_info "Service: $SERVICE_NAME"
log_info "CORS Origins: $CORS_ORIGINS"
echo ""

# Navigate to the service root directory
cd "$(dirname "$0")/.."

# Check prerequisites
if ! command -v gcloud &> /dev/null; then
    log_error "gcloud CLI not found. Install Google Cloud SDK first."
    exit 1
fi

if ! command -v docker &> /dev/null; then
    log_error "docker not found. Install Docker first."
    exit 1
fi

# Set project
log_info "Setting active project..."
gcloud config set project $PROJECT_ID --quiet

# Configure Docker authentication
log_info "Configuring Docker authentication..."
gcloud auth configure-docker ${REGION}-docker.pkg.dev --quiet

# Build the new image
log_info "Building Carrito Service with CORS changes..."
docker build -t ${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/carrito:latest \
  -f Dockerfile .
log_success "Build complete"

# Push to Artifact Registry
log_info "Pushing to Artifact Registry..."
docker push ${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/carrito:latest
log_success "Image pushed"

# Check if service exists
if ! gcloud run services describe $SERVICE_NAME --region=$REGION &> /dev/null; then
    log_error "Service '$SERVICE_NAME' not found in region '$REGION'"
    log_error "Deploy the service first using: cd infrastructure && ./deploy-to-gcp.sh"
    exit 1
fi

# Update the Cloud Run service with new image and CORS configuration
log_info "Updating Cloud Run service with CORS configuration..."

# Create temporary env vars file to handle commas in CORS origins
TEMP_ENV_FILE=$(mktemp)
cat > "$TEMP_ENV_FILE" << EOF
CORS_ALLOWED_ORIGINS=${CORS_ORIGINS}
EOF

gcloud run services update $SERVICE_NAME \
  --image=${REGION}-docker.pkg.dev/${PROJECT_ID}/lunari-services/carrito:latest \
  --region=$REGION \
  --update-env-vars="^@^${TEMP_ENV_FILE}" \
  --quiet

# Clean up temp file
rm -f "$TEMP_ENV_FILE"

# Get the service URL
SERVICE_URL=$(gcloud run services describe $SERVICE_NAME --region=$REGION --format="value(status.url)")

# Summary
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Redeploy Complete!${NC}"
echo -e "${GREEN}========================================${NC}\n"

echo -e "${YELLOW}Service URL:${NC}"
echo "  $SERVICE_URL"
echo ""

echo -e "${YELLOW}CORS Configuration:${NC}"
echo "  Allowed Origins: $CORS_ORIGINS"
echo ""

echo -e "${YELLOW}Test CORS:${NC}"
echo "  curl -H 'Origin: https://dsy-1104-millan-munoz.vercel.app' \\"
echo "       -H 'Access-Control-Request-Method: GET' \\"
echo "       -H 'Access-Control-Request-Headers: Authorization' \\"
echo "       -X OPTIONS $SERVICE_URL/api/v1/carts/USER_ID -v"
echo ""

echo -e "${YELLOW}View Logs:${NC}"
echo "  gcloud run services logs tail $SERVICE_NAME --region=$REGION"
echo ""

echo -e "${YELLOW}Rollback (if needed):${NC}"
echo "  gcloud run services update-traffic $SERVICE_NAME \\"
echo "    --to-revisions=PREVIOUS_REVISION=100 --region=$REGION"
echo ""

log_success "Carrito service redeployed successfully!"
