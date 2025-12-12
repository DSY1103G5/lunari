# Custom Domain Setup - Quick Start

This guide helps you map `user.mydomain.com` to your deployed Cloud Run service.

## The Problem

You deployed your service with `deploy-to-gcp.sh`, but when trying to map a custom domain, you got:
```
Domain mappings are not available in the region of the selected service.
```

## The Solution: Application Load Balancer (Recommended)

Google is deprecating direct domain mappings. The modern approach is using an **Application Load Balancer**.

### Why Load Balancer?

✅ Works in **all regions**
✅ Better performance (global load balancing + CDN)
✅ More features (Cloud Armor, custom SSL policies)
✅ Can serve multiple services from one domain
✅ Production-ready and future-proof

## Quick Setup (5 Steps)

### 1. Run the Setup Script

#### Option A: With Google-Managed SSL Certificate (Recommended)

```bash
cd infrastructure/

# Google will automatically provision a free SSL certificate
# Takes 10-60 minutes to activate
./setup-load-balancer.sh lunari-microservices us-central1 usuario-service user.mydomain.com
```

#### Option B: With Your Own SSL Certificate

If you already have an SSL certificate:

```bash
cd infrastructure/

# Use your existing certificate (immediately active, no wait time)
./setup-load-balancer.sh \
  lunari-microservices \
  us-central1 \
  usuario-service \
  user.mydomain.com \
  /path/to/your/certificate.pem \
  /path/to/your/private-key.pem

# See CUSTOM_SSL_CERTIFICATE.md for detailed instructions
```

### 2. Configure DNS

The script will give you a static IP address. Add this A record to your DNS provider:

**For Cloudflare:**
1. Go to DNS settings
2. Click "Add record"
3. Type: `A`
4. Name: `user`
5. IPv4 address: `[Static IP from script]`
6. Proxy status: DNS only (gray cloud)
7. Save

**For GoDaddy:**
1. Go to DNS Management
2. Click "Add"
3. Type: `A`
4. Host: `user`
5. Points to: `[Static IP]`
6. TTL: 3600
7. Save

**For Namecheap:**
1. Go to Advanced DNS
2. Add New Record
3. Type: `A Record`
4. Host: `user`
5. Value: `[Static IP]`
6. TTL: Automatic
7. Save

**For Google Domains:**
1. Go to DNS
2. Custom resource records
3. Name: `user`
4. Type: `A`
5. Data: `[Static IP]`
6. Save

### 3. Wait for DNS Propagation (5-30 minutes)

```bash
# Check if DNS is propagated
dig user.mydomain.com A +short

# Should return your static IP
# Or use: https://www.whatsmydns.net/#A/user.mydomain.com
```

### 4. Wait for SSL Certificate (10-60 minutes)

The script automatically creates a Google-managed SSL certificate. Monitor the status:

```bash
# Check certificate status
gcloud compute ssl-certificates describe usuario-cert \
  --global \
  --format="value(managed.status)"

# Status progression:
# PROVISIONING → ACTIVE (ready to use)

# Monitor continuously
watch -n 10 'gcloud compute ssl-certificates describe usuario-cert --global --format="value(managed.status)"'
```

### 5. Test Your Domain

Once the certificate is `ACTIVE`:

```bash
# Test health endpoint
curl https://user.mydomain.com/actuator/health

# Test API
curl https://user.mydomain.com/api/v1/users

# Open Swagger UI in browser
# https://user.mydomain.com/swagger-ui/index.html
```

## Troubleshooting

### DNS Not Resolving

```bash
# Check DNS
dig user.mydomain.com A +short

# If it doesn't return your static IP:
# - Verify the A record at your DNS provider
# - Wait longer (can take up to 48 hours, usually 5-30 min)
# - Clear local DNS cache:
sudo systemd-resolve --flush-caches  # Linux
```

### Certificate Stuck in PROVISIONING

