# Domain Mapping Summary - user.mydomain.com → Cloud Run

## The Problem You Encountered

```
Error: Domain mappings are not available in the region of the selected service.
```

This happens because Google Cloud is deprecating direct domain mappings for Cloud Run.

## The Solution: Application Load Balancer

Instead of moving your service to another region, use a **Load Balancer** which:
- ✅ Works in ALL regions (including us-central1 where you deployed)
- ✅ Provides better performance and features
- ✅ Is the recommended production approach
- ✅ Costs only ~$18-25/month

## Architecture Overview

```
                                    ┌─────────────────────┐
user.mydomain.com ──────────────────▶  Static IP Address  │
(Your Custom Domain)                 │  (Global IP)        │
                                    └──────────┬──────────┘
                                               │
                                               │ Port 443 (HTTPS)
                                               │
                                    ┌──────────▼───────────┐
                                    │  HTTPS Forwarding   │
                                    │  Rule               │
                                    └──────────┬──────────┘
                                               │
                                    ┌──────────▼───────────┐
                                    │  HTTPS Proxy        │
                                    │  + SSL Certificate  │
                                    │  (Google-managed)   │
                                    └──────────┬──────────┘
                                               │
                                    ┌──────────▼───────────┐
                                    │  URL Map            │
                                    │  (Routing Rules)    │
                                    └──────────┬──────────┘
                                               │
                                    ┌──────────▼───────────┐
                                    │  Backend Service    │
                                    │  (Load Balancer)    │
                                    └──────────┬──────────┘
                                               │
                                    ┌──────────▼───────────┐
                                    │  Serverless NEG     │
                                    │  (Network Endpoint  │
                                    │   Group)            │
                                    └──────────┬──────────┘
                                               │
                                    ┌──────────▼───────────┐
                                    │  Cloud Run Service  │
                                    │  (usuario-service)  │
                                    │  us-central1        │
                                    └─────────────────────┘
```

## Step-by-Step Process

### Step 1: Run Setup Script (5 minutes)

```bash
cd infrastructure/
./setup-load-balancer.sh lunari-microservices us-central1 usuario-service user.mydomain.com
```

**What it creates:**
1. Static IP address
2. Network Endpoint Group (NEG) → Links to your Cloud Run service
3. Backend Service → Handles traffic routing
4. URL Map → Defines routing rules
5. SSL Certificate → Google-managed HTTPS certificate
6. HTTPS Proxy → Handles HTTPS traffic
7. HTTP Proxy → Redirects HTTP to HTTPS
8. Forwarding Rules → Routes traffic from IP to proxies

**Script output:**
```
[INFO] Configuration:
  Project ID: lunari-microservices
  Region: us-central1
  Service: usuario-service
  Domain: user.mydomain.com

[INFO] Reserving static IP address...
[SUCCESS] Static IP created

========================================
  DNS CONFIGURATION REQUIRED
========================================

Add the following A record to your DNS provider:

  Type: A
  Name: user
  Value: 34.120.123.45  ← Your static IP
  TTL: 3600

Press Enter after DNS is configured...
```

### Step 2: Configure DNS (5-10 minutes)

Go to your DNS provider and add an A record:

**Common Providers:**

#### Cloudflare
```
Dashboard → Domain → DNS → Add record
┌─────────────────────────────────────┐
│ Type: A                             │
│ Name: user                          │
│ IPv4 address: 34.120.123.45         │
│ Proxy status: DNS only (gray cloud) │
│ TTL: Auto                           │
└─────────────────────────────────────┘
```

#### GoDaddy
```
My Products → Domain → DNS Management → Add
┌─────────────────────────────────────┐
│ Type: A                             │
│ Host: user                          │
│ Points to: 34.120.123.45            │
│ TTL: 3600                           │
└─────────────────────────────────────┘
```

#### Namecheap
```
Domain List → Manage → Advanced DNS → Add New Record
┌─────────────────────────────────────┐
│ Type: A Record                      │
│ Host: user                          │
│ Value: 34.120.123.45                │
│ TTL: Automatic                      │
└─────────────────────────────────────┘
```

#### Google Domains
```
My domains → Domain → DNS → Custom resource records
┌─────────────────────────────────────┐
│ Name: user                          │
│ Type: A                             │
│ TTL: 1h                             │
│ Data: 34.120.123.45                 │
└─────────────────────────────────────┘
```

### Step 3: Verify DNS Propagation (5-30 minutes)

```bash
# Check if DNS is propagated
dig user.mydomain.com A +short

# Expected output:
# 34.120.123.45  ← Should match your static IP

# Check from multiple locations
# https://www.whatsmydns.net/#A/user.mydomain.com
```

**DNS propagation timeline:**
- 🟢 Typically: 5-15 minutes
- 🟡 Sometimes: 30-60 minutes
- 🔴 Maximum: Up to 48 hours (rare)

### Step 4: Wait for SSL Certificate (10-60 minutes)

Google automatically provisions an SSL certificate once DNS is verified.

```bash
# Check certificate status
gcloud compute ssl-certificates describe usuario-cert \
  --global \
  --format="value(managed.status)"

# Status progression:
# PROVISIONING → (wait) → ACTIVE

# Monitor continuously
watch -n 30 'gcloud compute ssl-certificates describe usuario-cert --global --format="value(managed.status)"'
```

**Certificate timeline:**
- 🟢 Typically: 10-20 minutes after DNS propagates
- 🟡 Sometimes: 30-60 minutes
- 🔴 If stuck: Check DNS is correct and propagated

### Step 5: Test Your Domain

Once SSL certificate is `ACTIVE`:

