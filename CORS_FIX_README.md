# CORS Fix - Quick Start Guide

This guide helps you fix CORS errors in your LUNARi microservices deployed on GCP Cloud Run.

## Problem

Your frontend (https://dsy-1104-millan-munoz.vercel.app) is getting CORS errors when calling your backend APIs:

```
Access to fetch at 'https://inventory.aframuz.dev/api/v1/productos/activos'
from origin 'https://dsy-1104-millan-munoz.vercel.app' has been blocked by
CORS policy: No 'Access-Control-Allow-Origin' header is present on the
requested resource.
```

## Solution Applied

✅ Created `CorsConfig.java` for all three microservices
✅ Updated `SecurityConfig.java` (Usuario service) to enable CORS
✅ Added `CORS_ALLOWED_ORIGINS` configuration to `application.properties`
✅ Created redeploy scripts for easy deployment

## Quick Fix (Choose One)

> **Note:** If you get a `bash\r: No such file or directory` error, run `./fix-line-endings.sh` first to fix Windows line endings.

### Option 1: Redeploy All Services at Once (Recommended)

From the project root directory:

```bash
./redeploy-all-services.sh
```

This will:
- Rebuild all three microservices with CORS enabled
- Push to GCP Artifact Registry
- Update Cloud Run services
- Set `CORS_ALLOWED_ORIGINS` environment variable

### Option 2: Redeploy Services Individually

```bash
# Usuario Service
cd usuario/infrastructure
./redeploy.sh

# Inventario Service
cd ../../inventario/infrastructure
./redeploy.sh

# Carrito Service
cd ../../carrito/infrastructure
./redeploy.sh
```

### Option 3: Manual GCP Console Configuration

If you prefer not to redeploy, you can just update the environment variable:

```bash
# Create temporary env file
cat > /tmp/cors-env.txt << 'EOF'
CORS_ALLOWED_ORIGINS=https://dsy-1104-millan-munoz.vercel.app,http://localhost:3000,http://localhost:5173
EOF

# Set CORS for each service using file reference
gcloud run services update usuario-service \
  --region=us-central1 \
  --update-env-vars="^@^/tmp/cors-env.txt"

gcloud run services update inventario-service \
  --region=us-central1 \
  --update-env-vars="^@^/tmp/cors-env.txt"

gcloud run services update carrito-service \
  --region=us-central1 \
  --update-env-vars="^@^/tmp/cors-env.txt"

# Clean up
rm /tmp/cors-env.txt
```

**Note:** Option 3 requires that you've already deployed the CORS code changes at least once.

## Customization

### Add More Allowed Origins

```bash
# Example with multiple domains
./redeploy-all-services.sh \
  lunari-microservices \
  us-central1 \
  "https://dsy-1104-millan-munoz.vercel.app,https://production.com,https://staging.com"
```

### Individual Service with Custom Origins

```bash
cd usuario/infrastructure
./redeploy.sh lunari-microservices us-central1 "https://custom-domain.com"
```

## Verification

### 1. Check Environment Variable is Set

```bash
gcloud run services describe usuario-service \
  --region=us-central1 \
  --format="yaml(spec.template.spec.containers[0].env)" | grep CORS
```

Expected output:
```yaml
- name: CORS_ALLOWED_ORIGINS
  value: https://dsy-1104-millan-munoz.vercel.app,http://localhost:3000,http://localhost:5173
```

### 2. Test CORS with curl

```bash
curl -H "Origin: https://dsy-1104-millan-munoz.vercel.app" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS \
     https://inventory.aframuz.dev/api/v1/productos/activos \
     -v
```

Look for these headers in the response:
```
access-control-allow-origin: https://dsy-1104-millan-munoz.vercel.app
access-control-allow-credentials: true
access-control-allow-methods: GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD
```

### 3. Test from Frontend

Open your frontend application and check the browser console. The CORS errors should be gone!

## Files Created/Modified

### New Files
- `usuario/src/main/java/cl/duoc/lunari/api/user/config/CorsConfig.java`
- `inventario/src/main/java/cl/duoc/lunari/api/inventory/config/CorsConfig.java`
- `carrito/src/main/java/cl/duoc/lunari/api/cart/config/CorsConfig.java`
- `usuario/infrastructure/redeploy.sh`
- `inventario/infrastructure/redeploy.sh`
- `carrito/infrastructure/redeploy.sh`
- `redeploy-all-services.sh`
- `GCP_CORS_CONFIGURATION_GUIDE.md` (comprehensive guide)

### Modified Files
- `usuario/src/main/java/cl/duoc/lunari/api/user/config/SecurityConfig.java`
- `usuario/src/main/resources/application.properties`
- `inventario/src/main/resources/application.properties`
- `carrito/src/main/resources/application.properties`

## Default CORS Configuration

The CORS configuration allows:

**Origins:**
- `https://dsy-1104-millan-munoz.vercel.app` (your Vercel frontend)
- `http://localhost:3000` (local development)
- `http://localhost:5173` (Vite dev server)

**Methods:**
- GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD

**Headers:**
- Authorization
- Content-Type
- Accept
- X-Requested-With
- X-API-Key (for Inventario service)
- Origin
- Access-Control-Request-Method
- Access-Control-Request-Headers

**Other:**
- Credentials: ✅ Enabled
- Max Age: 3600 seconds (1 hour)

## Troubleshooting

### Script error: "bash\r: No such file or directory"

**Cause:** Windows line endings (CRLF) instead of Unix line endings (LF)

**Solution:**
```bash
# Fix all shell scripts automatically
./fix-line-endings.sh

# Or manually fix specific scripts
sed -i 's/\r$//' usuario/infrastructure/redeploy.sh
sed -i 's/\r$//' inventario/infrastructure/redeploy.sh
sed -i 's/\r$//' carrito/infrastructure/redeploy.sh
sed -i 's/\r$//' redeploy-all-services.sh
```

### Deployment error: "Bad syntax for dict arg"

**Cause:** gcloud interprets commas in the CORS origins as multiple environment variables

**Solution:** The redeploy scripts now use a temp file approach. If you're using manual gcloud commands, use the `^@^filename` syntax:

```bash
# Correct - using file reference
cat > /tmp/cors.txt << 'EOF'
CORS_ALLOWED_ORIGINS=https://domain.com,http://localhost:3000
EOF
gcloud run services update usuario-service \
  --region=us-central1 \
  --update-env-vars="^@^/tmp/cors.txt"
rm /tmp/cors.txt

# Wrong - direct commas will fail
gcloud run services update usuario-service \
  --region=us-central1 \
  --update-env-vars="CORS_ALLOWED_ORIGINS=https://domain.com,http://localhost:3000"
```

### CORS errors still appearing?

1. **Clear browser cache** - Hard refresh (Ctrl+Shift+R / Cmd+Shift+R)
2. **Check deployment status**:
   ```bash
   gcloud run services list --region=us-central1
   ```
3. **View service logs**:
   ```bash
   gcloud run services logs tail usuario-service --region=us-central1
   ```
4. **Verify the origin matches exactly** - including https:// and no trailing slash

### Need to add a new origin?

Just redeploy with the new origin in the third parameter:
```bash
./redeploy-all-services.sh lunari-microservices us-central1 \
  "https://dsy-1104-millan-munoz.vercel.app,https://new-domain.com"
```

### Want to remove CORS restrictions (NOT RECOMMENDED)?

For development only, you could allow all origins by setting:
```bash
CORS_ALLOWED_ORIGINS="*"
```
**⚠️ Warning:** Never use `*` in production with credentials!

## Additional Resources

- **Comprehensive Guide:** See `GCP_CORS_CONFIGURATION_GUIDE.md` for advanced configuration
- **GCP Cloud Run Docs:** https://cloud.google.com/run/docs
- **CORS Specification:** https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS

## Support

If you encounter issues:
1. Check the comprehensive guide: `GCP_CORS_CONFIGURATION_GUIDE.md`
2. View service logs: `gcloud run services logs tail SERVICE_NAME --region=us-central1`
3. Verify environment variables are set correctly
4. Test with curl to isolate frontend vs backend issues

---

**Quick Command Reference:**

```bash
# Redeploy all services
./redeploy-all-services.sh

# Check service status
gcloud run services list --region=us-central1

# View logs
gcloud run services logs tail usuario-service --region=us-central1

# Test CORS
curl -H "Origin: https://dsy-1104-millan-munoz.vercel.app" \
     -X OPTIONS https://inventory.aframuz.dev/api/v1/productos/activos -v
```

**That's it! Your CORS issues should be resolved.** 🎉
