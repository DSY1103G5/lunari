# GCP Cloud Run - CORS Configuration Guide

Complete guide for configuring CORS (Cross-Origin Resource Sharing) for LUNARi microservices deployed on Google Cloud Platform Cloud Run.

## Table of Contents
1. [Quick Redeploy (Recommended)](#quick-redeploy-recommended)
2. [Manual CORS Configuration](#manual-cors-configuration)
3. [Verification](#verification)
4. [Troubleshooting](#troubleshooting)
5. [Advanced Configuration](#advanced-configuration)

---

## Quick Redeploy (Recommended)

Use the provided redeploy scripts to update all services with CORS configuration:

### Usuario Service
```bash
cd usuario/infrastructure
./redeploy.sh lunari-microservices us-central1 "https://dsy-1104-millan-munoz.vercel.app"
```

### Inventario Service
```bash
cd inventario/infrastructure
./redeploy.sh lunari-microservices us-central1 "https://dsy-1104-millan-munoz.vercel.app"
```

### Carrito Service
```bash
cd carrito/infrastructure
./redeploy.sh lunari-microservices us-central1 "https://dsy-1104-millan-munoz.vercel.app"
```

**Parameters:**
- `$1`: GCP Project ID (default: `lunari-microservices`)
- `$2`: GCP Region (default: `us-central1`)
- `$3`: CORS Allowed Origins (default includes your Vercel domain + localhost)

---

## Manual CORS Configuration

### Method 1: Using gcloud CLI (Recommended)

#### Set CORS for a Single Origin
```bash
gcloud run services update usuario-service \
  --region=us-central1 \
  --update-env-vars="CORS_ALLOWED_ORIGINS=https://dsy-1104-millan-munoz.vercel.app"
```

#### Set CORS for Multiple Origins
**Important:** When using multiple origins with `--update-env-vars`, use a file with the `^@^filename` syntax:

```bash
# Create env file
cat > /tmp/cors-env.txt << 'EOF'
CORS_ALLOWED_ORIGINS=https://dsy-1104-millan-munoz.vercel.app,https://production-domain.com,http://localhost:3000
EOF

# Update service
gcloud run services update inventario-service \
  --region=us-central1 \
  --update-env-vars="^@^/tmp/cors-env.txt"

# Clean up
rm /tmp/cors-env.txt
```

#### Update All Three Services at Once
```bash
# Create env file with CORS origins
cat > /tmp/cors-env.txt << 'EOF'
CORS_ALLOWED_ORIGINS=https://dsy-1104-millan-munoz.vercel.app,http://localhost:3000,http://localhost:5173
EOF

# Update each service
for SERVICE in usuario-service inventario-service carrito-service; do
  echo "Updating $SERVICE..."
  gcloud run services update $SERVICE \
    --region=us-central1 \
    --update-env-vars="^@^/tmp/cors-env.txt"
done

# Clean up
rm /tmp/cors-env.txt
```

### Method 2: Using GCP Console (Web UI)

1. **Navigate to Cloud Run**
   - Go to [console.cloud.google.com](https://console.cloud.google.com)
   - Select your project (`lunari-microservices`)
   - Navigate to **Cloud Run** in the left sidebar

2. **Select Your Service**
   - Click on the service name (e.g., `usuario-service`)

3. **Edit Service Configuration**
   - Click **"EDIT & DEPLOY NEW REVISION"** at the top

4. **Add Environment Variable**
   - Scroll to **"Container(s), Volumes, Networking, Security"** section
   - Click **"VARIABLES & SECRETS"** tab
   - Click **"+ ADD VARIABLE"**
   - Name: `CORS_ALLOWED_ORIGINS`
   - Value: `https://dsy-1104-millan-munoz.vercel.app,http://localhost:3000,http://localhost:5173`

5. **Deploy**
   - Click **"DEPLOY"** at the bottom
   - Wait for deployment to complete (usually 1-3 minutes)

6. **Repeat for Other Services**
   - Repeat steps 2-5 for `inventario-service` and `carrito-service`

### Method 3: Using YAML Configuration

Create a service YAML file:

```yaml
apiVersion: serving.knative.dev/v1
kind: Service
metadata:
  name: usuario-service
spec:
  template:
    spec:
      containers:
      - image: us-central1-docker.pkg.dev/lunari-microservices/lunari-services/usuario:latest
        env:
        - name: CORS_ALLOWED_ORIGINS
          value: "https://dsy-1104-millan-munoz.vercel.app,http://localhost:3000"
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        # ... other env vars
```

Apply the configuration:
```bash
gcloud run services replace service.yaml --region=us-central1
```

---

## Verification

### 1. Check Environment Variables
Verify that CORS_ALLOWED_ORIGINS is set correctly:

```bash
# Usuario Service
gcloud run services describe usuario-service \
  --region=us-central1 \
  --format="value(spec.template.spec.containers[0].env)"

# Or more readable output
gcloud run services describe usuario-service \
  --region=us-central1 \
  --format="yaml(spec.template.spec.containers[0].env)" | grep -A2 CORS
```

### 2. Test CORS with curl
Test preflight OPTIONS request:

```bash
# Usuario Service
curl -H "Origin: https://dsy-1104-millan-munoz.vercel.app" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     https://user.aframuz.dev/api/v1/auth/login \
     -v

# Inventario Service
curl -H "Origin: https://dsy-1104-millan-munoz.vercel.app" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: X-API-Key" \
     -X OPTIONS \
     https://inventory.aframuz.dev/api/v1/productos/activos \
     -v

# Carrito Service
curl -H "Origin: https://dsy-1104-millan-munoz.vercel.app" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: Authorization" \
     -X OPTIONS \
     https://cart.aframuz.dev/api/v1/carts/USER_ID \
     -v
```

**Expected Response Headers:**
```
< HTTP/2 200
< access-control-allow-origin: https://dsy-1104-millan-munoz.vercel.app
< access-control-allow-methods: GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD
< access-control-allow-headers: Authorization, Content-Type, Accept, X-Requested-With, X-API-Key, Origin, Access-Control-Request-Method, Access-Control-Request-Headers
< access-control-allow-credentials: true
< access-control-max-age: 3600
```

### 3. Test from Browser Console
Open your frontend application and run in the browser console:

```javascript
// Test Usuario Service
fetch('https://user.aframuz.dev/api/v1/auth/login', {
  method: 'OPTIONS',
  headers: {
    'Origin': 'https://dsy-1104-millan-munoz.vercel.app'
  }
})
.then(response => {
  console.log('CORS Headers:', {
    'access-control-allow-origin': response.headers.get('access-control-allow-origin'),
    'access-control-allow-methods': response.headers.get('access-control-allow-methods'),
    'access-control-allow-credentials': response.headers.get('access-control-allow-credentials')
  });
})
.catch(error => console.error('CORS Error:', error));
```

### 4. View Service Logs
Check logs for CORS-related issues:

```bash
# Stream logs in real-time
gcloud run services logs tail usuario-service --region=us-central1

# View recent logs
gcloud run services logs read usuario-service --region=us-central1 --limit=50
```

---

## Troubleshooting

### Issue: "Bad syntax for dict arg" when setting CORS_ALLOWED_ORIGINS

**Cause:** gcloud interprets commas in the value as multiple environment variable declarations.

**Solution:** Use the `^@^filename` syntax to read from a file:
```bash
# Correct - using file reference
cat > /tmp/cors.txt << 'EOF'
CORS_ALLOWED_ORIGINS=https://domain1.com,https://domain2.com
EOF

gcloud run services update usuario-service \
  --region=us-central1 \
  --update-env-vars="^@^/tmp/cors.txt"

rm /tmp/cors.txt

# Wrong - direct commas will fail
gcloud run services update usuario-service \
  --region=us-central1 \
  --update-env-vars="CORS_ALLOWED_ORIGINS=https://domain1.com,https://domain2.com"
```

The redeploy scripts automatically handle this using temporary files.

### Issue: "No 'Access-Control-Allow-Origin' header is present"

**Cause:** CORS_ALLOWED_ORIGINS environment variable not set or service not redeployed.

**Solution:**
```bash
# Set the environment variable
gcloud run services update usuario-service \
  --region=us-central1 \
  --update-env-vars="CORS_ALLOWED_ORIGINS=https://dsy-1104-millan-munoz.vercel.app"

# Verify it was set
gcloud run services describe usuario-service \
  --region=us-central1 \
  --format="yaml(spec.template.spec.containers[0].env)" | grep CORS
```

### Issue: "CORS policy: The value of the 'Access-Control-Allow-Origin' header must not be '*'"

**Cause:** Trying to use wildcard `*` with credentials.

**Solution:** Specify exact origins instead of `*`:
```bash
CORS_ALLOWED_ORIGINS="https://dsy-1104-millan-munoz.vercel.app,https://other-domain.com"
```

### Issue: "CORS policy: Response to preflight request doesn't pass access control check"

**Cause:** The origin is not in the allowed list.

**Solution:** Add your origin to the list:
```bash
# Check current origins
gcloud run services describe usuario-service \
  --region=us-central1 \
  --format="value(spec.template.spec.containers[0].env)" | grep CORS

# Add new origin
NEW_ORIGINS="https://dsy-1104-millan-munoz.vercel.app,https://new-domain.com"
gcloud run services update usuario-service \
  --region=us-central1 \
  --update-env-vars="CORS_ALLOWED_ORIGINS=$NEW_ORIGINS"
```

### Issue: OPTIONS request returns 401 Unauthorized

**Cause:** Security filters (JWT/API Key) are blocking OPTIONS requests.

**Solution:** This is already handled in the code. If you still see this:
1. Check that Spring Security config includes CORS before authentication
2. Verify CorsFilter is properly registered
3. Check logs for filter order issues

```bash
gcloud run services logs read usuario-service --region=us-central1 | grep -i "cors\|options"
```

### Issue: Changes not taking effect

**Cause:** Browser caching or service not fully deployed.

**Solution:**
```bash
# 1. Check deployment status
gcloud run services describe usuario-service --region=us-central1

# 2. Force new revision
gcloud run services update usuario-service \
  --region=us-central1 \
  --update-env-vars="CORS_ALLOWED_ORIGINS=https://dsy-1104-millan-munoz.vercel.app" \
  --no-traffic  # Deploy without traffic first

# 3. Route 100% traffic to latest
gcloud run services update-traffic usuario-service \
  --region=us-central1 \
  --to-latest

# 4. Clear browser cache or use incognito mode
```

---

## Advanced Configuration

### Dynamic CORS for Review Deployments

If you have preview deployments (e.g., Vercel preview URLs):

```bash
# Allow all Vercel preview URLs (not recommended for production)
CORS_ORIGINS="https://dsy-1104-millan-munoz.vercel.app,https://*.vercel.app"

# Better: Maintain a list of specific preview URLs
CORS_ORIGINS="https://dsy-1104-millan-munoz.vercel.app,https://preview-branch-abc123.vercel.app"
```

### Environment-Specific Configuration

Create separate services or use different env vars for staging/production:

```bash
# Production
gcloud run services update usuario-service-prod \
  --region=us-central1 \
  --update-env-vars="CORS_ALLOWED_ORIGINS=https://dsy-1104-millan-munoz.vercel.app,https://lunari.com"

# Staging
gcloud run services update usuario-service-staging \
  --region=us-central1 \
  --update-env-vars="CORS_ALLOWED_ORIGINS=https://staging.vercel.app,http://localhost:3000,http://localhost:5173"
```

### Using Secret Manager for CORS Origins

For sensitive or frequently changing origins:

```bash
# Create secret
echo -n "https://dsy-1104-millan-munoz.vercel.app,https://domain2.com" | \
  gcloud secrets create cors-allowed-origins --data-file=-

# Grant access to service account
PROJECT_NUMBER=$(gcloud projects describe lunari-microservices --format="value(projectNumber)")
SERVICE_ACCOUNT="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"

gcloud secrets add-iam-policy-binding cors-allowed-origins \
  --member="serviceAccount:${SERVICE_ACCOUNT}" \
  --role="roles/secretmanager.secretAccessor"

# Update service to use secret
gcloud run services update usuario-service \
  --region=us-central1 \
  --set-secrets="CORS_ALLOWED_ORIGINS=cors-allowed-origins:latest"
```

### Monitoring CORS Requests

Set up logging to monitor CORS requests:

```bash
# Create log-based metric
gcloud logging metrics create cors_preflight_requests \
  --description="Count of CORS preflight requests" \
  --log-filter='resource.type="cloud_run_revision"
    httpRequest.requestMethod="OPTIONS"'

# View metrics
gcloud logging read "resource.type=cloud_run_revision httpRequest.requestMethod=OPTIONS" \
  --limit=20 \
  --format=json
```

---

## Quick Reference Commands

### View Current Configuration
```bash
gcloud run services describe SERVICE_NAME --region=REGION --format=yaml
```

### Update Single Environment Variable
```bash
gcloud run services update SERVICE_NAME --region=REGION \
  --update-env-vars="KEY=value"
```

### Update Multiple Environment Variables
```bash
gcloud run services update SERVICE_NAME --region=REGION \
  --update-env-vars="KEY1=value1,KEY2=value2"
```

### Remove Environment Variable
```bash
gcloud run services update SERVICE_NAME --region=REGION \
  --remove-env-vars="KEY"
```

### List All Services
```bash
gcloud run services list --region=REGION
```

### View Service Logs
```bash
gcloud run services logs tail SERVICE_NAME --region=REGION
```

### Rollback to Previous Revision
```bash
# List revisions
gcloud run revisions list --service=SERVICE_NAME --region=REGION

# Rollback
gcloud run services update-traffic SERVICE_NAME \
  --to-revisions=REVISION_NAME=100 \
  --region=REGION
```

---

## Need Help?

- **GCP Cloud Run Docs:** https://cloud.google.com/run/docs
- **CORS Specification:** https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
- **Project Issues:** https://github.com/DSY1103G5/lunari/issues

---

## Summary

**For quick CORS fix:**
```bash
# Run from project root
cd usuario/infrastructure && ./redeploy.sh
cd ../../inventario/infrastructure && ./redeploy.sh
cd ../../carrito/infrastructure && ./redeploy.sh
```

**All services will be updated with:**
- ✅ CORS enabled
- ✅ Your Vercel domain allowed
- ✅ Localhost for development
- ✅ Proper headers and methods
- ✅ Credentials support

Your frontend should now be able to make cross-origin requests without CORS errors!
