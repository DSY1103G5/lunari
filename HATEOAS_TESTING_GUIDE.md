# API de Usuario HATEOAS - Ejemplos de Pruebas Manuales

Este documento proporciona ejemplos de cómo probar manualmente la implementación HATEOAS en la API de Usuario.

## Requisitos Previos

1. Iniciar la aplicación:
```bash
cd usuario
./mvnw spring-boot:run
```

2. La API estará disponible en: `http://localhost:8080`

## Ejemplos de Llamadas a la API

### 1. Endpoint de Descubrimiento de la API

**Solicitud:**
```bash
curl -X GET "http://localhost:8080/api/v1/users/api" \
  -H "Accept: application/hal+json" \
  -H "Content-Type: application/json"
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "response": {
    "message": "User API - HATEOAS enabled",
    "links": {
      "getAllUsers": "http://localhost:8080/api/v1/users",
      "getPaginatedUsers": "http://localhost:8080/api/v1/users/paginated?page=0&size=10&sort=createdAt,desc",
      "searchUsers": "http://localhost:8080/api/v1/users/search?query={query}&page=0&size=10",
      "getUserById": "http://localhost:8080/api/v1/users/{id}",
      "getUserByEmail": "http://localhost:8080/api/v1/users/email?email={email}",
      "registerUser": "http://localhost:8080/api/v1/users/register",
      "getAllRoles": "http://localhost:8080/api/v1/users/roles",
      "getUserStats": "http://localhost:8080/api/v1/users/stats"
    }
  },
  "message": "Success",
  "statusCode": 200
}
```

### 2. Obtener Usuarios Paginados

**Solicitud:**
```bash
curl -X GET "http://localhost:8080/api/v1/users/paginated?page=0&size=5&sort=firstName,asc" \
  -H "Accept: application/hal+json" \
  -H "Content-Type: application/json"
```

**Estructura de Respuesta Esperada:**
```json
{
  "success": true,
  "response": {
    "users": [
      {
        "id": "uuid-here",
        "firstName": "John",
        "lastName": "Doe",
        "email": "john@example.com",
        "active": true,
        "verified": true,
        "fullName": "John Doe",
        "status": "active",
        "_links": {
          "self": {"href": "http://localhost:8080/api/v1/users/uuid-here"},
          "users": {"href": "http://localhost:8080/api/v1/users"},
          "update": {"href": "http://localhost:8080/api/v1/users/uuid-here"},
          "delete": {"href": "http://localhost:8080/api/v1/users/uuid-here"},
          "deactivate": {"href": "http://localhost:8080/api/v1/users/uuid-here/status?active=false"},
          "change-password": {"href": "http://localhost:8080/api/v1/users/uuid-here/password"}
        }
      }
    ],
    "page": {
      "totalElements": 10,
      "totalPages": 2,
      "currentPage": 0,
      "pageSize": 5,
      "hasNext": true,
      "hasPrevious": false,
      "isFirst": true,
      "isLast": false
    },
    "_links": {
      "self": {"href": "http://localhost:8080/api/v1/users/paginated?page=0&size=5"},
      "first": {"href": "http://localhost:8080/api/v1/users/paginated?page=0&size=5"},
      "last": {"href": "http://localhost:8080/api/v1/users/paginated?page=1&size=5"},
      "next": {"href": "http://localhost:8080/api/v1/users/paginated?page=1&size=5"}
    }
  },
  "message": "Success",
  "statusCode": 200
}
```

### 3. Obtener Usuario Individual

**Solicitud:**
```bash
curl -X GET "http://localhost:8080/api/v1/users/{user-id}" \
  -H "Accept: application/hal+json" \
  -H "Content-Type: application/json"
```

**Estructura de Respuesta Esperada:**
```json
{
  "success": true,
  "response": {
    "id": "uuid-here",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "active": true,
    "verified": true,
    "fullName": "John Doe",
    "status": "active",
    "_links": {
      "self": {"href": "http://localhost:8080/api/v1/users/uuid-here"},
      "users": {"href": "http://localhost:8080/api/v1/users"},
      "update": {"href": "http://localhost:8080/api/v1/users/uuid-here"},
      "delete": {"href": "http://localhost:8080/api/v1/users/uuid-here"},
      "deactivate": {"href": "http://localhost:8080/api/v1/users/uuid-here/status?active=false"},
      "change-password": {"href": "http://localhost:8080/api/v1/users/uuid-here/password"}
    }
  },
  "message": "Success",
  "statusCode": 200
}
```

