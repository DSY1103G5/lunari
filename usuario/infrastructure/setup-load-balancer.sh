#!/usr/bin/env bash

# LUNARi Services - Application Load Balancer Setup Script
# Usage: ./setup-load-balancer.sh [project-id] [region] [service-name] [domain] [cert-file] [key-file]
# Example with Google-managed SSL: ./setup-load-balancer.sh lunari-microservices us-central1 usuario-service user.mydomain.com
# Example with custom SSL: ./setup-load-balancer.sh lunari-microservices us-central1 usuario-service user.mydomain.com /path/to/cert.pem /path/to/key.pem

set -e

# Configuration
PROJECT_ID="${1:-lunari-prod-1765210460}"
REGION="${2:-us-central1}"
SERVICE_NAME="${3:-usuario-service}"
DOMAIN="${4:-user.mydomain.com}"
CERT_FILE="${5}"
KEY_FILE="${6}"

# Determine SSL certificate type
if [ -n "$CERT_FILE" ] && [ -n "$KEY_FILE" ]; then
    USE_CUSTOM_CERT=true
    SSL_TYPE="custom"
else
    USE_CUSTOM_CERT=false
    SSL_TYPE="google-managed"
fi

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
echo -e "${GREEN}  LUNARi - Load Balancer Setup${NC}"
echo -e "${GREEN}========================================${NC}\n"

log_info "Configuration:"
echo "  Project ID: $PROJECT_ID"
echo "  Region: $REGION"
echo "  Service: $SERVICE_NAME"
echo "  Domain: $DOMAIN"
echo "  SSL Type: $SSL_TYPE"
if [ "$USE_CUSTOM_CERT" = true ]; then
    echo "  Certificate: $CERT_FILE"
    echo "  Private Key: $KEY_FILE"
fi
echo ""

# Check prerequisites
log_info "Checking prerequisites..."

if ! command -v gcloud &> /dev/null; then
    log_error "gcloud CLI not found. Please install Google Cloud SDK."
    exit 1
fi

# Validate custom certificate files if provided
if [ "$USE_CUSTOM_CERT" = true ]; then
    if [ ! -f "$CERT_FILE" ]; then
        log_error "Certificate file not found: $CERT_FILE"
        exit 1
    fi
    if [ ! -f "$KEY_FILE" ]; then
        log_error "Private key file not found: $KEY_FILE"
        exit 1
    fi
    log_info "Custom SSL certificate files validated"
fi

log_success "Prerequisites check passed"
echo ""

# Set project
log_info "Setting active project..."
gcloud config set project $PROJECT_ID
log_success "Project set to $PROJECT_ID"
echo ""

# Enable required APIs
log_info "Enabling required APIs..."
gcloud services enable \
  compute.googleapis.com \
  certificatemanager.googleapis.com \
  --quiet

log_success "APIs enabled"
echo ""

# Reserve static IP
log_info "Reserving static IP address..."
if gcloud compute addresses describe lunari-usuario-ip --global &>/dev/null; then
    log_info "Static IP already exists"
else
    gcloud compute addresses create lunari-usuario-ip \
      --global \
      --ip-version IPV4
    log_success "Static IP created"
fi

STATIC_IP=$(gcloud compute addresses describe lunari-usuario-ip \
  --global \
  --format="value(address)")

