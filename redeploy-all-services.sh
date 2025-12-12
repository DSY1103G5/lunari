#!/usr/bin/env bash

# LUNARi - Redeploy All Services Script
# This script rebuilds and redeploys all three microservices with CORS configuration
# Usage: ./redeploy-all-services.sh [project-id] [region] [cors-origins]
# Example: ./redeploy-all-services.sh lunari-microservices us-central1 "https://dsy-1104-millan-munoz.vercel.app"

set -e

# Default values
PROJECT_ID="${1:-lunari-microservices}"
REGION="${2:-us-central1}"
CORS_ORIGINS="${3:-https://dsy-1104-millan-munoz.vercel.app,http://localhost:3000,http://localhost:5173}"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_section() { echo -e "${CYAN}========================================${NC}"; echo -e "${CYAN}  $1${NC}"; echo -e "${CYAN}========================================${NC}\n"; }

# Header
clear
echo -e "${GREEN}"
cat << "EOF"
╔══════════════════════════════════════════════════════════╗
║                                                          ║
║           LUNARi Microservices Deployment                ║
║              Redeploy All Services                       ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

log_info "Project ID: $PROJECT_ID"
log_info "Region: $REGION"
log_info "CORS Origins: $CORS_ORIGINS"
echo ""

# Confirmation
echo -e "${YELLOW}This will redeploy all three microservices:${NC}"
echo "  1. Usuario Service"
echo "  2. Inventario Service"
echo "  3. Carrito Service"
echo ""
read -p "Do you want to continue? (y/n) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_info "Deployment cancelled by user"
    exit 0
fi
echo ""

# Track success/failure
DEPLOYED_SERVICES=()
FAILED_SERVICES=()

# Deploy Usuario Service
log_section "1/3 - Deploying Usuario Service"
if cd usuario/infrastructure && ./redeploy.sh "$PROJECT_ID" "$REGION" "$CORS_ORIGINS"; then
    DEPLOYED_SERVICES+=("Usuario Service")
    log_success "Usuario Service deployed successfully"
    cd ../..
else
    FAILED_SERVICES+=("Usuario Service")
    log_error "Usuario Service deployment failed"
    cd ../..
fi
echo ""

# Deploy Inventario Service
log_section "2/3 - Deploying Inventario Service"
if cd inventario/infrastructure && ./redeploy.sh "$PROJECT_ID" "$REGION" "$CORS_ORIGINS"; then
    DEPLOYED_SERVICES+=("Inventario Service")
    log_success "Inventario Service deployed successfully"
    cd ../..
else
    FAILED_SERVICES+=("Inventario Service")
    log_error "Inventario Service deployment failed"
    cd ../..
fi
echo ""

# Deploy Carrito Service
log_section "3/3 - Deploying Carrito Service"
if cd carrito/infrastructure && ./redeploy.sh "$PROJECT_ID" "$REGION" "$CORS_ORIGINS"; then
    DEPLOYED_SERVICES+=("Carrito Service")
    log_success "Carrito Service deployed successfully"
    cd ../..
else
    FAILED_SERVICES+=("Carrito Service")
    log_error "Carrito Service deployment failed"
    cd ../..
fi
echo ""

# Final Summary
log_section "Deployment Summary"

if [ ${#DEPLOYED_SERVICES[@]} -gt 0 ]; then
    echo -e "${GREEN}✓ Successfully Deployed (${#DEPLOYED_SERVICES[@]}/3):${NC}"
    for service in "${DEPLOYED_SERVICES[@]}"; do
        echo "  • $service"
    done
    echo ""
fi

if [ ${#FAILED_SERVICES[@]} -gt 0 ]; then
    echo -e "${RED}✗ Failed Deployments (${#FAILED_SERVICES[@]}/3):${NC}"
    for service in "${FAILED_SERVICES[@]}"; do
        echo "  • $service"
    done
    echo ""
fi

# Get Service URLs
log_info "Fetching service URLs..."
echo ""
echo -e "${YELLOW}Service URLs:${NC}"

USUARIO_URL=$(gcloud run services describe usuario-service --region=$REGION --format="value(status.url)" 2>/dev/null || echo "Not deployed")
INVENTARIO_URL=$(gcloud run services describe inventario-service --region=$REGION --format="value(status.url)" 2>/dev/null || echo "Not deployed")
CARRITO_URL=$(gcloud run services describe carrito-service --region=$REGION --format="value(status.url)" 2>/dev/null || echo "Not deployed")

echo "  Usuario:    $USUARIO_URL"
echo "  Inventario: $INVENTARIO_URL"
echo "  Carrito:    $CARRITO_URL"
echo ""

# CORS Configuration Info
echo -e "${YELLOW}CORS Configuration:${NC}"
echo "  Allowed Origins: $CORS_ORIGINS"
echo ""

# Next Steps
echo -e "${YELLOW}Verification Steps:${NC}"
echo "  1. Test CORS from your frontend:"
echo "     https://dsy-1104-millan-munoz.vercel.app"
echo ""
echo "  2. Check service health:"
echo "     curl $USUARIO_URL/actuator/health"
echo "     curl $INVENTARIO_URL/actuator/health"
echo "     curl $CARRITO_URL/actuator/health"
echo ""
echo "  3. View logs (in separate terminals):"
echo "     gcloud run services logs tail usuario-service --region=$REGION"
echo "     gcloud run services logs tail inventario-service --region=$REGION"
echo "     gcloud run services logs tail carrito-service --region=$REGION"
echo ""

# Exit code based on results
if [ ${#FAILED_SERVICES[@]} -eq 0 ]; then
    echo -e "${GREEN}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  All services deployed successfully! 🚀                  ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════════════════╝${NC}"
    exit 0
elif [ ${#DEPLOYED_SERVICES[@]} -eq 0 ]; then
    echo -e "${RED}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║  All deployments failed. Check logs above.              ║${NC}"
    echo -e "${RED}╚══════════════════════════════════════════════════════════╝${NC}"
    exit 1
else
    echo -e "${YELLOW}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${YELLOW}║  Partial deployment. Some services failed.              ║${NC}"
    echo -e "${YELLOW}╚══════════════════════════════════════════════════════════╝${NC}"
    exit 1
fi
