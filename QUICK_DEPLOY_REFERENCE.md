# Quick Deploy Reference - CORS Fix

## ⚡ Quick Commands

### Deploy All Services (Fastest)
```bash
./redeploy-all-services.sh
```

### Deploy Individual Services
```bash
# Usuario
cd usuario/infrastructure && ./redeploy.sh

# Inventario
cd ../../inventario/infrastructure && ./redeploy.sh

# Carrito
cd ../../carrito/infrastructure && ./redeploy.sh
```

### Custom Origins
```bash
# Custom origins for all services
./redeploy-all-services.sh \
  lunari-microservices \
  us-central1 \
  "https://your-domain.com,http://localhost:3000"
```

---

## 🔧 Manual Environment Variable Update

Only use this if the CORS code is already deployed:

```bash
# Quick update for all services (copy-paste ready)
# Create temp env file
cat > /tmp/cors-env.txt << 'EOF'
CORS_ALLOWED_ORIGINS=https://dsy-1104-millan-munoz.vercel.app,http://localhost:3000,http://localhost:5173
EOF

# Update all services
gcloud run services update usuario-service --region=us-central1 --update-env-vars="^@^/tmp/cors-env.txt"
gcloud run services update inventario-service --region=us-central1 --update-env-vars="^@^/tmp/cors-env.txt"
gcloud run services update carrito-service --region=us-central1 --update-env-vars="^@^/tmp/cors-env.txt"

# Clean up
rm /tmp/cors-env.txt
```

**Note:** Using `^@^filename` syntax to read from file - this properly handles commas in values.

---

## ✅ Verification Commands

### Check Environment Variable
```bash
# Usuario
gcloud run services describe usuario-service \
  --region=us-central1 \
  --format="value(spec.template.spec.containers[0].env)" | grep CORS

# Inventario
gcloud run services describe inventario-service \
  --region=us-central1 \
  --format="value(spec.template.spec.containers[0].env)" | grep CORS

# Carrito
gcloud run services describe carrito-service \
  --region=us-central1 \
  --format="value(spec.template.spec.containers[0].env)" | grep CORS
```

### Test CORS with curl
```bash
# Test Usuario
curl -H "Origin: https://dsy-1104-millan-munoz.vercel.app" \
     -H "Access-Control-Request-Method: POST" \
     -X OPTIONS \
     https://user.aframuz.dev/api/v1/auth/login -v 2>&1 | grep -i "access-control"

# Test Inventario
curl -H "Origin: https://dsy-1104-millan-munoz.vercel.app" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS \
     https://inventory.aframuz.dev/api/v1/productos/activos -v 2>&1 | grep -i "access-control"

# Test Carrito
curl -H "Origin: https://dsy-1104-millan-munoz.vercel.app" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS \
     https://cart.aframuz.dev/api/v1/carts/test -v 2>&1 | grep -i "access-control"
```

Expected output should include:
```
< access-control-allow-origin: https://dsy-1104-millan-munoz.vercel.app
< access-control-allow-credentials: true
< access-control-allow-methods: GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD
```

### Check Service Logs
```bash
# View live logs
gcloud run services logs tail usuario-service --region=us-central1
gcloud run services logs tail inventario-service --region=us-central1
gcloud run services logs tail carrito-service --region=us-central1
```

---

## 🚨 Common Issues & Fixes

### Issue: "bash\r: No such file or directory"
```bash
./fix-line-endings.sh
```

### Issue: "Bad syntax for dict arg"
**Problem:** Commas in CORS origins are interpreted as multiple env vars.

**Fix:** Use the `^@^filename` syntax to read from a file:
```bash
# ✓ Correct - using file
cat > /tmp/cors.txt << 'EOF'
CORS_ALLOWED_ORIGINS=https://domain1.com,https://domain2.com
EOF
gcloud run services update service-name --update-env-vars="^@^/tmp/cors.txt"

# ✗ Wrong - direct commas
--update-env-vars="CORS_ALLOWED_ORIGINS=https://domain1.com,https://domain2.com"
```

The automated scripts handle this for you!

### Issue: Service not found
```bash
# Check services exist
gcloud run services list --region=us-central1

# If missing, deploy with original script first
cd usuario/infrastructure && ./deploy-to-gcp.sh
```

### Issue: CORS still not working
1. Clear browser cache (Ctrl+Shift+R)
2. Verify env var is set (see verification commands above)
3. Check service logs for errors
4. Ensure origin URL matches exactly (including https://)

---

## 📋 Project & Region Info

**Default Values:**
- Project ID: `lunari-microservices`
- Region: `us-central1`
- Services:
  - `usuario-service` (port 8081)
  - `inventario-service` (port 8082)
  - `carrito-service` (port 8083)

**Service URLs:**
- Usuario: https://user.aframuz.dev
- Inventario: https://inventory.aframuz.dev
- Carrito: https://cart.aframuz.dev

**Allowed Origins (Default):**
- https://dsy-1104-millan-munoz.vercel.app
- http://localhost:3000
- http://localhost:5173

---

## 📚 More Help

- Full guide: `GCP_CORS_CONFIGURATION_GUIDE.md`
- CORS fix guide: `CORS_FIX_README.md`
- Fix scripts: `./fix-line-endings.sh`

---

## 💾 One-Liner for Copy-Paste

### Deploy everything:
```bash
./redeploy-all-services.sh && echo "✅ All services deployed!"
```

### Quick env var update:
```bash
cat > /tmp/cors.txt << 'EOF'
CORS_ALLOWED_ORIGINS=https://dsy-1104-millan-munoz.vercel.app,http://localhost:3000,http://localhost:5173
EOF
for svc in usuario-service inventario-service carrito-service; do gcloud run services update $svc --region=us-central1 --update-env-vars="^@^/tmp/cors.txt"; done && rm /tmp/cors.txt
```

### Verify all CORS settings:
```bash
for svc in usuario-service inventario-service carrito-service; do echo "=== $svc ==="; gcloud run services describe $svc --region=us-central1 --format="value(spec.template.spec.containers[0].env)" | grep CORS; done
```

---

**That's it! Choose the command that fits your need and run it.** 🚀
