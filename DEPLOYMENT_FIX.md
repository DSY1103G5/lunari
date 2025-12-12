# Deployment Fix Summary

## Issues Fixed

### 1. Missing `--port` Flag in Inventario Deployment Script
**Problem**: The inventario deployment script was missing the `--port=8082` flag, causing Cloud Run to not know which port to forward traffic to.

**Fix**: Added `--port=8082` to `inventario/infrastructure/deploy-to-gcp.sh` line 160.

### 2. Missing Spring Boot Actuator Dependency
**Problem**: Both inventario and carrito services were missing the Spring Boot Actuator dependency, which provides the `/actuator/health` endpoint that Cloud Run and the Dockerfile health checks require.

**Fix**: Added the following dependency to both `pom.xml` files:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 3. Missing Actuator Configuration
**Problem**: Actuator endpoints were not configured in `application.properties`.

**Fix**: Added the following configuration to both services' `application.properties`:
```properties
# Actuator endpoints
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
management.health.defaults.enabled=true
```

## Files Modified

### Inventario Service
- ✅ `inventario/infrastructure/deploy-to-gcp.sh` - Added `--port=8082`
- ✅ `inventario/pom.xml` - Added actuator dependency
- ✅ `inventario/src/main/resources/application.properties` - Added actuator config

### Carrito Service
- ✅ `carrito/pom.xml` - Added actuator dependency
- ✅ `carrito/src/main/resources/application.properties` - Added actuator config

## Next Steps

### Redeploy Inventario Service

```bash
cd inventario/
./infrastructure/deploy-to-gcp.sh [PROJECT_ID] [REGION]
```

After deployment completes, test the health endpoint:
```bash
curl https://[CLOUD-RUN-URL]/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### Deploy Carrito Service

```bash
cd carrito/
./infrastructure/deploy-to-gcp.sh [PROJECT_ID] [REGION]
```

Test:
```bash
curl https://[CLOUD-RUN-URL]/actuator/health
```

## Verification Commands

### Check Service Health
```bash
# Inventario
curl https://[INVENTARIO-URL]/actuator/health

# Carrito
curl https://[CARRITO-URL]/actuator/health
```

### View Logs
```bash
# Inventario
gcloud run services logs tail inventario-service --region=[REGION]

# Carrito
gcloud run services logs tail carrito-service --region=[REGION]
```

### Check Service Status
```bash
gcloud run services list --region=[REGION]
```

## What Cloud Run Expects

Cloud Run requires:
1. **Port Configuration**: `--port` flag matching the container's exposed port
2. **Health Check Endpoint**: `/actuator/health` must return 200 OK
3. **Quick Startup**: Container must start and be ready within the timeout (default 300s)
4. **Listen on PORT**: Application must listen on the `PORT` environment variable Cloud Run provides

## Common Errors and Solutions

### "Container failed to start and listen on the port"
- ✅ **FIXED**: Added `--port=8082` to deployment script
- ✅ **FIXED**: Added actuator for health checks
- Check: `server.port=${PORT:8082}` in application.properties

### "Health check failed"
- ✅ **FIXED**: Added actuator dependency
- ✅ **FIXED**: Added actuator configuration
- Verify: `curl [URL]/actuator/health` returns 200

### "Database connection failed"
- Check: NeonDB secrets are correctly stored in Secret Manager
- Verify: Secrets are accessible by Cloud Run service account
- Test: Connection string is correct

## Architecture Notes

### Port Configuration
- **Usuario**: 8081 (already configured correctly)
- **Inventario**: 8082 (now fixed)
- **Carrito**: 8083 (already configured correctly)

### Health Check Endpoints
All services now expose:
- `/actuator/health` - Overall health status
- `/actuator/info` - Application information

### Environment Variables
Cloud Run automatically sets:
- `PORT` - The port your container should listen on
- Database credentials from Secret Manager
- Custom env vars specified in deployment script

## Testing After Deployment

1. **Health Check**:
   ```bash
   curl https://[SERVICE-URL]/actuator/health
   ```

2. **API Endpoints**:
   ```bash
   # Inventario - List services
   curl https://[INVENTARIO-URL]/api/v1/services

   # Carrito - Get cart
   curl https://[CARRITO-URL]/api/v1/carts/[USER_ID]
   ```

3. **Swagger UI**:
   - Inventario: `https://[URL]/swagger-ui/index.html`
   - Carrito: `https://[URL]/swagger-ui/index.html`

## Reference

For complete deployment guide, see: `GCP_DEPLOYMENT_COMPLETE_GUIDE.md`

For quick commands, see: `QUICK_DEPLOYMENT_REFERENCE.md`
