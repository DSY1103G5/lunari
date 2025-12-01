# LUNARi Microservices API Documentation

Complete API documentation for the LUNARi e-commerce platform microservices.

**Version:** 1.0
**Last Updated:** December 1, 2025

## Table of Contents

1. [Overview](#overview)
2. [Service Architecture](#service-architecture)
3. [Usuario Service API](#usuario-service-api)
4. [Inventario Service API](#inventario-service-api)
5. [Carrito Service API](#carrito-service-api)
6. [Common Response Formats](#common-response-formats)
7. [Error Handling](#error-handling)
8. [Testing](#testing)

---

## Overview

LUNARi is a microservices-based e-commerce platform consisting of three main services:

- **Usuario (User Service)** - Port 8081 - User management, authentication, gamification
- **Inventario (Inventory Service)** - Port 8082 - Product and category management
- **Carrito (Cart Service)** - Port 8083 - Shopping cart, orders, and payment processing

All services use:
- **Spring Boot** 3.4.x
- **REST** with JSON responses
- **OpenAPI/Swagger** documentation
- **PostgreSQL** (production) / **H2** (testing)
- **HATEOAS** (Usuario service only)

---

## Service Architecture

```
┌─────────────────────┐
│   Usuario Service   │  Port 8081 (DynamoDB)
└──────────┬──────────┘
           │
           │ REST API Calls
           │
┌──────────▼──────────┐      ┌─────────────────────┐
│  Carrito Service    │◄─────┤ Inventario Service  │
│     Port 8083       │      │     Port 8082       │
│    (PostgreSQL)     │      │    (PostgreSQL)     │
└─────────────────────┘      └─────────────────────┘
           │
           │ Transbank Integration
           ▼
   ┌───────────────┐
   │   Payments    │
   └───────────────┘
```

### Inter-Service Communication

The **Carrito** service communicates with:
- **Usuario Service**: Validates user existence
- **Inventario Service**: Validates products and stock availability
- **Transbank**: Payment gateway integration

---

## Usuario Service API

**Base URL:** `http://localhost:8081/api/v1`
**Swagger UI:** `http://localhost:8081/swagger-ui/index.html`
**Database:** AWS DynamoDB
**Features:** Full HATEOAS support with dynamic hypermedia links

### User Management Endpoints

#### Get All Users
```http
GET /users
GET /users?limit=10&paginationToken={token}
```
**Query Parameters:**
- `limit` (optional, default: 10) - Maximum results per page
- `paginationToken` (optional) - Token for next page

**Response:**
```json
{
  "status": "success",
  "data": {
    "_embedded": {
      "users": [...]
    },
    "_links": {
      "self": { "href": "/api/v1/users" },
      "paginated": { "href": "/api/v1/users/paginated" }
    }
  }
}
```

#### Get Paginated Users with Filters
```http
GET /users/paginated?limit=10&paginationToken={token}&active=true&roleName=CLIENT
```
**Query Parameters:**
- `limit` (optional, default: 10)
- `paginationToken` (optional)
- `active` (optional) - Filter by active status
- `roleName` (optional) - Filter by role

#### Get User by ID
```http
GET /users/{userId}
```
**Path Parameters:**
- `userId` (string/UUID) - User ID

**Response:** User object with HATEOAS links
```json
{
  "status": "success",
  "data": {
    "userId": "uuid-here",
    "nombre": "John",
    "apellido": "Doe",
    "email": "john@example.com",
    "active": true,
    "role": { "roleId": 3, "name": "CLIENT" },
    "_links": {
      "self": { "href": "/api/v1/users/{userId}" },
      "activate": { "href": "/api/v1/users/{userId}/status?active=true" }
    }
  }
}
```

#### Get User by Email
```http
GET /users/email?email={email}
```
**Query Parameters:**
- `email` (required) - User email address

#### Register New User
```http
POST /users/register
Content-Type: application/json
```
**Request Body:**
```json
{
  "username": "jdoe",
  "email": "john@example.com",
  "password": "securePassword123",
  "personal": {
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+56912345678"
  }
}
```
**Response:** 201 Created with user object

#### Login (Authenticate User)
```http
POST /users/login
Content-Type: application/json
```
**Request Body:**
```json
{
  "identifier": "john@example.com",
  "password": "securePassword123"
}
```
**Notes:**
- `identifier` can be either email or username
- Password validation is currently plaintext (use BCrypt in production)

**Response:** 200 OK
```json
{
  "status": "success",
  "data": {
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "username": "jdoe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "role": "CLIENT",
    "roleId": 3,
    "isActive": true,
    "isVerified": true,
    "sessionToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "level": "Gold",
    "points": 1500,
    "message": "¡Bienvenido de vuelta, John Doe!"
  }
}
```

**Error Response:** 401 Unauthorized
```json
{
  "status": "error",
  "message": "Credenciales inválidas",
  "code": 401
}
```

#### Update User
```http
PUT /users/{userId}
Content-Type: application/json
```
**Request Body:** User object with updates

#### Delete User
```http
DELETE /users/{userId}
```
**Response:** 204 No Content

### Search and Filtering

#### Search Users
```http
GET /users/search?query={searchTerm}&limit=10&paginationToken={token}
```
Searches by name, last name, or email

#### Get Users by Role
```http
GET /users/role/{roleName}?limit=10&paginationToken={token}
```
**Path Parameters:**
- `roleName` - ADMIN, PRODUCT_OWNER, CLIENT, DEVOPS

### User Status Management

#### Update User Status
```http
PATCH /users/{userId}/status?active={true|false}
```

#### Change Password
```http
PATCH /users/{userId}/password
Content-Type: application/json

"newSecurePassword123"
```

#### Verify User
```http
POST /users/verify?token={verificationToken}
```

### Role Management

#### Assign Role by ID
```http
PATCH /users/{userId}/role/id/{roleId}
```
**Role IDs:**
- 1 = ADMIN
- 2 = PRODUCT_OWNER
- 3 = CLIENT
- 4 = DEVOPS

#### Assign Role by Name
```http
PATCH /users/{userId}/role/{roleName}
```

### Gamification

#### Add Points
```http
POST /users/{userId}/points?points={amount}
```
**Query Parameters:**
- `points` - Points to add (can be negative)

#### Update Level
```http
PATCH /users/{userId}/level?newLevel={level}
```

### Favorites Management

#### Add Favorite Service
```http
POST /users/{userId}/favorites/{serviceId}
```

#### Remove Favorite Service
```http
DELETE /users/{userId}/favorites/{serviceId}
```

#### Get User Favorites
```http
GET /users/{userId}/favorites
```
**Response:** Array of service IDs

### Purchase and Review Tracking

#### Record Purchase
```http
POST /users/{userId}/purchase?amount={amount}
```

#### Record Review
```http
POST /users/{userId}/review?rating={1.0-5.0}
```

### User Preferences

#### Update Preferences
```http
PATCH /users/{userId}/preferences
Content-Type: application/json
```
**Request Body:**
```json
{
  "notifications": {
    "email": true,
    "sms": false,
    "push": true
  },
  "client": {
    "favoriteCategories": ["electronics", "gaming"],
    "interests": ["PC", "PlayStation"]
  }
}
```

#### Update Address
```http
PATCH /users/{userId}/address
Content-Type: application/json
```
**Request Body:**
```json
{
  "street": "Av. Principal 123",
  "city": "Santiago",
  "region": "RM",
  "country": "Chile",
  "postalCode": "8320000"
}
```

#### Update Metadata
```http
PATCH /users/{userId}/metadata
Content-Type: application/json
```

### Statistics

#### Get User Statistics
```http
GET /users/stats
```
Returns system-wide user statistics

### API Root

#### Get API Links
```http
GET /users/api
```
Returns navigation links for the entire API

---

## Role Service Endpoints

**Base URL:** `http://localhost:8081/api/v1/roles`

#### Get All Roles
```http
GET /roles
```

#### Get Role by ID
```http
GET /roles/{roleId}
```

#### Get Role by Name
```http
GET /roles/name/{roleName}
```

#### Get Default Role
```http
GET /roles/default
```
Returns CLIENT role

#### Check Role Exists by ID
```http
GET /roles/{roleId}/exists
```

#### Check Role Exists by Name
```http
GET /roles/name/{roleName}/exists
```

---

## Inventario Service API

**Base URL:** `http://localhost:8082/api/v1`
**Swagger UI:** `http://localhost:8082/swagger-ui/index.html`
**Database:** PostgreSQL

### Product Endpoints

**Base Path:** `/productos`

#### Get All Products
```http
GET /productos
```

#### Get Active Products
```http
GET /productos/activos
```

#### Get Product by ID
```http
GET /productos/{id}
```
**Path Parameters:**
- `id` (integer) - Product ID

#### Get Product by Code
```http
GET /productos/code/{code}
```
**Path Parameters:**
- `code` (string) - Product code (SKU)

#### Search Products by Name
```http
GET /productos/buscar?nombre={searchTerm}
```

#### Get Products by Category
```http
GET /productos/categoria/{categoriaId}
```

#### Get Products by Brand
```http
GET /productos/marca/{marca}
```

#### Get Products in Stock
```http
GET /productos/en-stock
```

#### Get Products by Price Range
```http
GET /productos/precio?min={minPrice}&max={maxPrice}
```
**Query Parameters:**
- `min` (integer) - Minimum price
- `max` (integer) - Maximum price

#### Get Products by Rating
```http
GET /productos/rating?min={minRating}
```
**Query Parameters:**
- `min` (decimal) - Minimum rating (e.g., 4.0)

#### Get Products by Tag
```http
GET /productos/tag/{tag}
```

#### Create Product
```http
POST /productos
Content-Type: application/json
```
**Request Body:**
```json
{
  "code": "PROD-001",
  "nombre": "Gaming Laptop",
  "descripcion": "High-performance gaming laptop",
  "precio": 1500000,
  "stock": 10,
  "categoriaId": "electronics",
  "marca": "ASUS",
  "imagenUrl": "https://example.com/image.jpg",
  "activo": true,
  "rating": 4.5,
  "totalReviews": 120,
  "tags": ["gaming", "laptop", "RGB"]
}
```
**Response:** 201 Created

#### Update Product
```http
PUT /productos/{id}
Content-Type: application/json
```
**Request Body:** Product object with updates

#### Activate Product
```http
PATCH /productos/{id}/activar
```

#### Deactivate Product
```http
PATCH /productos/{id}/desactivar
```

#### Update Stock
```http
PATCH /productos/{id}/stock?stock={newStock}
```
**Query Parameters:**
- `stock` (integer) - New stock quantity

#### Delete Product
```http
DELETE /productos/{id}
```

### Category Endpoints

**Base Path:** `/categorias`

#### Get All Categories
```http
GET /categorias
```

#### Get Category by ID
```http
GET /categorias/{id}
```
**Path Parameters:**
- `id` (string) - Category ID

#### Search Categories by Name
```http
GET /categorias/buscar?nombre={searchTerm}
```

#### Create Category
```http
POST /categorias
Content-Type: application/json
```
**Request Body:**
```json
{
  "idCategoria": "electronics",
  "nombre": "Electrónica",
  "descripcion": "Productos electrónicos",
  "imagenUrl": "https://example.com/cat-electronics.jpg"
}
```
**Response:** 201 Created

#### Update Category
```http
PUT /categorias/{id}
Content-Type: application/json
```

#### Delete Category
```http
DELETE /categorias/{id}
```

---

## Carrito Service API

**Base URL:** `http://localhost:8083/api/v1`
**Swagger UI:** `http://localhost:8083/swagger-ui/index.html`
**Database:** PostgreSQL
**External Integration:** Transbank WebPay Plus

### Cart Management Endpoints

**Base Path:** `/cart`

#### Get or Create User Cart
```http
GET /cart/usuario/{usuarioId}
```
**Path Parameters:**
- `usuarioId` (UUID) - User ID

**Response:**
```json
{
  "status": "success",
  "data": {
    "carritoId": "uuid",
    "usuarioId": "uuid",
    "items": [...],
    "totalProductos": 150000,
    "cantidadItems": 3,
    "estado": "ACTIVO",
    "creadoEl": "2025-12-01T10:00:00Z"
  }
}
```

#### Get Cart by ID
```http
GET /cart/{carritoId}
```

#### Get All Carts for User
```http
GET /cart/usuario/{usuarioId}/todos
```

#### Add Item to Cart
```http
POST /cart/{carritoId}/items
Content-Type: application/json
```
**Request Body:**
```json
{
  "servicioId": "product-uuid",
  "cantidad": 2,
  "personalizaciones": {
    "color": "red",
    "size": "L"
  }
}
```
**Response:** 201 Created with cart item

#### Update Item Quantity
```http
PUT /cart/items/{itemId}
Content-Type: application/json
```
**Request Body:**
```json
{
  "cantidad": 5
}
```

#### Remove Item from Cart
```http
DELETE /cart/items/{itemId}
```
**Response:** 204 No Content

#### Process Cart (Finalize)
```http
PUT /cart/{carritoId}/procesar
```
Marks cart as processed

#### Empty Cart
```http
DELETE /cart/{carritoId}/vaciar
```
**Response:** 204 No Content

#### Get Cart Statistics
```http
GET /cart/estadisticas
```
Returns system-wide cart statistics

### Checkout Endpoints

**Base Path:** `/checkout`

#### Initiate Checkout
```http
POST /checkout/initiate
Content-Type: application/json
```
**Request Body:**
```json
{
  "carritoId": "uuid",
  "returnUrl": "https://myapp.com/payment/return"
}
```

**Response:**
```json
{
  "status": "success",
  "data": {
    "numeroOrden": "ORD-20251201-00001",
    "transbankToken": "01ab23cd4ef567890123456789abcdef",
    "transbankUrl": "https://webpay3gint.transbank.cl/webpayserver/initTransaction",
    "pedidoId": "uuid",
    "montoTotal": 150000
  }
}
```

**Notes:**
- Creates a `Pedido` (order) from the cart
- Initiates payment with Transbank
- Returns URL to redirect user for payment
- Marks cart as PROCESADO

#### Confirm Checkout
```http
GET /checkout/confirm?token_ws={transbankToken}
```
**Query Parameters:**
- `token_ws` (required) - Token returned by Transbank

**Response:**
```json
{
  "status": "success",
  "data": {
    "pedidoId": "uuid",
    "numeroPedido": "ORD-20251201-00001",
    "estadoPedido": "PAGO_COMPLETADO",
    "pago": {
      "estadoPago": "APROBADO",
      "authorizationCode": "123456"
    }
  }
}
```

#### Confirm Checkout (POST)
```http
POST /checkout/confirm?token_ws={transbankToken}
```
Alternative POST method for Transbank callbacks

### Order Management Endpoints

**Base Path:** `/orders`

#### Get Order by ID
```http
GET /orders/{orderId}
```

#### Get Order by Order Number
```http
GET /orders/numero/{numeroOrden}
```
**Example:** `/orders/numero/ORD-20251201-00001`

#### Get Orders by User
```http
GET /orders/usuario/{usuarioId}
```
Returns all orders for a user, ordered by creation date descending

#### Get Orders by User and Status
```http
GET /orders/usuario/{usuarioId}/estado/{estado}
```
**Path Parameters:**
- `estado` - Order status:
  - `CREADO` - Order created
  - `PAGO_PENDIENTE` - Payment pending
  - `PAGO_COMPLETADO` - Payment completed
  - `PROCESANDO` - Processing
  - `COMPLETADO` - Completed
  - `CANCELADO` - Cancelled
  - `FALLIDO` - Failed

**Example:**
```http
GET /orders/usuario/123e4567-e89b-12d3-a456-426614174000/estado/PAGO_COMPLETADO
```

#### Update Order Status
```http
PUT /orders/{orderId}/estado?nuevoEstado={estado}
```
**Query Parameters:**
- `nuevoEstado` - New order status (see status list above)

**Note:** Restricted for administrators

#### Cancel Order
```http
PUT /orders/{orderId}/cancel
```
Cancels an order if not yet completed

### Payment Query Endpoints

**Base Path:** `/payments`

#### Get Payment by ID
```http
GET /payments/{pagoId}
```

**Response:**
```json
{
  "status": "success",
  "data": {
    "pagoId": "uuid",
    "pedidoId": "uuid",
    "metodoPago": "WEBPAY_PLUS",
    "estadoPago": "APROBADO",
    "montoTotal": 150000,
    "transbankToken": "token-here",
    "transbankBuyOrder": "ORD-20251201-00001",
    "authorizationCode": "123456",
    "responseCode": 0,
    "creadoEl": "2025-12-01T10:00:00Z",
    "confirmadoEl": "2025-12-01T10:05:00Z"
  }
}
```

#### Get Payment by Order ID
```http
GET /payments/pedido/{pedidoId}
```

#### Get Payment by Transbank Token
```http
GET /payments/token/{token}
```

#### Check Payment Approval
```http
GET /payments/{pagoId}/approved
```
**Response:**
```json
{
  "status": "success",
  "data": true
}
```

#### Payment Service Health Check
```http
GET /payments/health
```
**Response:**
```json
{
  "status": "success",
  "data": "Payment service is running"
}
```

---

## Common Response Formats

All services use a standardized response format:

### Success Response
```json
{
  "status": "success",
  "data": { ... },
  "timestamp": "2025-12-01T10:00:00Z"
}
```

### Error Response
```json
{
  "status": "error",
  "message": "Error description",
  "code": 404,
  "timestamp": "2025-12-01T10:00:00Z"
}
```

### HATEOAS Links (Usuario Service Only)

User responses include hypermedia links:
```json
{
  "_links": {
    "self": {
      "href": "/api/v1/users/123"
    },
    "activate": {
      "href": "/api/v1/users/123/status?active=true"
    },
    "deactivate": {
      "href": "/api/v1/users/123/status?active=false"
    },
    "update": {
      "href": "/api/v1/users/123"
    },
    "delete": {
      "href": "/api/v1/users/123"
    }
  }
}
```

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Invalid request data |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate resource (e.g., email exists) |
| 422 | Unprocessable Entity | Validation error |
| 500 | Internal Server Error | Server error |

### Common Error Scenarios

#### Duplicate Email (Usuario)
```json
{
  "status": "error",
  "message": "Email ya está registrado",
  "code": 409
}
```

#### Product Not Found (Inventario)
```json
{
  "status": "error",
  "message": "Producto no encontrado",
  "code": 404
}
```

#### Cart Empty (Carrito)
```json
{
  "status": "error",
  "message": "El carrito está vacío",
  "code": 400
}
```

#### Payment Failed (Carrito)
```json
{
  "status": "error",
  "message": "Pago rechazado por Transbank",
  "code": 400
}
```

---

## Testing

### Testing with cURL

#### Register a User
```bash
curl -X POST http://localhost:8081/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "personal": {
      "firstName": "Test",
      "lastName": "User",
      "phone": "+56912345678"
    }
  }'
```

#### Login (Authenticate User)
```bash
curl -X POST http://localhost:8081/api/v1/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "test@example.com",
    "password": "password123"
  }'
```

#### Create a Product
```bash
curl -X POST http://localhost:8082/api/v1/productos \
  -H "Content-Type: application/json" \
  -d '{
    "code": "TEST-001",
    "nombre": "Test Product",
    "precio": 10000,
    "stock": 100,
    "categoriaId": "test-category",
    "activo": true
  }'
```

#### Add Item to Cart
```bash
curl -X POST http://localhost:8083/api/v1/cart/{carritoId}/items \
  -H "Content-Type: application/json" \
  -d '{
    "servicioId": "product-uuid",
    "cantidad": 1
  }'
```

#### Initiate Checkout
```bash
curl -X POST http://localhost:8083/api/v1/checkout/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "carritoId": "cart-uuid",
    "returnUrl": "http://localhost:3000/payment/return"
  }'
```

### Testing with Swagger UI

Each service provides interactive API documentation:

- **Usuario:** http://localhost:8081/swagger-ui/index.html
- **Inventario:** http://localhost:8082/swagger-ui/index.html
- **Carrito:** http://localhost:8083/swagger-ui/index.html

### Service Startup Order

To test the complete system:

```bash
# Terminal 1 - Usuario Service
cd usuario
mvn spring-boot:run

# Terminal 2 - Inventario Service
cd inventario
mvn spring-boot:run

# Terminal 3 - Carrito Service
cd carrito
mvn spring-boot:run
```

### Database Configuration

Each service requires a `.env` file in `src/main/resources/`:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=lunari_db
DB_USER=postgres
DB_PASSWORD=your_password
```

For **Carrito** service, also configure Transbank:
```env
TRANSBANK_API_KEY=597055555532
TRANSBANK_COMMERCE_CODE=597055555532
TRANSBANK_ENVIRONMENT=TEST
```

---

## Notes

### Pagination (Usuario Service)

Usuario service uses token-based pagination with DynamoDB:
- Use `limit` to control page size
- Use `paginationToken` from previous response for next page
- Default limit is 10 items

### Product Codes (Inventario)

Product codes should be unique and follow format: `{CATEGORY}-{NUMBER}` (e.g., `ELEC-001`)

### Order Numbers (Carrito)

Order numbers are auto-generated with format: `ORD-YYYYMMDD-XXXXX`
Example: `ORD-20251201-00001`

### Transbank Integration

The Carrito service integrates with Transbank WebPay Plus:
- **TEST environment**: Uses default test credentials
- **PRODUCTION**: Requires real Transbank credentials
- Test cards available in Transbank documentation

### Background Jobs (Carrito)

After successful payment, the system:
1. Reduces product stock (async job)
2. Awards points to user (async job)
3. Updates order status

---

## Support

For issues or questions:
- Repository: https://github.com/DSY1103G5/lunari
- Documentation: See individual service README files
- Swagger UI: Available on each service

---

**Document Version:** 1.0
**Generated:** December 1, 2025
**Services:** Usuario v0.0.1 | Inventario v0.0.1 | Carrito v0.0.1
