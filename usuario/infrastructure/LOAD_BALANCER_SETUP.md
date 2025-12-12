# Application Load Balancer Setup for Cloud Run (Recommended)

This guide shows how to set up an Application Load Balancer to map your custom domain `user.mydomain.com` to your Cloud Run service. This is the modern, recommended approach for production workloads.

## Why Use Application Load Balancer?

- **Multi-region support**: Works in all Cloud Run regions
- **Advanced features**: SSL policies, CDN, Cloud Armor, custom headers
- **Better performance**: Global load balancing with Cloud CDN
- **Future-proof**: Direct domain mapping is being deprecated
- **Unified gateway**: Can map multiple services to one domain with path-based routing

## Prerequisites

- Usuario Service deployed to Cloud Run
- Domain ownership (will verify during setup)
- GCP project with billing enabled

## Step 1: Enable Required APIs

```bash
# Set your project
PROJECT_ID="lunari-microservices"
gcloud config set project $PROJECT_ID

# Enable required APIs
gcloud services enable \
  compute.googleapis.com \
  certificatemanager.googleapis.com \
  --quiet
```

## Step 2: Reserve a Static IP Address

```bash
# Reserve a global static IP address
gcloud compute addresses create lunari-usuario-ip \
  --global \
  --ip-version IPV4

# Get the IP address (save this for DNS configuration)
STATIC_IP=$(gcloud compute addresses describe lunari-usuario-ip \
  --global \
  --format="value(address)")

echo "Your static IP: $STATIC_IP"
```

## Step 3: Create Serverless NEG (Network Endpoint Group)

```bash
# Set variables
REGION="us-central1"
SERVICE_NAME="usuario-service"

# Create serverless NEG pointing to your Cloud Run service
gcloud compute network-endpoint-groups create usuario-neg \
  --region=$REGION \
  --network-endpoint-type=serverless \
  --cloud-run-service=$SERVICE_NAME
```

## Step 4: Create Backend Service

```bash
# Create backend service
gcloud compute backend-services create usuario-backend \
  --global \
  --load-balancing-scheme=EXTERNAL_MANAGED

# Add the NEG to the backend service
gcloud compute backend-services add-backend usuario-backend \
  --global \
  --network-endpoint-group=usuario-neg \
  --network-endpoint-group-region=$REGION
```

## Step 5: Create URL Map

```bash
# Create URL map (routing rules)
gcloud compute url-maps create usuario-lb \
  --default-service=usuario-backend

# (Optional) Add path-based routing later for multiple services
# Example:
# gcloud compute url-maps add-path-matcher usuario-lb \
#   --path-matcher-name=services \
#   --default-service=usuario-backend \
#   --path-rules="/api/inventory/*=inventory-backend,/api/cart/*=cart-backend"
```

## Step 6: Configure DNS Records

**Before setting up SSL, configure your DNS:**

```bash
# Add an A record to your DNS provider:
# Type: A
# Name: user
# Value: [STATIC_IP from Step 2]
# TTL: 3600

echo "Add this A record to your DNS:"
echo "Type: A"
echo "Name: user"
echo "Value: $STATIC_IP"
echo "Domain: user.mydomain.com → $STATIC_IP"
```

### DNS Configuration by Provider

Go to your DNS provider and add:

**Cloudflare / GoDaddy / Namecheap / Google Domains:**
```
Type: A
Name: user
Value: [Your Static IP]
TTL: 3600 (or Auto)
```

**Wait 5-15 minutes for DNS propagation before proceeding.**

Verify DNS:
```bash
# Check DNS propagation
dig user.mydomain.com A +short
# Should return your static IP

# Or use online tool
# https://www.whatsmydns.net/#A/user.mydomain.com
```

## Step 7: Create SSL Certificate

### Option A: Google-Managed SSL Certificate (Recommended)

Free, automatic renewal, but requires 10-60 minute provisioning time:

```bash
DOMAIN="user.mydomain.com"

# Create Google-managed SSL certificate
gcloud compute ssl-certificates create usuario-cert \
  --domains=$DOMAIN \
  --global

# Check certificate status (takes 10-60 minutes after DNS is configured)
gcloud compute ssl-certificates describe usuario-cert \
  --global \
  --format="get(managed.status)"
```