echo ""
echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}  DNS CONFIGURATION REQUIRED${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""
echo "Add the following A record to your DNS provider:"
echo ""
echo "  Type: A"
echo "  Name: ${DOMAIN%%.*.*}  (subdomain part)"
echo "  Value: $STATIC_IP"
echo "  TTL: 3600 (or Auto)"
echo ""
echo "Example for common providers:"
echo "  - Cloudflare: DNS → Add record → Type: A → Name: user → IPv4: $STATIC_IP"
echo "  - GoDaddy: DNS Management → Add → Type: A → Host: user → Points to: $STATIC_IP"
echo "  - Namecheap: Advanced DNS → Add New Record → Type: A Record → Host: user → Value: $STATIC_IP"
echo ""
echo -e "${YELLOW}Verify DNS propagation:${NC}"
echo "  dig $DOMAIN A +short"
echo "  (Should return: $STATIC_IP)"
echo ""
read -p "Press Enter after DNS is configured and propagated (or Ctrl+C to exit)..."
echo ""

# Verify DNS
log_info "Verifying DNS configuration..."
DNS_IP=$(dig $DOMAIN A +short | head -n 1)
if [ "$DNS_IP" = "$STATIC_IP" ]; then
    log_success "DNS correctly configured: $DOMAIN → $STATIC_IP"
else
    log_warning "DNS not yet propagated or incorrect"
    log_warning "Expected: $STATIC_IP, Got: $DNS_IP"
    log_warning "Continuing anyway - certificate provisioning may take longer..."
fi
echo ""

# Create serverless NEG
log_info "Creating serverless Network Endpoint Group..."
if gcloud compute network-endpoint-groups describe usuario-neg --region=$REGION &>/dev/null; then
    log_info "NEG already exists"
else
    gcloud compute network-endpoint-groups create usuario-neg \
      --region=$REGION \
      --network-endpoint-type=serverless \
      --cloud-run-service=$SERVICE_NAME
    log_success "NEG created"
fi
echo ""

# Create backend service
log_info "Creating backend service..."
if gcloud compute backend-services describe usuario-backend --global &>/dev/null; then
    log_info "Backend service already exists"
else
    gcloud compute backend-services create usuario-backend \
      --global \
      --load-balancing-scheme=EXTERNAL_MANAGED

    gcloud compute backend-services add-backend usuario-backend \
      --global \
      --network-endpoint-group=usuario-neg \
      --network-endpoint-group-region=$REGION

    log_success "Backend service created"
fi
echo ""

# Create URL map
log_info "Creating URL map..."
if gcloud compute url-maps describe usuario-lb --global &>/dev/null; then
    log_info "URL map already exists"
else
    gcloud compute url-maps create usuario-lb \
      --default-service=usuario-backend
    log_success "URL map created"
fi
echo ""

# Create SSL certificate
if [ "$USE_CUSTOM_CERT" = true ]; then
    log_info "Uploading custom SSL certificate..."
    if gcloud compute ssl-certificates describe usuario-cert --global &>/dev/null; then
        log_info "SSL certificate already exists"
        CERT_TYPE=$(gcloud compute ssl-certificates describe usuario-cert \
          --global \
          --format="value(type)")
        log_info "Certificate type: $CERT_TYPE"
    else
        gcloud compute ssl-certificates create usuario-cert \
          --certificate=$CERT_FILE \
          --private-key=$KEY_FILE \
          --global
        log_success "Custom SSL certificate uploaded"
    fi
    CERT_STATUS="ACTIVE"  # Custom certs are immediately active
else
    log_info "Creating Google-managed SSL certificate..."
    if gcloud compute ssl-certificates describe usuario-cert --global &>/dev/null; then
        log_info "SSL certificate already exists"
        CERT_STATUS=$(gcloud compute ssl-certificates describe usuario-cert \
          --global \
          --format="value(managed.status)")
        log_info "Current status: $CERT_STATUS"
    else
        gcloud compute ssl-certificates create usuario-cert \
          --domains=$DOMAIN \
          --global
        log_success "SSL certificate created (provisioning in progress...)"
        CERT_STATUS="PROVISIONING"
    fi
fi
echo ""

# Create HTTPS proxy
log_info "Creating HTTPS proxy..."
if gcloud compute target-https-proxies describe usuario-https-proxy --global &>/dev/null; then
    log_info "HTTPS proxy already exists"
else
    gcloud compute target-https-proxies create usuario-https-proxy \
      --ssl-certificates=usuario-cert \
      --url-map=usuario-lb
    log_success "HTTPS proxy created"
fi
echo ""

# Create HTTP URL map and proxy for redirect
log_info "Creating HTTP proxy for redirect..."
if gcloud compute url-maps describe usuario-lb-http --global &>/dev/null; then
    log_info "HTTP URL map already exists"
else
    gcloud compute url-maps create usuario-lb-http \
      --default-service=usuario-backend
    log_success "HTTP URL map created"
fi

if gcloud compute target-http-proxies describe usuario-http-proxy --global &>/dev/null; then
    log_info "HTTP proxy already exists"
else
    gcloud compute target-http-proxies create usuario-http-proxy \
      --url-map=usuario-lb-http
    log_success "HTTP proxy created"
fi
echo ""

# Create forwarding rules
log_info "Creating HTTPS forwarding rule..."
if gcloud compute forwarding-rules describe usuario-https-forwarding-rule --global &>/dev/null; then
    log_info "HTTPS forwarding rule already exists"
else
    gcloud compute forwarding-rules create usuario-https-forwarding-rule \
      --global \
      --target-https-proxy=usuario-https-proxy \
      --address=lunari-usuario-ip \
      --ports=443
    log_success "HTTPS forwarding rule created"
fi

log_info "Creating HTTP forwarding rule..."
if gcloud compute forwarding-rules describe usuario-http-forwarding-rule --global &>/dev/null; then
    log_info "HTTP forwarding rule already exists"
else
    gcloud compute forwarding-rules create usuario-http-forwarding-rule \
      --global \
      --target-http-proxy=usuario-http-proxy \
      --address=lunari-usuario-ip \
      --ports=80
    log_success "HTTP forwarding rule created"
fi
echo ""

# Check certificate status
log_info "Checking SSL certificate status..."

if [ "$USE_CUSTOM_CERT" = true ]; then
    # Custom certificates are immediately active
    CERT_STATUS="ACTIVE"
    echo "  Certificate Type: Custom (Self-managed)"
    echo "  Certificate Status: $CERT_STATUS"
    log_success "Custom SSL certificate is ACTIVE and ready!"
else
    # Google-managed certificates need provisioning time
    CERT_STATUS=$(gcloud compute ssl-certificates describe usuario-cert \
      --global \
      --format="value(managed.status)")

    echo "  Certificate Type: Google-managed"
    echo "  Certificate Status: $CERT_STATUS"

    if [ "$CERT_STATUS" = "ACTIVE" ]; then
        log_success "SSL certificate is ACTIVE and ready!"
    elif [ "$CERT_STATUS" = "PROVISIONING" ]; then
        log_warning "SSL certificate is still PROVISIONING (this can take 10-60 minutes)"
        log_warning "The certificate will be automatically activated once ready"
    else
        log_warning "SSL certificate status: $CERT_STATUS"
        log_warning "Check DNS configuration if status is FAILED_NOT_VISIBLE"
    fi
fi
echo ""

# Summary
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Load Balancer Setup Complete!${NC}"
echo -e "${GREEN}========================================${NC}\n"

echo -e "${YELLOW}Configuration Summary:${NC}"
echo "  Domain: $DOMAIN"
echo "  Static IP: $STATIC_IP"
echo "  SSL Type: $SSL_TYPE"
echo "  SSL Status: $CERT_STATUS"
echo ""

if [ "$CERT_STATUS" = "ACTIVE" ]; then
    echo -e "${YELLOW}Test Commands:${NC}"
    echo "  Health Check:"
    echo "    curl https://$DOMAIN/actuator/health"
    echo ""
    echo "  API Endpoint:"
    echo "    curl https://$DOMAIN/api/v1/users"
    echo ""
    echo "  Swagger UI:"
    echo "    https://$DOMAIN/swagger-ui/index.html"
    echo ""
else
    echo -e "${YELLOW}Wait for SSL Certificate:${NC}"
    echo "  Check status with:"
    echo "    gcloud compute ssl-certificates describe usuario-cert --global"
    echo ""
    echo "  Once status is ACTIVE, test with:"
    echo "    curl https://$DOMAIN/actuator/health"
    echo ""
fi

if [ "$USE_CUSTOM_CERT" = false ] && [ "$CERT_STATUS" != "ACTIVE" ]; then
    echo -e "${YELLOW}Monitor Certificate Provisioning:${NC}"
    echo "  watch -n 10 'gcloud compute ssl-certificates describe usuario-cert --global --format=\"value(managed.status)\"'"
    echo ""
fi

echo -e "${YELLOW}View Logs:${NC}"
echo "  # Cloud Run logs"
echo "  gcloud run services logs tail $SERVICE_NAME --region=$REGION"
echo ""
echo "  # Load Balancer logs"
echo "  gcloud logging read \"resource.type=http_load_balancer\" --limit 50"
echo ""

echo -e "${YELLOW}Optional Enhancements:${NC}"
echo "  1. Enable Cloud CDN:"
echo "     gcloud compute backend-services update usuario-backend --global --enable-cdn"
echo ""
echo "  2. Enable Cloud Armor (DDoS protection):"
echo "     See LOAD_BALANCER_SETUP.md for details"
echo ""
echo "  3. Set up path-based routing for multiple services"
echo ""

echo -e "${YELLOW}Cleanup (if needed):${NC}"
echo "  See LOAD_BALANCER_SETUP.md for cleanup commands"
echo ""

log_success "Setup script completed successfully!"
