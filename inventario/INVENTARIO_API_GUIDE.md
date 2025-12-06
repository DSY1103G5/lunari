# Inventario API - Comprehensive Guide

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Getting Started](#getting-started)
4. [API Endpoints](#api-endpoints)
   - [Product Management](#product-management)
   - [Category Management](#category-management)
5. [Data Models](#data-models)
6. [Security & API Keys](#security--api-keys)
7. [Business Logic & Workflows](#business-logic--workflows)
8. [Configuration](#configuration)
9. [Error Handling](#error-handling)
10. [Testing](#testing)

---

## Overview

The **Inventario API** (Inventory Service) is a Spring Boot microservice responsible for managing the gaming products catalog, categories, and stock management. It is part of the LUNARi e-commerce platform and operates as an independent service using PostgreSQL for data persistence.

### Key Features

- Product catalog management (gaming products)
- Category-based product organization (10 predefined gaming categories)
- Stock management with validation
- Product search and filtering (by name, brand, category, price, rating, tags)
- JSONB support for flexible product specifications and tags
- Two-tier API key authentication (ADMIN and SERVICE levels)
- Service-to-service stock reduction for cart checkout
- Image URL management
- Product activation/deactivation

### Technology Stack

- **Framework**: Spring Boot 3.4.5
- **Java Version**: 21
- **Database**: PostgreSQL (production), H2 (testing)
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Custom API Key authentication (two-tier)
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Build Tool**: Maven
- **Utilities**: Lombok, dotenv-java, Hypersistence Utils (JSONB)

### Base URL

- **Production**: `http://localhost:8082/api/v1`
- **Swagger UI**: `http://localhost:8082/swagger-ui`
- **API Docs**: `http://localhost:8082/api-docs`

---

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│          Controllers (REST API)          │
│  - ProductoController                    │
│  - CategoriaController                   │
│  - HomeController                        │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│        Security Filter Layer             │
│  - ApiKeyFilter                          │
│     - Validates X-API-Key header         │
│     - Enforces ADMIN/SERVICE access      │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│            Service Layer                 │
│  - ProductoService                       │
│  - CategoriaService                      │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Repository Layer                │
│  - ProductoRepository                    │
│  - CategoriaRepository                   │
│     - JPA derived queries                │
│     - Native JSONB queries               │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│      PostgreSQL Database (NeonDB)        │
│  Tables: producto, categoria             │
└─────────────────────────────────────────┘
```

### Inter-Service Communication

The Inventario service is called by:

- **Carrito Service** (Port 8083): Validates products and reduces stock after checkout

---

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- PostgreSQL database (NeonDB or local instance)
- API keys for authentication (optional for development)

### Environment Configuration

Create a `.env` file in `src/main/resources/` with:

```env
# Database Configuration
DB_HOST=your-neon-endpoint.us-east-1.aws.neon.tech
DB_PORT=5432
DB_NAME=lunari_inventory
DB_USER=your-db-user
DB_PASSWORD=your-db-password

# API Security (optional - can be disabled)
API_SECURITY_ENABLED=true
ADMIN_API_KEY=your-admin-key-here
SERVICE_API_KEY=your-service-key-here
```

### Generate API Keys

```bash
# Generate ADMIN API key
openssl rand -hex 32

# Generate SERVICE API key
openssl rand -hex 32
```

### Database Setup

Execute the SQL schema creation script:

```sql
-- Create categoria table
CREATE TABLE categoria (
    id_categoria VARCHAR(10) PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    creado_el TIMESTAMP WITH TIME ZONE,
    actualizado_el TIMESTAMP WITH TIME ZONE
);

-- Create producto table
CREATE TABLE producto (
    id_producto SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    categoria_id VARCHAR(10) NOT NULL REFERENCES categoria(id_categoria),
    precio_clp INTEGER NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    marca VARCHAR(100),
    rating NUMERIC(2,1),
    specs JSONB,
    descripcion TEXT,
    tags JSONB,
    imagen VARCHAR(500),
    is_activo BOOLEAN DEFAULT true,
    creado_el TIMESTAMP WITH TIME ZONE,
    actualizado_el TIMESTAMP WITH TIME ZONE
);
```

### Running the Service

```bash
# Navigate to inventario directory
cd inventario/

# Run with Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/lunari-inventory-app.jar
```

The service will start on port **8082**.

### Verifying the Service

```bash
# Access Swagger UI
open http://localhost:8082/swagger-ui

# Test public endpoint (no API key required)
curl http://localhost:8082/api/v1/productos/activos
```

---

## API Endpoints

All endpoints return responses wrapped in a standard `ApiResponse` format:

```json
{
  "success": true,
  "response": { ... },
  "message": "Success",
  "statusCode": 200
}
```

Error responses:

```json
{
  "success": false,
  "response": null,
  "message": "Error description",
  "statusCode": 400
}
```

---

## Product Management

### Public Endpoints (No Authentication Required)

#### 1. Get All Products

**Endpoint**: `GET /api/v1/productos`

**Description**: Retrieves all products from the catalog.

**Response**:
```json
{
  "success": true,
  "response": [
    {
      "idProducto": 1,
      "code": "JM001",
      "nombre": "Catan - Juego de Mesa",
      "categoria": {
        "idCategoria": "JM",
        "nombreCategoria": "Juegos de Mesa"
      },
      "precioCLP": 45990,
      "stock": 25,
      "marca": "Devir",
      "rating": 4.8,
      "specs": ["3-4 jugadores", "60-120 min", "Edad: 10+"],
      "descripcion": "Juego de estrategia de construcción y comercio",
      "tags": ["estrategia", "familiar", "multijugador"],
      "imagen": "https://example.com/catan.jpg",
      "isActivo": true,
      "creadoEl": "2025-01-01T10:00:00Z",
      "actualizadoEl": "2025-01-03T15:30:00Z"
    }
  ],
  "message": "Success",
  "statusCode": 200
}
```

**Status Codes**:
- `200 OK`: Products retrieved successfully

---

#### 2. Get Active Products Only

**Endpoint**: `GET /api/v1/productos/activos`

**Description**: Retrieves only active products (isActivo = true).

**Response**: Same format as Get All Products (filtered for active only)

**Status Codes**:
- `200 OK`: Active products retrieved successfully

---

#### 3. Get Product by ID

**Endpoint**: `GET /api/v1/productos/{id}`

**Description**: Retrieves a specific product by its ID.

**Path Parameters**:
- `id` (Integer): Product ID

**Response**:
```json
{
  "success": true,
  "response": {
    "idProducto": 1,
    "code": "JM001",
    "nombre": "Catan - Juego de Mesa",
    "categoria": {
      "idCategoria": "JM",
      "nombreCategoria": "Juegos de Mesa"
    },
    "precioCLP": 45990,
    "stock": 25,
    "marca": "Devir",
    "rating": 4.8,
    "specs": ["3-4 jugadores", "60-120 min", "Edad: 10+"],
    "descripcion": "Juego de estrategia de construcción y comercio",
    "tags": ["estrategia", "familiar", "multijugador"],
    "imagen": "https://example.com/catan.jpg",
    "isActivo": true
  },
  "message": "Success",
  "statusCode": 200
}
```

**Status Codes**:
- `200 OK`: Product found
- `404 NOT FOUND`: Product not found

---

#### 4. Get Product by Code

**Endpoint**: `GET /api/v1/productos/code/{code}`

**Description**: Retrieves a product by its unique code.

**Path Parameters**:
- `code` (String): Product code (e.g., "JM001")

**Status Codes**:
- `200 OK`: Product found
- `404 NOT FOUND`: Product not found

---

#### 5. Search Products by Name

**Endpoint**: `GET /api/v1/productos/buscar?nombre={searchTerm}`

**Description**: Searches products by name (case-insensitive partial match).

**Query Parameters**:
- `nombre` (String): Search term

**Example**:
```bash
GET /api/v1/productos/buscar?nombre=catan
```

**Status Codes**:
- `200 OK`: Results returned (empty array if no matches)

---

#### 6. Get Products by Category

**Endpoint**: `GET /api/v1/productos/categoria/{categoriaId}`

**Description**: Retrieves all products in a specific category.

**Path Parameters**:
- `categoriaId` (String): Category ID (e.g., "JM", "AC", "CG")

**Example**:
```bash
GET /api/v1/productos/categoria/JM
```

**Status Codes**:
- `200 OK`: Products returned

---

#### 7. Get Products by Brand

**Endpoint**: `GET /api/v1/productos/marca/{marca}`

**Description**: Retrieves all products from a specific brand.

**Path Parameters**:
- `marca` (String): Brand name

**Example**:
```bash
GET /api/v1/productos/marca/Razer
```

**Status Codes**:
- `200 OK`: Products returned

---

#### 8. Get Products in Stock

**Endpoint**: `GET /api/v1/productos/en-stock`

**Description**: Retrieves products with stock > 0.

**Status Codes**:
- `200 OK`: In-stock products returned

---

#### 9. Get Products by Price Range

**Endpoint**: `GET /api/v1/productos/precio?min={minPrice}&max={maxPrice}`

**Description**: Retrieves products within a price range (in CLP).

**Query Parameters**:
- `min` (Integer): Minimum price (CLP)
- `max` (Integer): Maximum price (CLP)

**Example**:
```bash
GET /api/v1/productos/precio?min=10000&max=50000
```

**Status Codes**:
- `200 OK`: Products in price range returned

---

#### 10. Get Products by Minimum Rating

**Endpoint**: `GET /api/v1/productos/rating?min={minRating}`

**Description**: Retrieves products with rating >= minimum.

**Query Parameters**:
- `min` (BigDecimal): Minimum rating (e.g., 4.5)

**Example**:
```bash
GET /api/v1/productos/rating?min=4.5
```

**Status Codes**:
- `200 OK`: Products with minimum rating returned

---

#### 11. Get Products by Tag

**Endpoint**: `GET /api/v1/productos/tag/{tag}`

**Description**: Retrieves products that have a specific tag (JSONB query).

**Path Parameters**:
- `tag` (String): Tag name

**Example**:
```bash
GET /api/v1/productos/tag/estrategia
```

**Implementation**: Uses PostgreSQL JSONB containment operator (`@>`)

**Status Codes**:
- `200 OK`: Products with tag returned

---

### Protected Endpoints - SERVICE API Key Required

**Header**: `X-API-Key: {your-service-api-key}`

#### 12. Reduce Product Stock

**Endpoint**: `PATCH /api/v1/productos/{id}/reducir-stock?cantidad={quantity}`

**Description**: Reduces product stock by specified quantity. Used by Carrito service during checkout.

**Path Parameters**:
- `id` (Integer): Product ID

**Query Parameters**:
- `cantidad` (Integer): Quantity to reduce

**Response**:
```json
{
  "success": true,
  "response": {
    "idProducto": 1,
    "code": "JM001",
    "nombre": "Catan - Juego de Mesa",
    "stock": 20,
    "isActivo": true
  },
  "message": "Success",
  "statusCode": 200
}
```

**Validation**:
- Product must exist
- Quantity must be > 0
- Current stock must be >= requested quantity

**Status Codes**:
- `200 OK`: Stock reduced successfully
- `400 BAD REQUEST`: Insufficient stock or invalid quantity
- `401 UNAUTHORIZED`: Missing or invalid API key
- `404 NOT FOUND`: Product not found

---

### Protected Endpoints - ADMIN API Key Required

**Header**: `X-API-Key: {your-admin-api-key}`

#### 13. Create Product

**Endpoint**: `POST /api/v1/productos`

**Description**: Creates a new product in the catalog.

**Request Body**:
```json
{
  "code": "JM002",
  "nombre": "Ticket to Ride",
  "categoria": {
    "idCategoria": "JM"
  },
  "precioCLP": 38990,
  "stock": 15,
  "marca": "Days of Wonder",
  "rating": 4.6,
  "specs": ["2-5 jugadores", "30-60 min", "Edad: 8+"],
  "descripcion": "Juego de construcción de rutas ferroviarias",
  "tags": ["estrategia", "familiar"],
  "imagen": "https://example.com/ticket-to-ride.jpg"
}
```

**Required Fields**:
- `code` (String): Unique product code
- `nombre` (String): Product name
- `categoria` (Categoria): Category object with idCategoria
- `precioCLP` (Integer): Price in Chilean Pesos

**Optional Fields**:
- `stock` (Integer): Initial stock (default: 0)
- `marca` (String): Brand name
- `rating` (BigDecimal): Rating (0.0 - 5.0)
- `specs` (List&lt;String&gt;): Specifications array
- `descripcion` (String): Description
- `tags` (List&lt;String&gt;): Tags array
- `imagen` (String): Image URL

**Response**: Created product object

**Validation**:
- Category must exist
- Product code must be unique

**Status Codes**:
- `201 CREATED`: Product created successfully
- `400 BAD REQUEST`: Validation error or duplicate code
- `401 UNAUTHORIZED`: Missing or invalid API key

---

#### 14. Update Product

**Endpoint**: `PUT /api/v1/productos/{id}`

**Description**: Updates an existing product.

**Path Parameters**:
- `id` (Integer): Product ID

**Request Body**: Same format as Create Product (all fields)

**Response**: Updated product object

**Status Codes**:
- `200 OK`: Product updated successfully
- `400 BAD REQUEST`: Validation error
- `401 UNAUTHORIZED`: Missing or invalid API key
- `404 NOT FOUND`: Product not found

---

#### 15. Activate Product

**Endpoint**: `PATCH /api/v1/productos/{id}/activar`

**Description**: Activates a product (sets isActivo = true).

**Path Parameters**:
- `id` (Integer): Product ID

**Response**: Updated product object

**Status Codes**:
- `200 OK`: Product activated
- `401 UNAUTHORIZED`: Missing or invalid API key
- `404 NOT FOUND`: Product not found

---

#### 16. Deactivate Product

**Endpoint**: `PATCH /api/v1/productos/{id}/desactivar`

**Description**: Deactivates a product (sets isActivo = false).

**Path Parameters**:
- `id` (Integer): Product ID

**Response**: Updated product object

**Status Codes**:
- `200 OK`: Product deactivated
- `401 UNAUTHORIZED`: Missing or invalid API key
- `404 NOT FOUND`: Product not found

---

#### 17. Set Product Stock

**Endpoint**: `PATCH /api/v1/productos/{id}/stock?stock={newStock}`

**Description**: Sets product stock to an exact value.

**Path Parameters**:
- `id` (Integer): Product ID

**Query Parameters**:
- `stock` (Integer): New stock value

**Example**:
```bash
PATCH /api/v1/productos/1/stock?stock=100
```

**Response**: Updated product object

**Status Codes**:
- `200 OK`: Stock updated
- `401 UNAUTHORIZED`: Missing or invalid API key
- `404 NOT FOUND`: Product not found

---

#### 18. Delete Product

**Endpoint**: `DELETE /api/v1/productos/{id}`

**Description**: Permanently deletes a product from the catalog.

**Path Parameters**:
- `id` (Integer): Product ID

**Response**: No content

**Status Codes**:
- `204 NO CONTENT`: Product deleted successfully
- `401 UNAUTHORIZED`: Missing or invalid API key
- `404 NOT FOUND`: Product not found

---

## Category Management

### Public Endpoints (No Authentication Required)

#### 19. Get All Categories

**Endpoint**: `GET /api/v1/categorias`

**Description**: Retrieves all product categories.

**Response**:
```json
{
  "success": true,
  "response": [
    {
      "idCategoria": "JM",
      "nombreCategoria": "Juegos de Mesa",
      "descripcion": "Juegos de mesa tradicionales y modernos",
      "creadoEl": "2025-01-01T10:00:00Z",
      "actualizadoEl": "2025-01-01T10:00:00Z"
    },
    {
      "idCategoria": "AC",
      "nombreCategoria": "Accesorios",
      "descripcion": "Accesorios gaming y periféricos",
      "creadoEl": "2025-01-01T10:00:00Z",
      "actualizadoEl": "2025-01-01T10:00:00Z"
    }
  ],
  "message": "Success",
  "statusCode": 200
}
```

**Predefined Categories**:
1. **JM** - Juegos de Mesa (Board Games)
2. **AC** - Accesorios (Accessories)
3. **CO** - Consolas (Consoles)
4. **CG** - Cartas Gaming (Gaming Cards)
5. **SG** - Suscripciones Gaming (Gaming Subscriptions)
6. **MS** - Merchandising (Merchandise)
7. **MP** - Merchandise Personalizados (Personalized Merchandise)
8. **PP** - Pre-Pedidos (Pre-orders)
9. **PG** - Promociones Gaming (Gaming Promotions)
10. **ST** - Suscripciones / Tiendas Aliadas (Partner Subscriptions)

**Status Codes**:
- `200 OK`: Categories retrieved successfully

---

#### 20. Get Category by ID

**Endpoint**: `GET /api/v1/categorias/{id}`

**Description**: Retrieves a specific category by its ID.

**Path Parameters**:
- `id` (String): Category ID (e.g., "JM")

**Status Codes**:
- `200 OK`: Category found
- `404 NOT FOUND`: Category not found

---

#### 21. Search Categories by Name

**Endpoint**: `GET /api/v1/categorias/buscar?nombre={searchTerm}`

**Description**: Searches categories by name (case-insensitive partial match).

**Query Parameters**:
- `nombre` (String): Search term

**Example**:
```bash
GET /api/v1/categorias/buscar?nombre=juegos
```

**Status Codes**:
- `200 OK`: Results returned

---

### Protected Endpoints - ADMIN API Key Required

**Header**: `X-API-Key: {your-admin-api-key}`

#### 22. Create Category

**Endpoint**: `POST /api/v1/categorias`

**Description**: Creates a new product category.

**Request Body**:
```json
{
  "idCategoria": "VR",
  "nombreCategoria": "Realidad Virtual",
  "descripcion": "Productos de realidad virtual y VR gaming"
}
```

**Required Fields**:
- `idCategoria` (String): Unique category code (max 10 chars)
- `nombreCategoria` (String): Category name (must be unique)

**Optional Fields**:
- `descripcion` (String): Category description

**Response**: Created category object

**Status Codes**:
- `201 CREATED`: Category created successfully
- `400 BAD REQUEST`: Duplicate ID or name
- `401 UNAUTHORIZED`: Missing or invalid API key

---

#### 23. Update Category

**Endpoint**: `PUT /api/v1/categorias/{id}`

**Description**: Updates an existing category.

**Path Parameters**:
- `id` (String): Category ID

**Request Body**: Same format as Create Category

**Response**: Updated category object

**Status Codes**:
- `200 OK`: Category updated successfully
- `401 UNAUTHORIZED`: Missing or invalid API key
- `404 NOT FOUND`: Category not found

---

#### 24. Delete Category

**Endpoint**: `DELETE /api/v1/categorias/{id}`

**Description**: Deletes a category. Will fail if products reference this category.

**Path Parameters**:
- `id` (String): Category ID

**Response**: No content

**Status Codes**:
- `204 NO CONTENT`: Category deleted successfully
- `400 BAD REQUEST`: Category has associated products
- `401 UNAUTHORIZED`: Missing or invalid API key
- `404 NOT FOUND`: Category not found

---

## Data Models

### Producto (Product Entity)

**Database Table**: `producto`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| idProducto | Integer | PK, Auto-increment | Product ID |
| code | String | Unique, Not null, max 20 | Product code (e.g., "JM001") |
| nombre | String | Not null, max 255 | Product name |
| categoria | Categoria | FK, Not null | Category reference |
| precioCLP | Integer | Not null | Price in Chilean Pesos |
| stock | Integer | Not null, default 0 | Current stock quantity |
| marca | String | max 100 | Brand/manufacturer |
| rating | BigDecimal | precision 2, scale 1 | Product rating (0.0 - 5.0) |
| specs | List&lt;String&gt; | JSONB | Specifications array |
| descripcion | String | TEXT | Product description |
| tags | List&lt;String&gt; | JSONB | Tags for categorization |
| imagen | String | max 500 | Image URL |
| isActivo | Boolean | default true | Active/inactive status |
| creadoEl | OffsetDateTime | TIMESTAMP WITH TZ | Creation timestamp |
| actualizadoEl | OffsetDateTime | TIMESTAMP WITH TZ | Last update timestamp |

**Relationships**:
- Many-to-One: `categoria` (Each product belongs to one category)

**Auto-Lifecycle**:
- `@PrePersist`: Sets `creadoEl`, `actualizadoEl`, defaults `isActivo=true`, `stock=0`
- `@PreUpdate`: Updates `actualizadoEl` to current timestamp

**Example**:
```json
{
  "idProducto": 1,
  "code": "AC001",
  "nombre": "Razer BlackWidow V3 Pro",
  "categoria": {
    "idCategoria": "AC",
    "nombreCategoria": "Accesorios"
  },
  "precioCLP": 159990,
  "stock": 12,
  "marca": "Razer",
  "rating": 4.7,
  "specs": [
    "Switches mecánicos Green",
    "Wireless 2.4GHz + Bluetooth",
    "RGB Chroma",
    "Batería hasta 200 horas"
  ],
  "descripcion": "Teclado mecánico inalámbrico premium para gaming profesional",
  "tags": ["teclado", "mecanico", "wireless", "rgb"],
  "imagen": "https://s3.amazonaws.com/lunari/products/razer-blackwidow-v3-pro.jpg",
  "isActivo": true,
  "creadoEl": "2025-01-02T14:30:00Z",
  "actualizadoEl": "2025-01-03T10:15:00Z"
}
```

---

### Categoria (Category Entity)

**Database Table**: `categoria`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| idCategoria | String | PK, max 10 | Category code (e.g., "JM", "AC") |
| nombreCategoria | String | Unique, Not null, max 100 | Category name |
| descripcion | String | TEXT | Category description |
| creadoEl | OffsetDateTime | TIMESTAMP WITH TZ | Creation timestamp |
| actualizadoEl | OffsetDateTime | TIMESTAMP WITH TZ | Last update timestamp |
| productos | Set&lt;Producto&gt; | OneToMany, Lazy | Products in category |

**Relationships**:
- One-to-Many: `productos` (One category can have many products)

**Auto-Lifecycle**:
- `@PrePersist`: Sets `creadoEl`, `actualizadoEl`
- `@PreUpdate`: Updates `actualizadoEl`

**Example**:
```json
{
  "idCategoria": "JM",
  "nombreCategoria": "Juegos de Mesa",
  "descripcion": "Juegos de mesa tradicionales, modernos y de estrategia",
  "creadoEl": "2025-01-01T10:00:00Z",
  "actualizadoEl": "2025-01-01T10:00:00Z"
}
```

---

## Security & API Keys

### Two-Tier API Key System

**ADMIN Key**:
- Full access to all protected endpoints
- Can create, update, delete products and categories
- Can modify stock and activate/deactivate products
- Used for administrative operations

**SERVICE Key**:
- Limited access for service-to-service communication
- Can only reduce stock (for Carrito service)
- Cannot create, update, or delete resources
- ADMIN key also works for SERVICE-level operations

### ApiKeyFilter Implementation

**How It Works**:
1. Intercepts requests to endpoints with `@RequireApiKey` annotation
2. Extracts API key from `X-API-Key` HTTP header
3. Validates key against configured ADMIN or SERVICE key
4. Checks if key has sufficient permissions for the endpoint
5. Returns 401 Unauthorized if validation fails

**Error Responses**:

Missing API Key:
```json
{
  "success": false,
  "response": null,
  "message": "API key is required. Include 'X-API-Key' header.",
  "statusCode": 401
}
```

Invalid or Insufficient Permissions:
```json
{
  "success": false,
  "response": null,
  "message": "Invalid or insufficient API key permissions.",
  "statusCode": 401
}
```

### Using API Keys

**ADMIN Operations**:
```bash
# Create product (requires ADMIN key)
curl -X POST http://localhost:8082/api/v1/productos \
  -H "X-API-Key: your-admin-key" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "JM001",
    "nombre": "Catan",
    "precioCLP": 45990,
    "categoria": {"idCategoria": "JM"}
  }'
```

**SERVICE Operations**:
```bash
# Reduce stock (requires SERVICE or ADMIN key)
curl -X PATCH "http://localhost:8082/api/v1/productos/1/reducir-stock?cantidad=5" \
  -H "X-API-Key: your-service-key"
```

### Disabling Security (Development Only)

Set in `.env`:
```env
API_SECURITY_ENABLED=false
```

When disabled, all endpoints are accessible without API keys.

---

## Business Logic & Workflows

### Product Creation Workflow

1. **Receive product data** from request
2. **Validate category exists** (throws exception if not found)
3. **Check code uniqueness** (throws exception if duplicate)
4. **Set default values**:
   - stock: 0 (if not provided)
   - isActivo: true
   - creadoEl: current timestamp
   - actualizadoEl: current timestamp
5. **Save to database**
6. **Return created product**

---

### Stock Reduction Workflow

1. **Authenticate** via SERVICE or ADMIN API key
2. **Validate product exists** (throws exception if not found)
3. **Validate quantity** > 0 (throws exception if invalid)
4. **Check sufficient stock** (throws exception if insufficient)
5. **Reduce stock**: `currentStock - quantity`
6. **Update timestamp** (actualizadoEl)
7. **Save to database**
8. **Return updated product**

**Validation Rules**:
- Quantity must be > 0
- Current stock must be >= requested quantity
- Product must exist and be in database

---

### Product Search & Filtering

**By Name** (case-insensitive):
- Uses `ILIKE` query via JPA derived method
- Partial match: "cat" matches "Catan"

**By Price Range**:
- SQL: `WHERE precio_clp BETWEEN :min AND :max`

**By Rating**:
- SQL: `WHERE rating >= :minRating`

**By Tag** (JSONB):
- Native query: `SELECT * FROM producto WHERE tags @> CAST(:tag AS jsonb)`
- Uses PostgreSQL JSONB containment operator

**By Category**:
- JPA relationship traversal: `findByCategoriaIdCategoria`

---

### Category Management

**Create Category**:
1. Validate ID and name uniqueness
2. Set timestamps
3. Save to database
4. Return created category

**Delete Category**:
1. Check if category has associated products
2. If yes: Throw exception (cannot delete)
3. If no: Delete category
4. Return 204 No Content

---

## Configuration

### application.properties

**Location**: `src/main/resources/application.properties`

```properties
# Application
spring.application.name=lunari-inventory-api
server.port=8082

# Database (PostgreSQL)
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=require
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Error Handling
server.error.whitelabel.enabled=false

# Swagger/OpenAPI
springdoc.swagger-ui.path=/swagger-ui
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true

# API Security
api.security.enabled=${API_SECURITY_ENABLED:true}
api.security.admin-key=${ADMIN_API_KEY:}
api.security.service-key=${SERVICE_API_KEY:}
```

---

### Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| DB_HOST | Yes | - | PostgreSQL database host |
| DB_PORT | Yes | - | PostgreSQL port (usually 5432) |
| DB_NAME | Yes | - | Database name |
| DB_USER | Yes | - | Database username |
| DB_PASSWORD | Yes | - | Database password |
| API_SECURITY_ENABLED | No | true | Enable/disable API key authentication |
| ADMIN_API_KEY | Yes* | - | Admin API key (*if security enabled) |
| SERVICE_API_KEY | Yes* | - | Service API key (*if security enabled) |

---

### Spring Profiles

- **default**: Uses main `application.properties`
- **test**: Testing with H2 in-memory database

**Activate Profile**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## Error Handling

### Custom Error Controller

**Endpoint**: `/error`

**Error Response Format**:
```json
{
  "timestamp": 1704288000000,
  "status": 404,
  "error": "Not Found",
  "message": "Product with ID 999 not found",
  "path": "/api/v1/productos/999"
}
```

---

### Common Error Scenarios

| Scenario | Status Code | Message |
|----------|-------------|---------|
| Product not found | 404 | Product with ID {id} not found |
| Category not found | 404 | Category with ID {id} not found |
| Duplicate product code | 400 | Product code already exists |
| Insufficient stock | 400 | Insufficient stock. Available: {stock}, Requested: {quantity} |
| Missing API key | 401 | API key is required. Include 'X-API-Key' header. |
| Invalid API key | 401 | Invalid or insufficient API key permissions. |
| Category has products | 400 | Cannot delete category with associated products |

---

### HTTP Status Codes

- `200 OK`: Successful operation
- `201 CREATED`: Resource created successfully
- `204 NO CONTENT`: Successful deletion
- `400 BAD REQUEST`: Validation error or business rule violation
- `401 UNAUTHORIZED`: Missing or invalid API key
- `404 NOT FOUND`: Resource not found
- `500 INTERNAL SERVER ERROR`: Unexpected server error

---

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProductoServiceTest

# Run with coverage
mvn clean test jacoco:report
```

### Test Database

Tests automatically use H2 in-memory database (configured via test profile).

---

## Common Use Cases

### Use Case 1: Browse Products as Customer

```bash
# 1. Get all active products
curl http://localhost:8082/api/v1/productos/activos

# 2. Search by name
curl "http://localhost:8082/api/v1/productos/buscar?nombre=razer"

# 3. Filter by category
curl http://localhost:8082/api/v1/productos/categoria/AC

# 4. Filter by price range
curl "http://localhost:8082/api/v1/productos/precio?min=10000&max=50000"

# 5. Get highly rated products
curl "http://localhost:8082/api/v1/productos/rating?min=4.5"
```

---

### Use Case 2: Admin Product Management

```bash
# 1. Create new product
curl -X POST http://localhost:8082/api/v1/productos \
  -H "X-API-Key: your-admin-key" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "AC005",
    "nombre": "Logitech G Pro X Wireless",
    "categoria": {"idCategoria": "AC"},
    "precioCLP": 129990,
    "stock": 20,
    "marca": "Logitech",
    "rating": 4.8,
    "specs": ["Wireless", "Surround 7.1", "Batería 20h"],
    "tags": ["audifonos", "wireless", "gaming"]
  }'

# 2. Update stock
curl -X PATCH "http://localhost:8082/api/v1/productos/5/stock?stock=50" \
  -H "X-API-Key: your-admin-key"

# 3. Deactivate product
curl -X PATCH http://localhost:8082/api/v1/productos/5/desactivar \
  -H "X-API-Key: your-admin-key"
```

---

### Use Case 3: Carrito Service Integration

```bash
# Reduce stock during checkout (called by Carrito service)
curl -X PATCH "http://localhost:8082/api/v1/productos/1/reducir-stock?cantidad=3" \
  -H "X-API-Key: your-service-key"
```

---

## Best Practices

1. **Always validate stock** before checkout in client applications
2. **Use product codes** for human-readable references
3. **Leverage JSONB** for flexible product attributes (specs, tags)
4. **Implement image CDN** for performance (e.g., AWS S3, Cloudinary)
5. **Set realistic stock levels** and monitor inventory
6. **Use categories** to organize products logically
7. **Secure API keys** (never commit to version control)
8. **Disable inactive products** instead of deleting them (data preservation)
9. **Use price ranges** for better UX in product filtering
10. **Monitor stock reduction** for potential race conditions in high-traffic scenarios

---

## Troubleshooting

### Issue: Cannot create product - category not found

**Solution**: Ensure category exists by calling `GET /api/v1/categorias`. Create category first if needed.

### Issue: Duplicate product code error

**Solution**: Product codes must be unique. Check existing products or use a different code.

### Issue: Stock reduction fails with insufficient stock

**Solution**: Verify current stock via `GET /api/v1/productos/{id}`. Stock must be >= reduction amount.

### Issue: API key authentication fails

**Solution**:
- Verify API key is included in `X-API-Key` header
- Check key matches configured ADMIN or SERVICE key
- Ensure API security is enabled (`API_SECURITY_ENABLED=true`)
- Verify endpoint requires correct key type (ADMIN vs SERVICE)

### Issue: JSONB queries not working

**Solution**: Ensure PostgreSQL version supports JSONB (9.4+). Verify Hypersistence Utils dependency is included.

---

## Additional Resources

- **Swagger UI**: http://localhost:8082/swagger-ui
- **API Docs (JSON)**: http://localhost:8082/api-docs
- **PostgreSQL JSONB**: https://www.postgresql.org/docs/current/datatype-json.html
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **Spring Boot Docs**: https://spring.io/projects/spring-boot

---

## Support & Contribution

For issues, bugs, or feature requests, please contact the development team or create an issue in the project repository.

---

**Last Updated**: January 3, 2025
**Version**: 0.0.1
**Maintainer**: LUNARi Development Team