**Certificate Status:**
- `PROVISIONING`: In progress (wait)
- `ACTIVE`: Ready to use
- `FAILED_NOT_VISIBLE`: DNS not propagated or incorrect

**Important**: Certificate provisioning requires:
1. DNS A record pointing to the static IP
2. HTTP health check responding (will be set up in next step)
3. Can take 10-60 minutes

### Option B: Custom SSL Certificate (If You Have Your Own)

If you already have an SSL certificate, you can use it immediately (no wait time):

```bash
# Upload your custom certificate
gcloud compute ssl-certificates create usuario-cert \
  --certificate=/path/to/certificate.pem \
  --private-key=/path/to/private-key.pem \
  --global

# Verify upload
gcloud compute ssl-certificates describe usuario-cert --global
```

**Advantages:**
- ✅ Immediately active (no provisioning wait)
- ✅ Can use Extended Validation (EV) or Organization Validation (OV) certificates
- ✅ Use certificates from your preferred CA

**Requirements:**
- Certificate and private key must be in PEM format
- Private key must be unencrypted (no passphrase)
- Certificate must cover your domain (user.mydomain.com)

**For detailed instructions**, see: [CUSTOM_SSL_CERTIFICATE.md](./CUSTOM_SSL_CERTIFICATE.md)

**Quick example with Let's Encrypt:**
```bash
# Get certificate from Let's Encrypt
sudo certbot certonly --manual --preferred-challenges dns -d user.mydomain.com

# Upload to GCP
gcloud compute ssl-certificates create usuario-cert \
  --certificate=/etc/letsencrypt/live/user.mydomain.com/fullchain.pem \
  --private-key=/etc/letsencrypt/live/user.mydomain.com/privkey.pem \
  --global
```

## Step 8: Create HTTPS Proxy

```bash
# Create target HTTPS proxy
gcloud compute target-https-proxies create usuario-https-proxy \
  --ssl-certificates=usuario-cert \
  --url-map=usuario-lb
```

## Step 9: Create Forwarding Rule

```bash
# Create HTTPS forwarding rule (port 443)
gcloud compute forwarding-rules create usuario-https-forwarding-rule \
  --global \
  --target-https-proxy=usuario-https-proxy \
  --address=lunari-usuario-ip \
  --ports=443

# (Optional) Create HTTP forwarding rule to redirect to HTTPS
gcloud compute url-maps create usuario-lb-http \
  --default-service=usuario-backend

gcloud compute target-http-proxies create usuario-http-proxy \
  --url-map=usuario-lb-http

gcloud compute forwarding-rules create usuario-http-forwarding-rule \
  --global \
  --target-http-proxy=usuario-http-proxy \
  --address=lunari-usuario-ip \
  --ports=80
```

## Step 10: Verify and Test

```bash
# Check certificate status
gcloud compute ssl-certificates describe usuario-cert \
  --global

# Check forwarding rule
gcloud compute forwarding-rules describe usuario-https-forwarding-rule \
  --global

# Test your service (after certificate is ACTIVE)
curl https://user.mydomain.com/actuator/health

# Test API
curl https://user.mydomain.com/api/v1/users

# Open Swagger UI
# https://user.mydomain.com/swagger-ui/index.html
```

## Step 11: Enable Cloud CDN (Optional)

```bash
# Enable Cloud CDN on backend service
gcloud compute backend-services update usuario-backend \
  --global \
  --enable-cdn

# Configure cache settings
gcloud compute backend-services update usuario-backend \
  --global \
  --cache-mode=CACHE_ALL_STATIC \
  --default-ttl=3600 \
  --max-ttl=86400 \
  --client-ttl=3600
```

## Step 12: Enable Cloud Armor (Optional)

