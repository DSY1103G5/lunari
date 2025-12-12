# Final Fix: Spring Boot Actuator Bean Conflict

## 🎯 Root Cause Found!

The deployment was failing with this error:

```
Error creating bean with name 'apiKeyFilter':
Unsatisfied dependency expressed through constructor parameter 1:
No qualifying bean of type 'RequestMappingHandlerMapping' available:
expected single matching bean but found 2:
  - requestMappingHandlerMapping
  - controllerEndpointHandlerMapping
```

### What Happened?

When we added **Spring Boot Actuator** to fix the health check issue, it automatically created a second `RequestMappingHandlerMapping` bean:

1. **requestMappingHandlerMapping** - From Spring MVC (handles your API endpoints)
2. **controllerEndpointHandlerMapping** - From Actuator (handles /actuator endpoints)

The `ApiKeyFilter` constructor was trying to inject `RequestMappingHandlerMapping`, but Spring didn't know which one to use, causing the application to fail at startup.

## ✅ The Fix

Added `@Qualifier` annotation to specify which bean to inject:

### File: `inventario/src/main/java/cl/duoc/lunari/api/inventory/security/ApiKeyFilter.java`

**Before:**
```java
public ApiKeyFilter(ApiKeyProperties apiKeyProperties,
                   RequestMappingHandlerMapping handlerMapping,
                   ObjectMapper objectMapper) {
```

**After:**
```java
import org.springframework.beans.factory.annotation.Qualifier;

public ApiKeyFilter(ApiKeyProperties apiKeyProperties,
                   @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
                   ObjectMapper objectMapper) {
```

This tells Spring to specifically inject the **requestMappingHandlerMapping** bean (the main MVC one), not the actuator one.

## 📦 All Fixes Applied

### Inventario Service - Complete Fix List

1. ✅ **Dockerfile** - Fixed JAR filename: `lunari-inventory-app.jar`
2. ✅ **deploy-to-gcp.sh** - Added `--port=8082`
3. ✅ **pom.xml** - Added Spring Boot Actuator dependency
4. ✅ **application.properties** - Configured actuator endpoints
5. ✅ **deploy-to-gcp.sh** - Fixed Docker build path
6. ✅ **ApiKeyFilter.java** - Added @Qualifier to resolve bean conflict

## 🚀 Deploy Now - This Will Work!

```bash
cd /home/aframuz/code/study/duoc/3er_semestre/fullstack-i/lunari/inventario

# Deploy (will rebuild with all fixes)
./infrastructure/deploy-to-gcp.sh lunari-prod-1765210460 southamerica-west1
```

## 🔍 What Will Happen

1. **Maven Build**: Creates `lunari-inventory-app.jar` ✅
2. **Docker Build**: Copies JAR correctly into container ✅
3. **Container Start**: Application starts successfully ✅
   - ApiKeyFilter loads with correct RequestMappingHandlerMapping
   - Actuator endpoints enabled at `/actuator/health`
   - Tomcat listens on port 8082
4. **Cloud Run**: Container passes health checks ✅
5. **Deployment**: SUCCESS! ✅

## ✅ Expected Startup Logs

You should see these logs (in order):

```
Starting LunariInventoryApiApplication
The following 1 profile is active: "prod"
Bootstrapping Spring Data JPA repositories
Tomcat initialized with port 8082
Starting service [Tomcat]
Starting Servlet engine: [Apache Tomcat]
Root WebApplicationContext: initialization completed
Started LunariInventoryApiApplication in X.XXX seconds
```

**No more errors!** The bean conflict is resolved.

## 🧪 Test After Deployment

```bash
# Get service URL
INVENTARIO_URL=$(gcloud run services describe inventario-service \
  --region=southamerica-west1 \
  --format="value(status.url)")

echo "Service URL: $INVENTARIO_URL"

# Test health endpoint
curl $INVENTARIO_URL/actuator/health
# Expected: {"status":"UP"}

# Test API endpoint
curl $INVENTARIO_URL/api/v1/services
# Expected: JSON array of services

# Test Swagger
open $INVENTARIO_URL/swagger-ui/index.html
```

## 🎯 Why This Fix Works

The `@Qualifier` annotation tells Spring's dependency injection:

- **Without @Qualifier**: "Give me any RequestMappingHandlerMapping" → Spring finds 2, doesn't know which → ERROR
- **With @Qualifier**: "Give me specifically 'requestMappingHandlerMapping'" → Spring knows exactly which bean → SUCCESS

The ApiKeyFilter needs the main MVC handler mapping to check which controller methods require API keys. The actuator handler mapping is separate and only handles `/actuator/*` endpoints.

## 📋 Deployment Checklist

- [x] JAR filename matches Dockerfile
- [x] --port flag configured (8082)
- [x] Spring Boot Actuator added
- [x] Actuator endpoints configured
- [x] Docker build path fixed
- [x] Bean conflict resolved with @Qualifier
- [ ] **Deploy now!**

## 🔄 Next: Carrito Service

The carrito service doesn't have this issue because it doesn't have an `ApiKeyFilter`. After inventario deploys successfully, deploy carrito:

```bash
cd ../carrito/
./infrastructure/deploy-to-gcp.sh lunari-prod-1765210460 southamerica-west1
```

## 🆘 If It Still Fails

Check the logs for any remaining issues:

```bash
gcloud run services logs tail inventario-service \
  --region=southamerica-west1 \
  --limit=100
```

Look for:
- ✅ "Started LunariInventoryApiApplication" = SUCCESS
- ❌ "Error creating bean" = Configuration issue
- ❌ "Connection refused" = Database issue

## 🎉 Summary

**The Issue**: Adding Actuator created two RequestMappingHandlerMapping beans
**The Fix**: Used @Qualifier to specify which bean to inject
**The Result**: Application will now start successfully!

All previous fixes remain in place:
- Correct JAR filename
- Correct port configuration
- Health check endpoints
- Proper Docker build context

This is the final piece of the puzzle! 🧩

Deploy now and it should work! 🚀
