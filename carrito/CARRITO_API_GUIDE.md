# Carrito API - Comprehensive Guide

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Getting Started](#getting-started)
4. [API Endpoints](#api-endpoints)
   - [Cart Management](#cart-management)
   - [Checkout & Payment](#checkout--payment)
   - [Order Management](#order-management)
   - [Payment Queries](#payment-queries)
5. [Data Models](#data-models)
6. [Business Logic & Workflows](#business-logic--workflows)
7. [Integration with Other Services](#integration-with-other-services)
8. [Configuration](#configuration)
9. [Error Handling](#error-handling)
10. [Testing](#testing)

---

## Overview

The **Carrito API** (Cart Service) is a Spring Boot microservice responsible for managing shopping carts, checkout processes, orders, and payment integration with Transbank WebPay Plus. It is part of the LUNARi e-commerce platform and operates as an independent service that communicates with other microservices.

### Key Features

- Shopping cart management (create, update, delete items)
- Multi-item cart with customizations support
- Transbank WebPay Plus integration for payments
- Order lifecycle management
- Asynchronous stock reduction and points awarding
- Cart statistics and reporting
- Automatic cart expiration (30 days)

### Technology Stack

- **Framework**: Spring Boot 3.4.5
- **Java Version**: 21
- **Database**: PostgreSQL (production), H2 (testing)
- **ORM**: Spring Data JPA with Hibernate
- **Payment Gateway**: Transbank WebPay Plus SDK 2.0.0
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Build Tool**: Maven
- **Utilities**: Lombok, dotenv-java

### Base URL

- **Production**: `http://localhost:8083/api/v1`
- **Swagger UI**: `http://localhost:8083/swagger-ui`
- **API Docs**: `http://localhost:8083/api-docs`

---

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│          Controllers (REST API)          │
│  - CarritoController                     │
│  - CheckoutController                    │
│  - OrderController                       │
│  - PaymentController                     │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│            Service Layer                 │
│  - CarritoService                        │
│  - CheckoutService                       │
│  - OrderService                          │
│  - PaymentService                        │
│  - TransbankService                      │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Repository Layer                │
│  - CarritoRepository                     │
│  - CarritoItemRepository                 │
│  - PedidoRepository                      │
│  - PagoRepository                        │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         PostgreSQL Database              │
└─────────────────────────────────────────┘
```

### Inter-Service Communication

The Carrito service integrates with:

- **Usuario Service** (Port 8081): Validates user existence
- **Inventario Service** (Port 8082): Validates products and manages stock
- **Transbank API**: Processes payments via WebPay Plus

### Async Processing

- **StockReductionJob**: Background task to reduce inventory stock after payment confirmation
- **PointsAwardJob**: Background task to award loyalty points to users after order completion

---

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- PostgreSQL database
- Running instances of Usuario and Inventario services
- Transbank developer credentials (or use test environment)

### Environment Configuration

Create a `.env` file in `src/main/resources/` with:

```env
# Database
DB_HOST=your-postgres-host
DB_PORT=5432
DB_NAME=lunari_cart_db
DB_USER=your-db-user
DB_PASSWORD=your-db-password

# Transbank (TEST environment defaults provided)
TRANSBANK_API_KEY=597055555532
TRANSBANK_COMMERCE_CODE=597055555532
TRANSBANK_ENVIRONMENT=TEST
```

### Running the Service

```bash
# Navigate to carrito directory
cd carrito/

# Run with Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/lunari-cart-api-0.0.1-SNAPSHOT.jar
```

The service will start on port **8083**.

### Verifying the Service

```bash
# Health check
curl http://localhost:8083/api/v1/payments/health

# Access Swagger UI
open http://localhost:8083/swagger-ui
```

---

## API Endpoints

All endpoints return responses wrapped in a standard `ApiResponse` format:

```json
{
  "success": true,
  "data": { ... },
  "message": null,
  "statusCode": 200
}
```

Error responses:

```json
{
  "success": false,
  "data": null,
  "message": "Error description",
  "statusCode": 400
}
```

---

### Cart Management

#### 1. Get or Create Active Cart for User

**Endpoint**: `GET /api/v1/cart/usuario/{usuarioId}`

**Description**: Retrieves the active cart for a user or creates a new one if none exists.

**Path Parameters**:
- `usuarioId` (UUID): User ID

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "usuarioId": "123e4567-e89b-12d3-a456-426614174000",
    "estado": "ACTIVO",
    "totalEstimado": 45990.00,
    "items": [
      {
        "id": "660e8400-e29b-41d4-a716-446655440001",
        "servicioId": 1,
        "cantidad": 2,
        "precioUnitario": 22995.00,
        "subtotal": 45990.00,
        "personalizaciones": "Color: Azul"
      }
    ],
    "notasCliente": null,
    "fechaExpiracion": "2025-02-02T10:30:00Z",
    "creadoEl": "2025-01-03T10:30:00Z",
    "actualizadoEl": "2025-01-03T11:45:00Z"
  }
}
```

**Status Codes**:
- `200 OK`: Cart retrieved or created successfully
- `400 BAD REQUEST`: Invalid user ID or validation error

---

#### 2. Get Cart by ID

**Endpoint**: `GET /api/v1/cart/{carritoId}`

**Description**: Retrieves a specific cart by its ID.

**Path Parameters**:
- `carritoId` (UUID): Cart ID

**Status Codes**:
- `200 OK`: Cart found
- `404 NOT FOUND`: Cart not found

---

#### 3. Get All Carts for User

**Endpoint**: `GET /api/v1/cart/usuario/{usuarioId}/todos`

**Description**: Retrieves all carts (active and historical) for a user.

**Response**: Array of cart objects

**Status Codes**:
- `200 OK`: List returned successfully
- `500 INTERNAL SERVER ERROR`: Error retrieving carts

---

#### 4. Add Item to Cart

**Endpoint**: `POST /api/v1/cart/{carritoId}/items`

**Description**: Adds a product/service item to the cart.

**Path Parameters**:
- `carritoId` (UUID): Cart ID

**Request Body**:
```json
{
  "servicioId": 1,
  "cantidad": 2,
  "personalizaciones": "Color: Rojo, Talla: M"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "servicioId": 1,
    "cantidad": 2,
    "precioUnitario": 15990.00,
    "subtotal": 31980.00,
    "personalizaciones": "Color: Rojo, Talla: M"
  }
}
```

**Status Codes**:
- `201 CREATED`: Item added successfully
- `400 BAD REQUEST`: Invalid request or service not found

---

#### 5. Update Item Quantity

**Endpoint**: `PUT /api/v1/cart/items/{itemId}`

**Description**: Updates the quantity of a cart item. If quantity is 0, the item is removed.

**Path Parameters**:
- `itemId` (UUID): Cart item ID

**Request Body**:
```json
{
  "cantidad": 3
}
```

**Response**: Updated cart item (or null/204 if removed)

**Status Codes**:
- `200 OK`: Item updated
- `204 NO CONTENT`: Item removed (quantity was 0)
- `400 BAD REQUEST`: Invalid quantity

---

#### 6. Remove Item from Cart

**Endpoint**: `DELETE /api/v1/cart/items/{itemId}`

**Description**: Removes an item from the cart.

**Path Parameters**:
- `itemId` (UUID): Cart item ID

**Status Codes**:
- `204 NO CONTENT`: Item removed successfully
- `404 NOT FOUND`: Item not found

---

#### 7. Empty Cart

**Endpoint**: `DELETE /api/v1/cart/{carritoId}/vaciar`

**Description**: Removes all items from a cart.

**Path Parameters**:
- `carritoId` (UUID): Cart ID

**Status Codes**:
- `204 NO CONTENT`: Cart emptied successfully
- `400 BAD REQUEST`: Error emptying cart

---

#### 8. Get Cart Statistics

**Endpoint**: `GET /api/v1/cart/estadisticas`

**Description**: Returns global cart statistics.

**Response**:
```json
{
  "success": true,
  "data": {
    "totalCarritosActivos": 45,
    "totalCarritosProcesados": 230,
    "totalCarritosAbandonados": 12,
    "valorPromedioCarrito": 78500.00
  }
}
```

**Status Codes**:
- `200 OK`: Statistics retrieved
- `500 INTERNAL SERVER ERROR`: Error calculating statistics

---

### Checkout & Payment

#### 9. Initiate Checkout

**Endpoint**: `POST /api/v1/checkout/initiate`

**Description**: Initiates the checkout process. Creates an order, initiates payment with Transbank, and returns the payment URL.

**Request Body**:
```json
{
  "carritoId": "550e8400-e29b-41d4-a716-446655440000",
  "returnUrl": "https://myapp.com/payment/return",
  "notasCliente": "Entrega urgente"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "numeroOrden": "ORD-20250103-00001",
    "pedidoId": "880e8400-e29b-41d4-a716-446655440003",
    "transbankToken": "01ab02cd03ef04gh05ij06kl07mn08op",
    "transbankUrl": "https://webpay3gint.transbank.cl/webpayserver/initTransaction",
    "monto": 45990.00
  }
}
```

**Flow**:
1. Validates cart exists and has items
2. Creates order (Pedido) with state `CREADO`
3. Initiates payment with Transbank
4. Creates payment record (Pago) with state `PENDIENTE`
5. Updates order state to `PAGO_PENDIENTE`
6. Returns Transbank payment URL

**Status Codes**:
- `200 OK`: Checkout initiated successfully
- `400 BAD REQUEST`: Cart is empty or invalid data
- `404 NOT FOUND`: Cart not found
- `500 INTERNAL SERVER ERROR`: Error processing checkout

---

#### 10. Confirm Checkout

**Endpoint**: `GET /api/v1/checkout/confirm?token_ws={token}`

**Description**: Confirms payment after user returns from Transbank. This is the callback URL.

**Query Parameters**:
- `token_ws` (String): Transbank token

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "880e8400-e29b-41d4-a716-446655440003",
    "numeroPedido": "ORD-20250103-00001",
    "usuarioId": "123e4567-e89b-12d3-a456-426614174000",
    "estadoPedido": "PAGO_COMPLETADO",
    "totalProductos": 45990.00,
    "totalPuntosGanados": 459,
    "items": [...],
    "pago": {
      "id": "990e8400-e29b-41d4-a716-446655440004",
      "metodoPago": "WEBPAY_PLUS",
      "estadoPago": "APROBADO",
      "montoTotal": 45990.00,
      "authorizationCode": "1234567890",
      "responseCode": 0
    },
    "creadoEl": "2025-01-03T12:00:00Z",
    "completadoEl": "2025-01-03T12:05:00Z"
  }
}
```

**Flow**:
1. Retrieves payment by token
2. Confirms payment with Transbank
3. Updates payment state (APROBADO or RECHAZADO)
4. Updates order state (PAGO_COMPLETADO or FALLIDO)
5. Triggers async jobs:
   - Stock reduction in Inventario service
   - Points awarding in Usuario service

**Status Codes**:
- `200 OK`: Payment confirmed successfully
- `400 BAD REQUEST`: Invalid token or payment rejected
- `404 NOT FOUND`: Payment not found
- `500 INTERNAL SERVER ERROR`: Error confirming payment

---

#### 11. Confirm Checkout (POST)

**Endpoint**: `POST /api/v1/checkout/confirm?token_ws={token}`

**Description**: Alternative POST endpoint for checkout confirmation (same functionality as GET).

---

### Order Management

#### 12. Get Order by ID

**Endpoint**: `GET /api/v1/orders/{orderId}`

**Description**: Retrieves an order by its ID.

**Path Parameters**:
- `orderId` (UUID): Order ID

**Status Codes**:
- `200 OK`: Order found
- `404 NOT FOUND`: Order not found

---

#### 13. Get Order by Number

**Endpoint**: `GET /api/v1/orders/numero/{numeroOrden}`

**Description**: Retrieves an order by its order number (e.g., ORD-20250103-00001).

**Path Parameters**:
- `numeroOrden` (String): Order number

**Status Codes**:
- `200 OK`: Order found
- `404 NOT FOUND`: Order not found

---

#### 14. Get Orders by User

**Endpoint**: `GET /api/v1/orders/usuario/{usuarioId}`

**Description**: Retrieves all orders for a specific user, ordered by creation date descending.

**Path Parameters**:
- `usuarioId` (UUID): User ID

**Response**: Array of order objects

**Status Codes**:
- `200 OK`: List returned successfully

---

#### 15. Get Orders by User and State

**Endpoint**: `GET /api/v1/orders/usuario/{usuarioId}/estado/{estado}`

**Description**: Retrieves orders for a user filtered by order state.

**Path Parameters**:
- `usuarioId` (UUID): User ID
- `estado` (String): Order state (CREADO, PAGO_PENDIENTE, PAGO_COMPLETADO, PROCESANDO, COMPLETADO, CANCELADO, FALLIDO)

**Status Codes**:
- `200 OK`: Filtered list returned
- `400 BAD REQUEST`: Invalid state value

---

#### 16. Update Order Status

**Endpoint**: `PUT /api/v1/orders/{orderId}/estado?nuevoEstado={estado}`

**Description**: Updates the state of an order (admin operation).

**Path Parameters**:
- `orderId` (UUID): Order ID

**Query Parameters**:
- `nuevoEstado` (String): New state value

**Status Codes**:
- `200 OK`: State updated successfully
- `400 BAD REQUEST`: Invalid state or transition not allowed
- `404 NOT FOUND`: Order not found

---

#### 17. Cancel Order

**Endpoint**: `PUT /api/v1/orders/{orderId}/cancel`

**Description**: Cancels an order if it hasn't been completed yet.

**Path Parameters**:
- `orderId` (UUID): Order ID

**Status Codes**:
- `200 OK`: Order cancelled successfully
- `400 BAD REQUEST`: Cannot cancel order in current state
- `404 NOT FOUND`: Order not found

---

### Payment Queries

#### 18. Get Payment by ID

**Endpoint**: `GET /api/v1/payments/{pagoId}`

**Description**: Retrieves payment details by payment ID.

**Path Parameters**:
- `pagoId` (UUID): Payment ID

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "990e8400-e29b-41d4-a716-446655440004",
    "pedidoId": "880e8400-e29b-41d4-a716-446655440003",
    "metodoPago": "WEBPAY_PLUS",
    "estadoPago": "APROBADO",
    "montoTotal": 45990.00,
    "transbankToken": "01ab02cd03ef04gh05ij06kl07mn08op",
    "transbankBuyOrder": "ORD-20250103-00001",
    "authorizationCode": "1234567890",
    "responseCode": 0,
    "creadoEl": "2025-01-03T12:00:00Z",
    "confirmadoEl": "2025-01-03T12:05:00Z"
  }
}
```

**Status Codes**:
- `200 OK`: Payment found
- `404 NOT FOUND`: Payment not found

---

#### 19. Get Payment by Order ID

**Endpoint**: `GET /api/v1/payments/pedido/{pedidoId}`

**Description**: Retrieves the payment associated with a specific order.

**Path Parameters**:
- `pedidoId` (UUID): Order ID

**Status Codes**:
- `200 OK`: Payment found
- `404 NOT FOUND`: No payment found for order

---

#### 20. Get Payment by Token

**Endpoint**: `GET /api/v1/payments/token/{token}`

**Description**: Retrieves payment by Transbank token.

**Path Parameters**:
- `token` (String): Transbank token

**Status Codes**:
- `200 OK`: Payment found
- `404 NOT FOUND`: Payment not found

---

#### 21. Check if Payment is Approved

**Endpoint**: `GET /api/v1/payments/{pagoId}/approved`

**Description**: Returns boolean indicating if payment was approved.

**Response**:
```json
{
  "success": true,
  "data": true
}
```

**Status Codes**:
- `200 OK`: Status checked successfully
- `404 NOT FOUND`: Payment not found

---

#### 22. Payment Service Health Check

**Endpoint**: `GET /api/v1/payments/health`

**Description**: Health check endpoint.

**Response**:
```json
{
  "success": true,
  "data": "Payment service is running"
}
```

---

## Data Models

### Carrito (Cart)

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key |
| usuarioId | UUID | User ID (foreign key to Usuario service) |
| estado | EstadoCarrito | Cart state (ACTIVO, PROCESADO, ABANDONADO, EXPIRADO) |
| totalEstimado | BigDecimal | Estimated total amount |
| notasCliente | String | Customer notes |
| fechaExpiracion | OffsetDateTime | Expiration date (default: 30 days from creation) |
| creadoEl | OffsetDateTime | Creation timestamp |
| actualizadoEl | OffsetDateTime | Last update timestamp |
| numeroOrden | String | Order number (if converted to order) |
| items | List&lt;CarritoItem&gt; | Cart items |

### CarritoItem (Cart Item)

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key |
| carrito | Carrito | Parent cart (foreign key) |
| servicioId | Integer | Product/service ID (from Inventario service) |
| cantidad | Integer | Quantity |
| precioUnitario | BigDecimal | Unit price (snapshot at time of addition) |
| subtotal | BigDecimal | Subtotal (cantidad * precioUnitario) |
| personalizaciones | String | Customizations (JSON or text) |
| creadoEl | OffsetDateTime | Creation timestamp |

### Pedido (Order)

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key |
| numeroPedido | String | Unique order number (ORD-YYYYMMDD-XXXXX) |
| carritoId | UUID | Source cart ID |
| usuarioId | UUID | User ID |
| estadoPedido | EstadoPedido | Order state |
| totalProductos | BigDecimal | Total amount |
| totalPuntosGanados | Integer | Loyalty points earned (1 point per 100 CLP) |
| notasCliente | String | Customer notes |
| creadoEl | OffsetDateTime | Creation timestamp |
| actualizadoEl | OffsetDateTime | Last update timestamp |
| completadoEl | OffsetDateTime | Completion timestamp |
| items | List&lt;PedidoItem&gt; | Order items |
| pago | Pago | Associated payment |

### PedidoItem (Order Item)

Similar to CarritoItem but associated with an Order instead of a Cart.

### Pago (Payment)

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key |
| pedido | Pedido | Associated order (foreign key) |
| metodoPago | MetodoPago | Payment method (WEBPAY_PLUS) |
| estadoPago | EstadoPago | Payment state |
| montoTotal | BigDecimal | Total amount |
| transbankToken | String | Transbank token |
| transbankBuyOrder | String | Transbank buy order |
| transbankSessionId | String | Transbank session ID |
| paymentUrl | String | Transbank payment URL |
| authorizationCode | String | Authorization code (on approval) |
| responseCode | Integer | Response code from Transbank |
| creadoEl | OffsetDateTime | Creation timestamp |
| confirmadoEl | OffsetDateTime | Confirmation timestamp |

### Enums

#### EstadoCarrito (Cart State)

- **ACTIVO**: Active cart (default)
- **PROCESADO**: Cart converted to order
- **ABANDONADO**: Cart abandoned by user
- **EXPIRADO**: Cart expired (past expiration date)

#### EstadoPedido (Order State)

- **CREADO**: Order created, awaiting payment initiation
- **PAGO_PENDIENTE**: Payment initiated in Transbank, awaiting user confirmation
- **PAGO_COMPLETADO**: Payment confirmed successfully
- **PROCESANDO**: Processing order (stock reduction, points awarding)
- **COMPLETADO**: Order completed successfully
- **CANCELADO**: Order cancelled
- **FALLIDO**: Order failed (payment rejected or processing error)

**Typical Flow**: CREADO → PAGO_PENDIENTE → PAGO_COMPLETADO → PROCESANDO → COMPLETADO

#### EstadoPago (Payment State)

- **PENDIENTE**: Payment initiated, awaiting confirmation
- **APROBADO**: Payment approved by Transbank
- **RECHAZADO**: Payment rejected (insufficient funds, etc.)
- **ANULADO**: Payment refunded/reversed
- **EXPIRADO**: Payment expired (timeout)

**Typical Flow**: PENDIENTE → APROBADO

#### MetodoPago (Payment Method)

- **WEBPAY_PLUS**: Transbank WebPay Plus (currently the only supported method)

---

## Business Logic & Workflows

### Cart Management Flow

1. **User accesses cart**:
   - System checks for active cart
   - If none exists, creates a new cart with `ACTIVO` state
   - Returns cart with items

2. **User adds items**:
   - System validates product exists (via Inventario service)
   - Fetches current price from Inventario
   - Creates CarritoItem with quantity and price snapshot
   - Updates cart's `totalEstimado`

3. **User updates quantity**:
   - If quantity > 0: Updates item quantity and subtotal
   - If quantity = 0: Removes item from cart
   - Recalculates cart total

4. **Cart expiration**:
   - Carts have 30-day expiration by default
   - Expired carts can be marked as `EXPIRADO` by scheduled job (not shown in provided code)

### Checkout & Payment Flow

1. **Initiate Checkout** (`POST /checkout/initiate`):
   ```
   User → Frontend → Carrito API
   ├─ Validate cart exists and has items
   ├─ Validate user exists (via Usuario service)
   ├─ Create Pedido with state CREADO
   ├─ Copy cart items to PedidoItems
   ├─ Calculate points to earn (1 point per 100 CLP)
   ├─ Create Pago record with PENDIENTE state
   ├─ Initiate payment with Transbank
   │  ├─ Generate transbankBuyOrder (order number)
   │  ├─ Generate transbankSessionId
   │  └─ Call Transbank WebPay Plus API
   ├─ Store Transbank token and URL in Pago
   ├─ Update Pedido state to PAGO_PENDIENTE
   ├─ Update Carrito state to PROCESADO
   └─ Return CheckoutInitiateResponse with paymentUrl
   ```

2. **User completes payment**:
   ```
   User → Transbank WebPay → Enters payment details → Confirms
   ```

3. **Confirm Checkout** (`GET /checkout/confirm?token_ws=...`):
   ```
   Transbank → Redirects to returnUrl with token_ws
   Frontend → Carrito API (/checkout/confirm)
   ├─ Retrieve Pago by token
   ├─ Call Transbank to confirm transaction
   ├─ If approved (responseCode = 0):
   │  ├─ Update Pago: estado = APROBADO, save authCode & responseCode
   │  ├─ Update Pedido: estado = PAGO_COMPLETADO
   │  ├─ Launch async job: StockReductionJob
   │  │  └─ Call Inventario service to reduce stock for each item
   │  └─ Launch async job: PointsAwardJob
   │     └─ Call Usuario service to add points to user account
   ├─ Else (payment rejected):
   │  ├─ Update Pago: estado = RECHAZADO
   │  └─ Update Pedido: estado = FALLIDO
   └─ Return Pedido with complete details
   ```

4. **Post-payment processing** (Async):
   ```
   StockReductionJob (async):
   └─ For each PedidoItem:
      └─ Call Inventario API to reduce stock
         └─ If success: Continue
         └─ If failure: Log error (order still completes)

   PointsAwardJob (async):
   └─ Call Usuario API to add points
      └─ If success: Update Pedido.totalPuntosGanados
      └─ If failure: Log error (order still completes)
   ```

### Order State Transitions

```
CREADO (Order created)
   ↓
PAGO_PENDIENTE (Payment initiated, awaiting user action)
   ↓ (payment approved)
PAGO_COMPLETADO (Payment confirmed)
   ↓
PROCESANDO (Post-payment jobs running)
   ↓
COMPLETADO (Order fully completed)

Alternative flows:
CREADO → CANCELADO (user cancels before payment)
PAGO_PENDIENTE → FALLIDO (payment rejected)
PAGO_COMPLETADO → FALLIDO (error in post-processing, rare)
```

### Payment State Transitions

```
PENDIENTE (Payment created and sent to Transbank)
   ↓ (user completes payment)
APROBADO (Payment confirmed by Transbank)

Alternative flows:
PENDIENTE → RECHAZADO (insufficient funds, card declined)
PENDIENTE → EXPIRADO (user didn't complete payment)
APROBADO → ANULADO (refund/reversal, manual operation)
```

---

## Integration with Other Services

### Usuario Service Integration

**Base URL**: `http://localhost:8081/api/v1`

**Client**: `UsuarioServiceClient` (src/main/java/.../service/client/UsuarioServiceClient.java)

**Operations**:

1. **Validate User Exists**:
   - **Endpoint**: `GET /users/{id}`
   - **Used in**: Cart creation, checkout
   - **Purpose**: Ensure user exists before creating cart or order

2. **Award Loyalty Points**:
   - **Endpoint**: `POST /points/add`
   - **Used in**: PointsAwardJob (async after payment)
   - **Request**:
     ```json
     {
       "userId": "uuid",
       "points": 459,
       "orderId": "uuid",
       "reason": "Compra completada"
     }
     ```

### Inventario Service Integration

**Base URL**: `http://localhost:8082/api/v1`

**Client**: `InventarioServiceClient` (src/main/java/.../service/client/InventarioServiceClient.java)

**Operations**:

1. **Get Product Details**:
   - **Endpoint**: `GET /productos/{id}`
   - **Used in**: Adding items to cart
   - **Purpose**: Fetch current price and validate product exists

2. **Reduce Stock**:
   - **Endpoint**: `POST /productos/reduce-stock`
   - **Used in**: StockReductionJob (async after payment)
   - **Request**:
     ```json
     {
       "productoId": 1,
       "cantidad": 2,
       "pedidoId": "uuid"
     }
     ```

### Transbank Integration

**SDK**: Transbank WebPay Plus SDK 2.0.0

**Service**: `TransbankService` (src/main/java/.../service/TransbankService.java)

**Configuration**:
- **Environment**: TEST (development) or PRODUCTION
- **Commerce Code**: Merchant identifier
- **API Key**: Authentication key

**Operations**:

1. **Create Transaction**:
   - Initiates payment transaction
   - Returns token and payment URL
   - Parameters: buyOrder, sessionId, amount, returnUrl

2. **Commit Transaction**:
   - Confirms payment after user completes it
   - Validates transaction with Transbank
   - Returns payment status, authorization code, response code

**Response Codes**:
- `0`: Approved
- `-1`, `-2`, ... : Various rejection reasons

---

## Configuration

### Application Properties

**Location**: `src/main/resources/application.properties`

**Key configurations**:

```properties
# Server
server.port=8083

# Database
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=require
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Microservices URLs
lunari.services.usuario.url=http://localhost:8081
lunari.services.inventario.url=http://localhost:8082

# Transbank
transbank.api.key=${TRANSBANK_API_KEY:597055555532}
transbank.commerce.code=${TRANSBANK_COMMERCE_CODE:597055555532}
transbank.environment=${TRANSBANK_ENVIRONMENT:TEST}

# Async Execution
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10
spring.task.execution.pool.queue-capacity=100
```

### Environment Profiles

- **default**: Uses main `application.properties`
- **local**: `application-local.properties`
- **dev**: `application-dev.properties`
- **test**: `application-test.properties` (uses H2 database)
- **prod**: `application-prod.properties`

Activate profile: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

---

## Error Handling

### Custom Exceptions

- **CarritoNotFoundException**: Cart not found (404)
- **EmptyCartException**: Cannot process empty cart (400)
- **InsufficientStockException**: Not enough inventory (400)
- **OrderNotFoundException**: Order not found (404)
- **InvalidOrderStateException**: Invalid state transition (400)
- **PaymentNotFoundException**: Payment not found (404)
- **PaymentFailedException**: Payment processing failed (500)

### Standard Error Response

```json
{
  "success": false,
  "data": null,
  "message": "Cart not found",
  "statusCode": 404
}
```

### HTTP Status Codes

- `200 OK`: Successful operation
- `201 CREATED`: Resource created successfully
- `204 NO CONTENT`: Successful deletion or empty response
- `400 BAD REQUEST`: Invalid input or business rule violation
- `404 NOT FOUND`: Resource not found
- `500 INTERNAL SERVER ERROR`: Unexpected server error

---

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CarritoServiceTest

# Run with coverage
mvn clean test jacoco:report
```

### Test Database

Tests automatically use H2 in-memory database configured via `application-test.properties`.

### Test Strategy

1. **Unit Tests**: Service layer with mocked repositories
2. **Integration Tests**: Repository layer with H2 database
3. **Controller Tests**: REST endpoints with MockMvc

### Example Test Structure

```
src/test/java/cl/duoc/lunari/api/cart/
├── service/
│   ├── CarritoServiceTest.java
│   ├── CheckoutServiceTest.java
│   └── OrderServiceTest.java
├── repository/
│   ├── CarritoRepositoryTest.java
│   └── PedidoRepositoryTest.java
└── controller/
    ├── CarritoControllerTest.java
    └── CheckoutControllerTest.java
```

---

## Common Use Cases

### Use Case 1: Complete Purchase Flow

```bash
# 1. Get or create cart for user
GET /api/v1/cart/usuario/{userId}

# 2. Add items to cart
POST /api/v1/cart/{cartId}/items
{
  "servicioId": 1,
  "cantidad": 2,
  "personalizaciones": "Color: Azul"
}

# 3. Initiate checkout
POST /api/v1/checkout/initiate
{
  "carritoId": "{cartId}",
  "returnUrl": "https://myapp.com/payment/return",
  "notasCliente": "Entrega rápida"
}
# Response includes transbankUrl - redirect user here

# 4. User pays on Transbank page, returns to returnUrl
# Transbank redirects to: https://myapp.com/payment/return?token_ws=ABC123

# 5. Confirm payment
GET /api/v1/checkout/confirm?token_ws=ABC123
# Returns completed order

# 6. View order details
GET /api/v1/orders/{orderId}
```

### Use Case 2: View Order History

```bash
# Get all orders for user
GET /api/v1/orders/usuario/{userId}

# Get only completed orders
GET /api/v1/orders/usuario/{userId}/estado/COMPLETADO

# Get specific order by number
GET /api/v1/orders/numero/ORD-20250103-00001
```

### Use Case 3: Check Payment Status

```bash
# Get payment by order ID
GET /api/v1/payments/pedido/{orderId}

# Check if payment is approved
GET /api/v1/payments/{paymentId}/approved
```

---

## Best Practices

1. **Always validate cart exists and has items before checkout**
2. **Handle async job failures gracefully** (stock reduction and points awarding are not critical to order completion)
3. **Store returnUrl from your frontend** to redirect users after Transbank payment
4. **Use order numbers (numeroPedido) for user-facing displays** instead of UUIDs
5. **Implement idempotency for payment confirmation** to handle duplicate callbacks from Transbank
6. **Monitor cart abandonment rates** using cart statistics endpoint
7. **Set appropriate cart expiration periods** based on your business needs
8. **Log all payment transactions** for reconciliation and debugging

---

## Troubleshooting

### Issue: Cart not found when adding items

**Solution**: Ensure the cart exists and is in `ACTIVO` state.

### Issue: Checkout fails with empty cart error

**Solution**: Verify cart has at least one item before initiating checkout.

### Issue: Payment confirmation fails

**Solution**: Check Transbank credentials and environment configuration. Ensure token_ws parameter is correctly passed from Transbank callback.

### Issue: Stock reduction or points awarding doesn't happen

**Solution**: These are async jobs. Check:
- Usuario and Inventario services are running
- Service URLs are correctly configured
- Check application logs for job execution errors

### Issue: "User not found" error during checkout

**Solution**: Ensure Usuario service is running and the user ID exists in the Usuario database.

---

## Additional Resources

- **Swagger UI**: http://localhost:8083/swagger-ui
- **API Docs (JSON)**: http://localhost:8083/api-docs
- **Transbank Developer Portal**: https://www.transbankdevelopers.cl/
- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa

---

## Support & Contribution

For issues, bugs, or feature requests, please contact the development team or create an issue in the project repository.

---

**Last Updated**: January 3, 2025
**Version**: 1.0.0
**Maintainer**: LUNARi Development Team