```bash
# Create security policy
gcloud compute security-policies create usuario-security-policy \
  --description="Security policy for Usuario Service"

# Add rate limiting rule
gcloud compute security-policies rules create 100 \
  --security-policy=usuario-security-policy \
  --expression="true" \
  --action="rate-based-ban" \
  --rate-limit-threshold-count=100 \
  --rate-limit-threshold-interval-sec=60 \
  --ban-duration-sec=600 \
  --conform-action=allow \
  --exceed-action=deny-429 \
  --enforce-on-key=IP

# Apply to backend service
gcloud compute backend-services update usuario-backend \
  --global \
  --security-policy=usuario-security-policy
```

## Complete Setup Script

Save this as `setup-load-balancer.sh`:

```bash
#!/usr/bin/env bash
set -e

# Configuration
PROJECT_ID="${1:-lunari-microservices}"
REGION="${2:-us-central1}"
SERVICE_NAME="${3:-usuario-service}"
DOMAIN="${4:-user.mydomain.com}"

echo "Setting up Application Load Balancer..."
echo "Project: $PROJECT_ID"
echo "Region: $REGION"
echo "Service: $SERVICE_NAME"
echo "Domain: $DOMAIN"
echo ""

# Enable APIs
echo "[1/9] Enabling required APIs..."
gcloud services enable compute.googleapis.com certificatemanager.googleapis.com --quiet

# Reserve static IP
echo "[2/9] Reserving static IP..."
if ! gcloud compute addresses describe lunari-usuario-ip --global &>/dev/null; then
    gcloud compute addresses create lunari-usuario-ip --global --ip-version IPV4
fi
STATIC_IP=$(gcloud compute addresses describe lunari-usuario-ip --global --format="value(address)")
echo "Static IP: $STATIC_IP"
echo ""
echo ">>> IMPORTANT: Add this A record to your DNS provider <<<"
echo "Type: A"
echo "Name: user (or your subdomain)"
echo "Value: $STATIC_IP"
echo "Press Enter after DNS is configured..."
read

# Create NEG
echo "[3/9] Creating serverless NEG..."
if ! gcloud compute network-endpoint-groups describe usuario-neg --region=$REGION &>/dev/null; then
    gcloud compute network-endpoint-groups create usuario-neg \
        --region=$REGION \
        --network-endpoint-type=serverless \
        --cloud-run-service=$SERVICE_NAME
fi

# Create backend service
echo "[4/9] Creating backend service..."
if ! gcloud compute backend-services describe usuario-backend --global &>/dev/null; then
    gcloud compute backend-services create usuario-backend \
        --global \
        --load-balancing-scheme=EXTERNAL_MANAGED

    gcloud compute backend-services add-backend usuario-backend \
        --global \
        --network-endpoint-group=usuario-neg \
        --network-endpoint-group-region=$REGION
fi

# Create URL map
echo "[5/9] Creating URL map..."
if ! gcloud compute url-maps describe usuario-lb --global &>/dev/null; then
    gcloud compute url-maps create usuario-lb --default-service=usuario-backend
fi

# Create SSL certificate
echo "[6/9] Creating SSL certificate..."
if ! gcloud compute ssl-certificates describe usuario-cert --global &>/dev/null; then
    gcloud compute ssl-certificates create usuario-cert \
        --domains=$DOMAIN \
        --global
fi

# Create HTTPS proxy
echo "[7/9] Creating HTTPS proxy..."
if ! gcloud compute target-https-proxies describe usuario-https-proxy --global &>/dev/null; then
    gcloud compute target-https-proxies create usuario-https-proxy \
        --ssl-certificates=usuario-cert \
        --url-map=usuario-lb
fi

# Create HTTP proxy for redirect
echo "[8/9] Creating HTTP proxy..."
if ! gcloud compute url-maps describe usuario-lb-http --global &>/dev/null; then
    gcloud compute url-maps create usuario-lb-http --default-service=usuario-backend
fi
if ! gcloud compute target-http-proxies describe usuario-http-proxy --global &>/dev/null; then
    gcloud compute target-http-proxies create usuario-http-proxy \
        --url-map=usuario-lb-http
fi

# Create forwarding rules
echo "[9/9] Creating forwarding rules..."
if ! gcloud compute forwarding-rules describe usuario-https-forwarding-rule --global &>/dev/null; then
    gcloud compute forwarding-rules create usuario-https-forwarding-rule \
        --global \
        --target-https-proxy=usuario-https-proxy \
        --address=lunari-usuario-ip \
        --ports=443
fi
if ! gcloud compute forwarding-rules describe usuario-http-forwarding-rule --global &>/dev/null; then
    gcloud compute forwarding-rules create usuario-http-forwarding-rule \
        --global \
        --target-http-proxy=usuario-http-proxy \
        --address=lunari-usuario-ip \
        --ports=80
fi

echo ""
echo "✅ Load Balancer setup complete!"
echo ""
echo "Your domain: https://$DOMAIN"
echo "Static IP: $STATIC_IP"
echo ""
echo "Note: SSL certificate provisioning takes 10-60 minutes."
echo "Check status with:"
echo "  gcloud compute ssl-certificates describe usuario-cert --global"
echo ""
echo "Test your service:"
echo "  curl https://$DOMAIN/actuator/health"
```

