# Custom Domain Setup for Cloud Run

> **⚠️ DEPRECATED APPROACH**
>
> This guide describes direct domain mapping to Cloud Run, which is being deprecated by Google and **not available in most regions**.
>
> **Recommended Alternative**: Use Application Load Balancer instead.
> - See: `DOMAIN_SETUP_QUICK_START.md` for quick setup
> - See: `LOAD_BALANCER_SETUP.md` for detailed guide
> - Run: `./setup-load-balancer.sh` for automated setup
>
> Load Balancer benefits: Works in all regions, better performance, more features, future-proof.

---

This guide explains how to map your custom domain `user.mydomain.com` to the Usuario Service deployed on Google Cloud Run using direct domain mapping (legacy approach).

## Prerequisites

- Usuario Service deployed to Cloud Run (completed via `deploy-to-gcp.sh`)
- Access to your domain registrar's DNS management console
- Owner/admin access to the GCP project
- Domain ownership verification (can be done during setup)

## Step 1: Verify Domain Ownership

Before mapping your domain, you need to verify ownership with Google Cloud.

### Option A: Verify via Search Console (Recommended)

```bash
# Open Google Search Console
# https://search.google.com/search-console

# Add your domain (mydomain.com) and follow verification steps
# You can verify using:
# - DNS TXT record (recommended)
# - HTML file upload
# - HTML meta tag
# - Google Analytics
# - Google Tag Manager
```

### Option B: Verify via Cloud Console

1. Navigate to: https://console.cloud.google.com/run/domains
2. Click "Add Mapping" or "Verify a new domain"
3. Follow the verification wizard

## Step 2: Map Custom Domain to Cloud Run

### Using gcloud CLI

```bash
# Set your project
gcloud config set project lunari-microservices

# Set your region
REGION="us-central1"
SERVICE_NAME="usuario-service"
DOMAIN="user.mydomain.com"

# Map the domain to your Cloud Run service
gcloud run domain-mappings create \
  --service=$SERVICE_NAME \
  --domain=$DOMAIN \
  --region=$REGION

# Check the status
gcloud run domain-mappings describe \
  --domain=$DOMAIN \
  --region=$REGION
```

### Using Cloud Console UI

1. Go to: https://console.cloud.google.com/run
2. Click on your `usuario-service`
3. Click the "MANAGE CUSTOM DOMAINS" tab
4. Click "ADD MAPPING"
5. Select your verified domain
6. Enter subdomain: `user`
7. Select the service: `usuario-service`
8. Click "CONTINUE"

## Step 3: Configure DNS Records

After creating the domain mapping, GCP will provide DNS records that you need to add to your domain registrar.

### Get DNS Record Information

```bash
# View the required DNS records
gcloud run domain-mappings describe \
  --domain=user.mydomain.com \
  --region=us-central1 \
  --format="value(status.resourceRecords)"
```

### Common DNS Record Types

GCP typically requires one of these configurations:

#### Configuration 1: CNAME Record (Most Common)

If you're mapping a subdomain like `user.mydomain.com`:

```
Type: CNAME
Name: user
Value: ghs.googlehosted.com.
TTL: 3600 (or default)
```

#### Configuration 2: A Records

If CNAME isn't available, use A records:

```
Type: A
Name: user
Value: [IP addresses provided by GCP]
TTL: 3600
```

You'll typically get 4 IP addresses like:
- 216.239.32.21
- 216.239.34.21
- 216.239.36.21
- 216.239.38.21

### Adding DNS Records to Your Domain Registrar

The exact steps vary by provider. Here are instructions for common registrars:

#### Cloudflare
1. Go to DNS settings
2. Click "Add record"
3. Select type (CNAME or A)
4. Enter name: `user`
5. Enter value from GCP
6. Disable proxy (orange cloud) initially
7. Save

#### GoDaddy
1. Go to DNS Management
2. Click "Add"
3. Select record type
4. Enter name: `user`
5. Enter value from GCP
6. Save

#### Namecheap
1. Go to Advanced DNS
2. Click "Add New Record"
3. Select type
4. Host: `user`
5. Value: from GCP
6. Save

#### Google Domains
1. Go to DNS settings
2. Scroll to "Custom resource records"
3. Enter name: `user`
4. Select type
5. Enter data from GCP
6. Add

## Step 4: Verify DNS Propagation

DNS changes can take 5 minutes to 48 hours to propagate globally.

### Check DNS Propagation

```bash
# Check if CNAME is set
dig user.mydomain.com CNAME +short

# Check if A records are set
dig user.mydomain.com A +short

# Check from multiple locations
# Use: https://www.whatsmydns.net/#CNAME/user.mydomain.com
```

### Test DNS Resolution

```bash
# Test with nslookup
nslookup user.mydomain.com

# Test with host
host user.mydomain.com

# Verify SSL certificate (after mapping is active)
curl -I https://user.mydomain.com/actuator/health
```

