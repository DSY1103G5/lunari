# Complete Deployment Fix - Inventario & Carrito Services

## Root Cause Analysis

The deployment was failing with: **"Error: Unable to access jarfile app.jar"**

### Issues Found and Fixed

#### 1. ⚠️ JAR Filename Mismatch (Critical) - Inventario
**Problem**:
- `pom.xml` builds: `lunari-inventory-app.jar` (line 84: `<finalName>lunari-inventory-app</finalName>`)
- `Dockerfile` looked for: `lunari-inventory-api-*.jar`

**Fix**: Updated `inventario/Dockerfile` line 22:
```dockerfile
# Before
COPY --from=build /app/target/lunari-inventory-api-*.jar app.jar

# After
COPY --from=build /app/target/lunari-inventory-app.jar app.jar
```

#### 2. 🔧 Missing --port Flag (Critical) - Inventario
**Problem**: Cloud Run didn't know which port to route traffic to.

**Fix**: Added `--port=8082` to `inventario/infrastructure/deploy-to-gcp.sh` line 160

#### 3. 📦 Missing Spring Boot Actuator (Critical)
**Problem**: No `/actuator/health` endpoint for health checks.

**Fix**: Added to both `inventario/pom.xml` and `carrito/pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

#### 4. ⚙️ Missing Actuator Configuration
**Fix**: Added to both `application.properties` files:
```properties
# Actuator endpoints
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
management.health.defaults.enabled=true
```

#### 5. 📂 Docker Build Context Paths
**Problem**: Inconsistent paths between usuario, inventario, and carrito scripts.

**Fix**: Standardized all deployment scripts to:
```bash
cd ..
docker build -t [...] -f Dockerfile .
cd infrastructure
```

## Files Modified Summary

### Inventario Service
- ✅ `inventario/Dockerfile` - Fixed JAR filename
- ✅ `inventario/infrastructure/deploy-to-gcp.sh` - Added --port, fixed build path
- ✅ `inventario/pom.xml` - Added actuator dependency
- ✅ `inventario/src/main/resources/application.properties` - Added actuator config

### Carrito Service
- ✅ `carrito/infrastructure/deploy-to-gcp.sh` - Fixed build path
- ✅ `carrito/pom.xml` - Added actuator dependency
- ✅ `carrito/src/main/resources/application.properties` - Added actuator config

## 🚀 Deploy Now

### Clean Build and Deploy Inventario

```bash
cd /home/aframuz/code/study/duoc/3er_semestre/fullstack-i/lunari/inventario

# Run deployment (will rebuild with correct JAR name)
./infrastructure/deploy-to-gcp.sh lunari-prod-1765210460 southamerica-west1
```

The deployment will now:
1. ✅ Build the correct JAR: `lunari-inventory-app.jar`
2. ✅ Copy it correctly in Dockerfile
3. ✅ Start the application on port 8082
4. ✅ Respond to health checks at `/actuator/health`
5. ✅ Deploy successfully to Cloud Run

### Expected Output

```
[INFO] Building Inventario Service...
[INFO] Pushing Inventario Service to Artifact Registry...
[INFO] Deploying Inventario Service to Cloud Run...
[SUCCESS] Inventario Service deployed: https://[...].run.app

Service URLs:
  Inventario Service: https://inventario-service-[...].run.app
  Swagger UI: https://inventario-service-[...].run.app/swagger-ui/index.html

Test Commands:
  Health Check:
    curl https://inventario-service-[...].run.app/actuator/health
```

### Verify Deployment

```bash
# Get the service URL
INVENTARIO_URL=$(gcloud run services describe inventario-service \
  --region=southamerica-west1 \
  --format="value(status.url)")

echo "Inventario URL: $INVENTARIO_URL"

# Test health endpoint
curl $INVENTARIO_URL/actuator/health
# Expected: {"status":"UP"}

# Test API
curl $INVENTARIO_URL/api/v1/services