## Troubleshooting

### SSL Certificate Stuck in PROVISIONING

```bash
# Check certificate status
gcloud compute ssl-certificates describe usuario-cert --global

# Common issues:
# 1. DNS not pointing to static IP
dig user.mydomain.com A +short  # Should return your static IP

# 2. Forwarding rules not active
gcloud compute forwarding-rules list --global

# 3. Certificate needs time (wait 10-60 minutes)
```

### DNS Not Resolving

```bash
# Verify DNS
dig user.mydomain.com A +short
nslookup user.mydomain.com

# Should return your static IP address
# If not, check DNS configuration at your provider
```

### Service Not Responding

```bash
# Check backend health
gcloud compute backend-services get-health usuario-backend --global

# Check Cloud Run service is running
gcloud run services describe usuario-service --region=us-central1

# View load balancer logs
gcloud logging read "resource.type=http_load_balancer" --limit 50
```

## Cleanup (if needed)

```bash
# Delete all resources (in reverse order)
gcloud compute forwarding-rules delete usuario-https-forwarding-rule --global --quiet
gcloud compute forwarding-rules delete usuario-http-forwarding-rule --global --quiet
gcloud compute target-https-proxies delete usuario-https-proxy --global --quiet
gcloud compute target-http-proxies delete usuario-http-proxy --global --quiet
gcloud compute ssl-certificates delete usuario-cert --global --quiet
gcloud compute url-maps delete usuario-lb --global --quiet
gcloud compute url-maps delete usuario-lb-http --global --quiet
gcloud compute backend-services delete usuario-backend --global --quiet
gcloud compute network-endpoint-groups delete usuario-neg --region=us-central1 --quiet
gcloud compute addresses delete lunari-usuario-ip --global --quiet
```

## Cost Considerations

- **Static IP**: ~$0.01/hour when not in use, free when in use
- **Load Balancer**: ~$18/month base + $0.008 per GB processed
- **Cloud CDN** (optional): $0.02-0.08 per GB depending on region
- **Cloud Armor** (optional): $0.75 per policy + $0.50 per million requests

## Useful Commands

```bash
# View all load balancer components
gcloud compute forwarding-rules list --global
gcloud compute backend-services list --global
gcloud compute url-maps list
gcloud compute ssl-certificates list --global

# Check certificate status
gcloud compute ssl-certificates describe usuario-cert --global --format="get(managed.status)"

# View logs
gcloud logging read "resource.type=http_load_balancer" --limit 50 --format json

# Update backend service
gcloud compute backend-services update usuario-backend --global

# Test from different regions
curl -w "@curl-format.txt" -o /dev/null -s https://user.mydomain.com/actuator/health
```

## Summary

With Application Load Balancer you get:
- ✅ Works in any Cloud Run region
- ✅ Global load balancing
- ✅ Automatic SSL certificate management
- ✅ Cloud CDN integration
- ✅ Cloud Armor DDoS protection
- ✅ Path-based routing for multiple services
- ✅ Future-proof solution

## Next Steps

1. Set up load balancers for other services (Inventario, Carrito)
2. Configure path-based routing to serve all services from one domain
3. Enable Cloud CDN for better performance
4. Set up monitoring and alerts
5. Configure Cloud Armor security policies
