#!/usr/bin/env bash

# Switch from Custom to Google-Managed SSL Certificate
# Usage: ./switch-to-google-cert.sh [project-id] [domain]

set -e

PROJECT_ID="${1:-lunari-prod-1765210460}"
DOMAIN="${2:-user.aframuz.dev}"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Switch to Google-Managed Certificate${NC}"
echo -e "${GREEN}========================================${NC}\n"

log_info "Configuration:"
echo "  Project: $PROJECT_ID"
echo "  Domain: $DOMAIN"
echo ""

# Set project
gcloud config set project $PROJECT_ID

# Step 1: Create new Google-managed certificate
log_info "Creating Google-managed SSL certificate for $DOMAIN..."
if gcloud compute ssl-certificates describe usuario-cert-google --global &>/dev/null 2>&1; then
    log_warning "usuario-cert-google already exists, deleting old one..."
    gcloud compute ssl-certificates delete usuario-cert-google --global --quiet
fi

gcloud compute ssl-certificates create usuario-cert-google \
  --domains=$DOMAIN \
  --global

log_success "Google-managed certificate created"
echo ""

# Step 2: Update HTTPS proxy to use new certificate
log_info "Updating HTTPS proxy to use Google-managed certificate..."
gcloud compute target-https-proxies update usuario-https-proxy \
  --ssl-certificates=usuario-cert-google \
  --global

log_success "HTTPS proxy updated"
echo ""

# Step 3: Delete old custom certificate
log_info "Deleting old custom certificate..."
if gcloud compute ssl-certificates describe usuario-cert --global &>/dev/null 2>&1; then
    gcloud compute ssl-certificates delete usuario-cert --global --quiet
    log_success "Old certificate deleted"
else
    log_info "Old certificate not found (already deleted)"
fi
echo ""

# Step 4: Check certificate status
log_info "Checking certificate provisioning status..."
CERT_STATUS=$(gcloud compute ssl-certificates describe usuario-cert-google \
  --global \
  --format="value(managed.status)")

echo ""
echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}  Certificate Status${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""
echo "Certificate: usuario-cert-google"
echo "Type: Google-managed"
echo "Domain: $DOMAIN"
echo "Status: $CERT_STATUS"
echo ""

if [ "$CERT_STATUS" = "ACTIVE" ]; then
    log_success "Certificate is ACTIVE! Your site is ready."
elif [ "$CERT_STATUS" = "PROVISIONING" ]; then
    log_warning "Certificate is PROVISIONING (this takes 10-60 minutes)"
    echo ""
    echo "Monitor status with:"
    echo "  watch -n 30 'gcloud compute ssl-certificates describe usuario-cert-google --global --format=\"value(managed.status)\"'"
    echo ""
    echo "The certificate will be ACTIVE when DNS is validated."
else
    log_warning "Certificate status: $CERT_STATUS"
    echo ""
    echo "Check certificate details:"
    echo "  gcloud compute ssl-certificates describe usuario-cert-google --global"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  DNS Verification${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Ensure your DNS A record points to the load balancer IP:"
echo ""

STATIC_IP=$(gcloud compute addresses describe lunari-usuario-ip \
  --global \
  --format="value(address)")

echo "  Domain: $DOMAIN"
echo "  Should point to: $STATIC_IP"
echo ""
echo "Check with:"
echo "  dig $DOMAIN A +short"
echo ""

DNS_IP=$(dig $DOMAIN A +short | head -n 1)
if [ "$DNS_IP" = "$STATIC_IP" ]; then
    log_success "DNS correctly configured: $DOMAIN → $STATIC_IP"
else
    log_warning "DNS check:"
    echo "  Expected: $STATIC_IP"
    echo "  Got: $DNS_IP"
    echo ""
    echo "If incorrect, update your DNS A record and wait for propagation."
fi

echo ""
echo -e "${GREEN}Next Steps:${NC}"
echo "1. Wait for certificate to become ACTIVE (10-60 minutes)"
echo "2. Test HTTPS: curl https://$DOMAIN/actuator/health"
echo "3. Certificate will auto-renew (no manual maintenance needed)"
echo ""
log_success "Certificate switch completed!"