# View logs
gcloud run services logs tail inventario-service --region=southamerica-west1
```

## 🎯 Deploy Carrito Service (After Inventario)

```bash
cd /home/aframuz/code/study/duoc/3er_semestre/fullstack-i/lunari/carrito

# Deploy
./infrastructure/deploy-to-gcp.sh lunari-prod-1765210460 southamerica-west1

# Test
CARRITO_URL=$(gcloud run services describe carrito-service \
  --region=southamerica-west1 \
  --format="value(status.url)")

curl $CARRITO_URL/actuator/health
```

## 📊 What Was The Problem?

The Docker container was failing to start because:

1. **Build succeeded** - Maven created `lunari-inventory-app.jar`
2. **Docker COPY failed** - Looked for `lunari-inventory-api-*.jar` (didn't exist)
3. **Container started** - But JAR file `app.jar` was empty/missing
4. **Java failed** - "Unable to access jarfile app.jar"
5. **Container crashed** - Before it could listen on port 8082
6. **Cloud Run timeout** - Container never became ready

The fix ensures:
- ✅ Correct JAR filename in Dockerfile
- ✅ JAR file is properly copied
- ✅ Application starts and listens on port 8082
- ✅ Health checks pass
- ✅ Cloud Run sees container as ready

## 🔍 Debugging Tips

If deployment still fails:

### Check Build Logs
```bash
# The Docker build should show:
# Step 12/16 : COPY --from=build /app/target/lunari-inventory-app.jar app.jar
# ---> Using cache
```

### Check Container Logs
```bash
gcloud run services logs tail inventario-service \
  --region=southamerica-west1 \
  --limit=100
```

Look for:
- ✅ "Started LunariInventoryApiApplication in X seconds"
- ❌ "Unable to access jarfile"
- ❌ "Address already in use" (port conflict)
- ❌ "Connection refused" (database issue)

### Verify Secrets
```bash
# Test database connection
gcloud secrets versions access latest --secret=db-host-inventario
gcloud secrets versions access latest --secret=db-name-inventario

# Test from Cloud Shell (if needed)
psql "postgresql://$(gcloud secrets versions access latest --secret=db-user-inventario):$(gcloud secrets versions access latest --secret=db-password-inventario)@$(gcloud secrets versions access latest --secret=db-host-inventario):5432/$(gcloud secrets versions access latest --secret=db-name-inventario)?sslmode=require"
```

## 🎉 Success Indicators

When deployment succeeds, you'll see:
1. ✅ Docker image pushed to Artifact Registry
2. ✅ Cloud Run service deployed
3. ✅ Service URL displayed
4. ✅ Health check returns `{"status":"UP"}`
5. ✅ Swagger UI accessible
6. ✅ API endpoints respond

## Next Steps After Successful Deployment

1. **Setup Custom Domains**:
   ```bash
   cd inventario/
   ./infrastructure/setup-load-balancer.sh lunari-prod-1765210460 \
     southamerica-west1 inventario-service inventory.aframuz.dev
   ```

2. **Update Carrito Service URLs** (after all services deployed):
   ```bash
   gcloud run services update carrito-service \
     --region=southamerica-west1 \
     --set-env-vars="USUARIO_SERVICE_URL=https://user.aframuz.dev,INVENTARIO_SERVICE_URL=https://inventory.aframuz.dev"
   ```

3. **Test Inter-Service Communication**:
   ```bash
   # Create a cart (will call usuario to validate user)
   curl -X POST https://cart.aframuz.dev/api/v1/carts \
     -H "Content-Type: application/json" \
     -d '{"userId":"test-user-id"}'
   ```

## 🆘 Still Having Issues?

1. Check Cloud Run logs URL provided in error message
2. Verify all secrets exist: `gcloud secrets list | grep -E "(inventario|carrito)"`
3. Ensure NeonDB is accessible: test connection from Cloud Shell
4. Check Artifact Registry: `gcloud artifacts docker images list southamerica-west1-docker.pkg.dev/lunari-prod-1765210460/lunari-services`
5. Verify service account permissions: Cloud Run needs access to secrets

All fixes are complete - deployment should now work! 🚀