**Note**: If you used a custom certificate, skip this - your certificate is immediately active!

Common causes (Google-managed certificates only):
1. **DNS not propagated yet** - Wait and verify with `dig`
2. **Wrong DNS record** - Should be A record, not CNAME
3. **Takes time** - Can take up to 60 minutes

```bash
# Verify DNS points to correct IP
STATIC_IP=$(gcloud compute addresses describe lunari-usuario-ip --global --format="value(address)")
echo "Expected IP: $STATIC_IP"
dig user.mydomain.com A +short
```

### Service Not Responding

```bash
# 1. Check Cloud Run service is running
gcloud run services describe usuario-service --region=us-central1

# 2. Check backend health
gcloud compute backend-services get-health usuario-backend --global

# 3. View load balancer logs
gcloud logging read "resource.type=http_load_balancer" --limit 20

# 4. Check forwarding rules
gcloud compute forwarding-rules list --global
```

## What the Script Creates

1. **Static IP Address**: Global static IP for your domain
2. **Network Endpoint Group (NEG)**: Links to your Cloud Run service
3. **Backend Service**: Routes traffic to your service
4. **URL Map**: Defines routing rules
5. **SSL Certificate**: Google-managed certificate for HTTPS
6. **HTTPS Proxy**: Handles HTTPS traffic
7. **HTTP Proxy**: Redirects HTTP → HTTPS
8. **Forwarding Rules**: Routes traffic from IP to proxies

## Cost Estimate

- **Static IP**: ~$0.01/hour unused, FREE when in use
- **Load Balancer**: ~$18/month base + $0.008/GB
- **SSL Certificate**: FREE (Google-managed)
- **Total**: ~$18-25/month for typical usage

## Next Steps

After your domain is working:

1. **Enable Cloud CDN** (faster, cheaper):
   ```bash
   gcloud compute backend-services update usuario-backend \
     --global --enable-cdn
   ```

2. **Add DDoS Protection** (Cloud Armor):
   ```bash
   # See LOAD_BALANCER_SETUP.md for full setup
   ```

3. **Set up other services**:
   - `inventory.mydomain.com` for Inventario
   - `cart.mydomain.com` for Carrito

4. **Monitor your service**:
   ```bash
   # View logs
   gcloud logging read "resource.type=http_load_balancer" --limit 50

   # Set up alerts
   # https://console.cloud.google.com/monitoring/alerting
   ```

## Alternative: Move to Supported Region

If you absolutely cannot use a load balancer, you can try moving to a region with domain mapping support, but this is **not recommended** as domain mappings are being deprecated.

See `CUSTOM_DOMAIN_SETUP.md` for details (not recommended).

## Full Documentation

- **Detailed Load Balancer Setup**: `LOAD_BALANCER_SETUP.md`
- **Automated Script**: `setup-load-balancer.sh`
- **Legacy Domain Mapping**: `CUSTOM_DOMAIN_SETUP.md` (deprecated)

## Support

If you encounter issues:

1. Check the detailed guide: `LOAD_BALANCER_SETUP.md`
2. View GCP documentation: https://cloud.google.com/load-balancing/docs/https/setup-global-ext-https-serverless
3. Check Cloud Run logs:
   ```bash
   gcloud run services logs tail usuario-service --region=us-central1
   ```

## Summary

```bash
# 1. Run setup script
./setup-load-balancer.sh lunari-microservices us-central1 usuario-service user.mydomain.com

# 2. Add A record at DNS provider
# Type: A, Name: user, Value: [Static IP from script]

# 3. Wait for DNS (5-30 min)
dig user.mydomain.com A +short

# 4. Wait for SSL (10-60 min)
gcloud compute ssl-certificates describe usuario-cert --global

# 5. Test your domain
curl https://user.mydomain.com/actuator/health
```

That's it! Your custom domain will be live once DNS propagates and the SSL certificate is provisioned.
