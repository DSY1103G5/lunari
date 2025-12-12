#!/usr/bin/env bash

# LUNARi - Deploy All Microservices to GCP
# Usage: ./deploy-all-services.sh [project-id] [region]
# Example: ./deploy-all-services.sh lunari-microservices us-central1

set -e

# Default values
PROJECT_ID="${1:-lunari-prod-1765210460}"
REGION="${2:-southamerica-west1}"

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
echo -e "${GREEN}  LUNARi - Deploy All Services${NC}"
echo -e "${GREEN}========================================${NC}\n"

log_info "Project ID: $PROJECT_ID"
log_info "Region: $REGION"
echo ""

# Confirm deployment
echo -e "${YELLOW}This will deploy all three microservices:${NC}"
echo "  1. Usuario Service → user.aframuz.dev"
echo "  2. Inventario Service → inventory.aframuz.dev"
echo "  3. Carrito Service → cart.aframuz.dev"
echo ""
read -p "Continue? (y/N): " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_warning "Deployment cancelled"
    exit 0
fi

# Deploy Usuario Service
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  Deploying Usuario Service${NC}"
echo -e "${GREEN}========================================${NC}\n"

cd usuario/
if [ -f "infrastructure/deploy-to-gcp.sh" ]; then
    ./infrastructure/deploy-to-gcp.sh $PROJECT_ID $REGION usuario-service
    log_success "Usuario Service deployed!"
else
    log_error "Usuario deployment script not found"
    exit 1
fi
cd ..

# Deploy Inventario Service
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  Deploying Inventario Service${NC}"
echo -e "${GREEN}========================================${NC}\n"

cd inventario/
if [ -f "infrastructure/deploy-to-gcp.sh" ]; then
    ./infrastructure/deploy-to-gcp.sh $PROJECT_ID $REGION inventario-service
    log_success "Inventario Service deployed!"
else
    log_error "Inventario deployment script not found"
    exit 1
fi
cd ..

# Deploy Carrito Service
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  Deploying Carrito Service${NC}"
echo -e "${GREEN}========================================${NC}\n"

cd carrito/
if [ -f "infrastructure/deploy-to-gcp.sh" ]; then
    ./infrastructure/deploy-to-gcp.sh $PROJECT_ID $REGION carrito-service
    log_success "Carrito Service deployed!"
else
    log_error "Carrito deployment script not found"
    exit 1
fi
cd ..

# Get service URLs
USUARIO_URL=$(gcloud run services describe usuario-service --region=$REGION --format="value(status.url)" 2>/dev/null || echo "Not deployed")
INVENTARIO_URL=$(gcloud run services describe inventario-service --region=$REGION --format="value(status.url)" 2>/dev/null || echo "Not deployed")
CARRITO_URL=$(gcloud run services describe carrito-service --region=$REGION --format="value(status.url)" 2>/dev/null || echo "Not deployed")

# Summary
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  All Services Deployed!${NC}"
echo -e "${GREEN}========================================${NC}\n"

echo -e "${YELLOW}Cloud Run URLs:${NC}"
echo "  Usuario: $USUARIO_URL"
echo "  Inventario: $INVENTARIO_URL"
echo "  Carrito: $CARRITO_URL"
echo ""

echo -e "${YELLOW}Next Steps:${NC}"
echo "  1. Set up custom domains for each service:"
echo ""
echo "     # Usuario Service"
echo "     cd usuario/"
echo "     ./infrastructure/setup-load-balancer.sh $PROJECT_ID $REGION usuario-service user.aframuz.dev"
echo ""
echo "     # Inventario Service"
echo "     cd inventario/"
echo "     ./infrastructure/setup-load-balancer.sh $PROJECT_ID $REGION inventario-service inventory.aframuz.dev"
echo ""
echo "     # Carrito Service"
echo "     cd carrito/"
echo "     ./infrastructure/setup-load-balancer.sh $PROJECT_ID $REGION carrito-service cart.aframuz.dev"
echo ""
echo "  2. Update Carrito service URLs after custom domains are active:"
echo "     gcloud run services update carrito-service \\"
echo "       --region=$REGION \\"
echo "       --set-env-vars=\"USUARIO_SERVICE_URL=https://user.aframuz.dev,INVENTARIO_SERVICE_URL=https://inventory.aframuz.dev\""
echo ""

echo -e "${YELLOW}Documentation:${NC}"
echo "  Complete guide: GCP_DEPLOYMENT_COMPLETE_GUIDE.md"
echo ""

log_success "Deployment script completed successfully!"