## Step 5: Enable HTTPS/SSL

Google Cloud Run automatically provisions SSL certificates for custom domains.

### Certificate Status

```bash
# Check certificate status
gcloud run domain-mappings describe \
  --domain=user.mydomain.com \
  --region=us-central1 \
  --format="value(status.conditions)"
```

Certificate provisioning typically takes:
- 15 minutes to 24 hours after DNS is configured
- Status shows "Ready" when complete

### Force HTTPS

Cloud Run automatically redirects HTTP to HTTPS once the certificate is provisioned.

## Step 6: Test Your Deployment

Once DNS propagates and SSL is provisioned:

```bash
# Test health endpoint
curl https://user.mydomain.com/actuator/health

# Test API endpoint
curl https://user.mydomain.com/api/v1/users

# Test Swagger UI
# Open in browser: https://user.mydomain.com/swagger-ui/index.html

# Test user registration
curl -X POST https://user.mydomain.com/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

## Troubleshooting

### Domain Mapping Not Ready

```bash
# Check status
gcloud run domain-mappings describe \
  --domain=user.mydomain.com \
  --region=us-central1

# Common issues:
# - DNS not propagated (wait up to 48 hours)
# - Wrong DNS records (verify with dig/nslookup)
# - Domain not verified (check Search Console)
```

### Certificate Not Provisioning

```bash
# Verify DNS is correct
dig user.mydomain.com +short

# Delete and recreate mapping if stuck
gcloud run domain-mappings delete \
  --domain=user.mydomain.com \
  --region=us-central1

# Recreate after DNS is confirmed working
gcloud run domain-mappings create \
  --service=usuario-service \
  --domain=user.mydomain.com \
  --region=us-central1
```

### DNS Not Resolving

- Verify record type is correct (CNAME for subdomain)
- Check TTL hasn't expired old records
- Flush local DNS cache:
  ```bash
  # Linux
  sudo systemd-resolve --flush-caches

  # macOS
  sudo dscacheutil -flushcache; sudo killall -HUP mDNSResponder

  # Windows
  ipconfig /flushdns
  ```

## Security Considerations

### Enable Cloud Armor (Optional)

Protect your service from DDoS and web attacks:

```bash
# Create security policy
gcloud compute security-policies create lunari-usuario-policy \
  --description "Security policy for Usuario Service"

# Add rate limiting rule
gcloud compute security-policies rules create 1000 \
  --security-policy lunari-usuario-policy \
  --expression "true" \
  --action "rate-based-ban" \
  --rate-limit-threshold-count 100 \
  --rate-limit-threshold-interval-sec 60 \
  --ban-duration-sec 600
```

### Update CORS Settings

If you need to allow requests from your custom domain, update your Spring Boot configuration:

```properties
# In application.properties or application-prod.properties
cors.allowed-origins=https://user.mydomain.com,https://www.mydomain.com
```

## Additional Services

Repeat this process for other microservices:

- **Inventario Service**: `inventory.mydomain.com` or `inv.mydomain.com`
- **Carrito Service**: `cart.mydomain.com`

## Useful Commands

```bash
# List all domain mappings
gcloud run domain-mappings list --region=us-central1

# Update service after domain mapping
gcloud run services update usuario-service --region=us-central1

# View service URL
gcloud run services describe usuario-service \
  --region=us-central1 \
  --format="value(status.url)"

# Monitor logs
gcloud run services logs tail usuario-service --region=us-central1

# Delete domain mapping
gcloud run domain-mappings delete \
  --domain=user.mydomain.com \
  --region=us-central1
```

## Summary Checklist

- [ ] Verify domain ownership in Google Search Console or Cloud Console
- [ ] Create domain mapping in Cloud Run
- [ ] Add CNAME or A records to your DNS provider
- [ ] Wait for DNS propagation (15 min - 48 hours)
- [ ] Verify SSL certificate is provisioned
- [ ] Test all endpoints with custom domain
- [ ] Update any hardcoded URLs in your application
- [ ] Update CORS configuration if needed
- [ ] Configure CDN/Cloud Armor if needed
- [ ] Update documentation with new URLs

## Resources

- [Cloud Run Custom Domains Documentation](https://cloud.google.com/run/docs/mapping-custom-domains)
- [Domain Verification Guide](https://cloud.google.com/storage/docs/domain-name-verification)
- [SSL Certificate Troubleshooting](https://cloud.google.com/run/docs/troubleshooting#certificate-provisioning)
- [DNS Propagation Checker](https://www.whatsmydns.net/)

## Next Steps

After setting up the custom domain:

1. Update your frontend application to use `https://user.mydomain.com` instead of Cloud Run URL
2. Configure monitoring and alerts for the custom domain
3. Set up API Gateway if you need a unified API endpoint
4. Consider using Cloud CDN for better performance
5. Implement rate limiting and security policies
6. Update your API documentation with the new URLs