### 4. Registrar Nuevo Usuario

**Solicitud:**
```bash
curl -X POST "http://localhost:8080/api/v1/users/register" \
  -H "Accept: application/hal+json" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "password": "securePassword123",
    "roleId": 1
  }'
```

**Estructura de Respuesta Esperada:**
```json
{
  "success": true,
  "response": {
    "id": "new-uuid-here",
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "active": true,
    "verified": false,
    "fullName": "Jane Smith",
    "status": "pending_verification",
    "_links": {
      "self": {"href": "http://localhost:8080/api/v1/users/new-uuid-here"},
      "users": {"href": "http://localhost:8080/api/v1/users"},
      "update": {"href": "http://localhost:8080/api/v1/users/new-uuid-here"},
      "delete": {"href": "http://localhost:8080/api/v1/users/new-uuid-here"},
      "deactivate": {"href": "http://localhost:8080/api/v1/users/new-uuid-here/status?active=false"},
      "change-password": {"href": "http://localhost:8080/api/v1/users/new-uuid-here/password"},
      "verify": {"href": "http://localhost:8080/api/v1/users/verify"}
    }
  },
  "message": "Success",
  "statusCode": 201
}
```

### 5. Buscar Usuarios

**Solicitud:**
```bash
curl -X GET "http://localhost:8080/api/v1/users/search?query=John&page=0&size=10" \
  -H "Accept: application/hal+json" \
  -H "Content-Type: application/json"
```

### 6. Obtener Usuarios por Empresa

**Solicitud:**
```bash
curl -X GET "http://localhost:8080/api/v1/users/company/{company-id}?page=0&size=10" \
  -H "Accept: application/hal+json" \
  -H "Content-Type: application/json"
```

### 7. Obtener Usuarios por Rol

**Solicitud:**
```bash
curl -X GET "http://localhost:8080/api/v1/users/role/1?page=0&size=10" \
  -H "Accept: application/hal+json" \
  -H "Content-Type: application/json"
```

## Comportamiento de Enlaces HATEOAS

### Enlaces Condicionales

La API proporciona diferentes enlaces basados en el estado del usuario:

1. **Usuarios Verificados**: No muestran enlace de verificación
2. **Usuarios No Verificados**: Muestran enlace de verificación
3. **Usuarios Activos**: Muestran enlace de desactivación
4. **Usuarios Inactivos**: Muestran enlace de activación
5. **Usuarios con Empresa**: Muestran enlace company-users
6. **Usuarios con Rol**: Muestran enlaces role-users y assign-role

### Enlaces de Navegación

Las respuestas paginadas incluyen enlaces de navegación:
- `first`: Primera página
- `last`: Última página  
- `next`: Página siguiente (si está disponible)
- `previous`: Página anterior (si está disponible)
- `self`: Página actual

## Pruebas con Diferentes Tipos de Media

Puedes probar con diferentes encabezados Accept:

1. **HAL JSON (Recomendado):**
   ```bash
   -H "Accept: application/hal+json"
   ```

2. **JSON Regular:**
   ```bash
   -H "Accept: application/json"
   ```

3. **Cualquiera:**
   ```bash
   -H "Accept: */*"
   ```

## Manejo de Errores

La API mantiene los principios HATEOAS incluso en las respuestas de error:

**Ejemplo 404 No Encontrado:**
```json
{
  "success": false,
  "message": "Usuario no encontrado",
  "statusCode": 404
}
```

**Ejemplo 400 Solicitud Incorrecta:**
```json
{
  "success": false,
  "message": "Email no puede ser nulo o vacío",
  "statusCode": 400
}
```

## Consejos de Implementación para Clientes

1. **Seguir Enlaces**: Siempre usa los enlaces proporcionados en lugar de construir URLs
2. **Verificar Disponibilidad de Enlaces**: Verifica que los enlaces existan antes de usarlos
3. **Manejar Enlaces Faltantes**: Algunos enlaces son condicionales basados en el estado del usuario
4. **Usar Enlaces Self**: Usa enlaces self para refrescar recursos
5. **Navegar Páginas**: Usa enlaces next/previous para paginación

## Pruebas en Navegador

También puedes probar la API en un navegador web visitando:
- `http://localhost:8080/api/v1/users/api` - Descubrimiento de API
- `http://localhost:8080/api/v1/users/paginated` - Usuarios paginados

El navegador mostrará la respuesta JSON con todos los enlaces HATEOAS.