```bash
# 1. Health check
curl https://user.mydomain.com/actuator/health

# Expected response:
# {"status":"UP"}

# 2. Test API endpoint
curl https://user.mydomain.com/api/v1/users

# 3. Open Swagger UI in browser
# https://user.mydomain.com/swagger-ui/index.html

# 4. Test user registration
curl -X POST https://user.mydomain.com/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

## Timeline Summary

| Step | Task | Time |
|------|------|------|
| 1 | Run setup script | 5 min |
| 2 | Configure DNS | 5 min |
| 3 | Wait for DNS propagation | 5-30 min |
| 4 | Wait for SSL provisioning | 10-60 min |
| 5 | Test domain | 2 min |
| **Total** | **End-to-end setup** | **30-120 min** |

## Cost Breakdown

| Component | Cost | Notes |
|-----------|------|-------|
| Static IP | FREE | When in use |
| Load Balancer | $18/month | Base cost |
| Data Transfer | $0.008/GB | After first 1 GB |
| SSL Certificate | FREE | Google-managed |
| **Estimated Total** | **$18-25/month** | For typical traffic |

Compare to alternatives:
- Cloud SQL: $25-50/month (not needed, using NeonDB)
- Cloud CDN: +$10-20/month (optional, improves performance)
- Cloud Armor: +$5-10/month (optional, DDoS protection)

## What You Get

### ✅ Benefits

1. **Works in any region** - No need to redeploy your service
2. **Automatic SSL/HTTPS** - Google-managed certificate
3. **Global load balancing** - Routes users to nearest location
4. **Production-ready** - Enterprise-grade reliability
5. **Future-proof** - Won't be deprecated like domain mappings
6. **Scalable** - Handles traffic spikes automatically
7. **HTTP → HTTPS redirect** - Automatic security

### 🚀 Optional Enhancements

Enable after basic setup:

```bash
# 1. Enable Cloud CDN (faster, cheaper)
gcloud compute backend-services update usuario-backend \
  --global \
  --enable-cdn

# 2. Enable Cloud Armor (DDoS protection)
gcloud compute security-policies create usuario-security-policy
gcloud compute backend-services update usuario-backend \
  --global \
  --security-policy=usuario-security-policy
```

## Troubleshooting

### Issue: DNS Not Resolving

```bash
# Check current DNS
dig user.mydomain.com A +short

# If empty or wrong IP:
# 1. Verify A record at DNS provider
# 2. Check record: Type=A, Name=user, Value=[Static IP]
# 3. Wait longer (can take up to 48 hours)
# 4. Clear local DNS cache:
sudo systemd-resolve --flush-caches
```

### Issue: Certificate Stuck in PROVISIONING

```bash
# Common causes:
# 1. DNS not propagated yet - wait and check with dig
# 2. Wrong IP in DNS - verify IP matches static IP
# 3. Time needed - can take 60 minutes

# Check expected vs actual IP
EXPECTED=$(gcloud compute addresses describe lunari-usuario-ip --global --format="value(address)")
ACTUAL=$(dig user.mydomain.com A +short | head -n1)
echo "Expected: $EXPECTED"
echo "Actual: $ACTUAL"
# They should match!
```

### Issue: 404 or 502 Errors

```bash
# Check backend health
gcloud compute backend-services get-health usuario-backend --global

# Check Cloud Run service
gcloud run services describe usuario-service --region=us-central1

# Check logs
gcloud logging read "resource.type=http_load_balancer" --limit=20
```

## For Multiple Services

After setting up Usuario service, repeat for other services:

### Inventario Service
```bash
# Use subdomain: inventory.mydomain.com or inv.mydomain.com
./setup-load-balancer.sh lunari-microservices us-central1 inventario-service inventory.mydomain.com
```

### Carrito Service
```bash
# Use subdomain: cart.mydomain.com
./setup-load-balancer.sh lunari-microservices us-central1 carrito-service cart.mydomain.com
```

### Or: Path-Based Routing (Advanced)

Serve all services from one domain:
- `mydomain.com/api/users/*` → Usuario Service
- `mydomain.com/api/inventory/*` → Inventario Service
- `mydomain.com/api/cart/*` → Carrito Service

See `LOAD_BALANCER_SETUP.md` for path-based routing setup.

## Useful Commands

```bash
# Check all load balancer resources
gcloud compute forwarding-rules list --global
gcloud compute backend-services list --global
gcloud compute ssl-certificates list --global

# Check certificate status
gcloud compute ssl-certificates describe usuario-cert \
  --global \
  --format="value(managed.status)"

# View load balancer logs
gcloud logging read "resource.type=http_load_balancer" \
  --limit=50 \
  --format=json

# View Cloud Run logs
gcloud run services logs tail usuario-service --region=us-central1

# Update service (after code changes)
# Re-run: ./deploy-to-gcp.sh
# Load balancer automatically routes to new version

# Delete everything (cleanup)
# See LOAD_BALANCER_SETUP.md for cleanup commands
```

## Next Steps After Setup

1. **Update your application** to use the custom domain in CORS settings
2. **Update documentation** with new URLs
3. **Set up monitoring** in Cloud Console
4. **Configure alerts** for downtime/errors
5. **Enable Cloud CDN** for better performance
6. **Set up CI/CD** to deploy automatically

## Documentation References

- **Quick Start**: `DOMAIN_SETUP_QUICK_START.md` - 5-step guide
- **Detailed Guide**: `LOAD_BALANCER_SETUP.md` - Complete setup
- **Main README**: `README.md` - All deployment options
- **GCP Deployment**: `GCP_DEPLOYMENT_GUIDE.md` - Cloud Run deployment

## Support

For issues or questions:
1. Check `LOAD_BALANCER_SETUP.md` troubleshooting section
2. View logs: `gcloud run services logs tail usuario-service`
3. GCP Documentation: https://cloud.google.com/load-balancing/docs/https/setup-global-ext-https-serverless
