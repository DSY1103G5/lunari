# API Security - Two-Tier API Key System

## Overview

The Inventario service implements a **two-tier API key authentication system** to protect sensitive operations while keeping read-only endpoints publicly accessible.

## Security Levels

### 🔓 Public Endpoints (No API Key Required)
All `GET` endpoints are publicly accessible for browsing products:

- `GET /api/v1/productos` - List all products
- `GET /api/v1/productos/activos` - List active products
- `GET /api/v1/productos/{id}` - Get product by ID
- `GET /api/v1/productos/code/{code}` - Get product by code
- `GET /api/v1/productos/buscar?nombre={name}` - Search products
- `GET /api/v1/productos/categoria/{id}` - Products by category
- `GET /api/v1/productos/marca/{marca}` - Products by brand
- `GET /api/v1/productos/en-stock` - Products in stock
- `GET /api/v1/productos/precio?min={min}&max={max}` - Products by price range
- `GET /api/v1/productos/rating?min={rating}` - Products by rating
- `GET /api/v1/productos/tag/{tag}` - Products by tag

### 🔑 SERVICE Key Endpoints
Limited access for service-to-service communication (e.g., Carrito service):

- `PATCH /api/v1/productos/{id}/reducir-stock?cantidad={qty}` - Reduce product stock

**Note:** ADMIN key also works for SERVICE endpoints.

### 🔐 ADMIN Key Endpoints
Full administrative access to write operations:

- `POST /api/v1/productos` - Create new product
- `PUT /api/v1/productos/{id}` - Update product
- `PATCH /api/v1/productos/{id}/activar` - Activate product
- `PATCH /api/v1/productos/{id}/desactivar` - Deactivate product
- `PATCH /api/v1/productos/{id}/stock?stock={qty}` - Update stock
- `DELETE /api/v1/productos/{id}` - Delete product

## Setup

### 1. Generate Secure API Keys

```bash
# Generate admin key
openssl rand -hex 32

# Generate service key
openssl rand -hex 32
```

### 2. Configure Environment Variables

Create a `.env` file in `src/main/resources/`:

```env
# Copy from .env.example
ADMIN_API_KEY=your-generated-admin-key
SERVICE_API_KEY=your-generated-service-key
API_SECURITY_ENABLED=true
```

### 3. For EC2 Deployment

Add environment variables to EC2 instance:

```bash
# Edit /etc/environment or ~/.bashrc
export ADMIN_API_KEY="your-admin-key"
export SERVICE_API_KEY="your-service-key"
export API_SECURITY_ENABLED="true"
```

Or pass as command-line arguments:

```bash
java -jar app.jar \
  --spring.profiles.active=prod \
  --ADMIN_API_KEY=your-admin-key \
  --SERVICE_API_KEY=your-service-key
```

## Usage

### Making Authenticated Requests

Include the API key in the `X-API-Key` header:

#### Admin Request Example
```bash
curl -X POST http://localhost:8082/api/v1/productos \
  -H "X-API-Key: your-admin-key" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "PROD001",
    "nombre": "Producto Test",
    "precio_clp": 10000,
    "stock": 100
  }'
```

#### Service Request Example (Carrito reducing stock)
```bash
curl -X PATCH "http://localhost:8082/api/v1/productos/1/reducir-stock?cantidad=5" \
  -H "X-API-Key: your-service-key"
```

#### Public Request Example (No key needed)
```bash
curl http://localhost:8082/api/v1/productos
```

## Error Responses

### 401 Unauthorized - Missing API Key
```json
{
  "success": false,
  "message": "API key is required. Include 'X-API-Key' header.",
  "data": null,
  "statusCode": 401
}
```

### 401 Unauthorized - Invalid API Key
```json
{
  "success": false,
  "message": "Invalid or insufficient API key permissions.",
  "data": null,
  "statusCode": 401
}
```

## Integration with Carrito Service

The Carrito service should use the **SERVICE key** when reducing stock:

```java
// In CarritoService or RestTemplate client
HttpHeaders headers = new HttpHeaders();
headers.set("X-API-Key", serviceApiKey);
// Make request to /api/v1/productos/{id}/reducir-stock
```

## Disabling Security (Development Only)

Set in `.env`:
```env
API_SECURITY_ENABLED=false
```

**⚠️ WARNING:** Never disable in production!

## Security Best Practices

1. **Never commit** `.env` files with real keys to version control
2. **Rotate keys** regularly (every 90 days recommended)
3. **Use different keys** per environment (dev, staging, production)
4. **Store keys securely** (AWS Secrets Manager, HashiCorp Vault, etc.)
5. **Monitor usage** - Log all authenticated requests for audit trails
6. **Limit key scope** - Use SERVICE key in Carrito, not ADMIN key

## Architecture

The security system uses:

- `ApiKeyProperties` - Configuration from environment variables
- `ApiKeyType` - Enum defining ADMIN vs SERVICE access levels
- `@RequireApiKey` - Annotation to mark protected endpoints
- `ApiKeyFilter` - Servlet filter that intercepts and validates requests

## Future Enhancements

Consider upgrading to:
- JWT tokens with expiration for better security
- Rate limiting per API key
- API key rotation mechanism
- Detailed audit logging with request tracking
- Integration with API Gateway for centralized auth
