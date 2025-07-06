# API de Usuario con Implementación HATEOAS

Este proyecto implementa HATEOAS (Hypermedia as the Engine of Application State) en la API de Usuario, proporcionando servicios REST auto-descriptivos con enlaces de navegación.

## ¿Qué es HATEOAS?

HATEOAS es una restricción de la arquitectura de aplicaciones REST que mantiene el cliente y el servidor desacoplados. Un cliente interactúa con una aplicación de red completamente a través de hipermedia proporcionada dinámicamente por los servidores de aplicación.

## Características Implementadas

### 1. UserRepresentation
- **Propósito**: DTO que extiende `RepresentationModel<UserRepresentation>` para soportar enlaces HATEOAS
- **Ubicación**: `cl.duoc.lunari.api.user.dto.UserRepresentation`
- **Características**:
  - Todos los campos de usuario excepto contraseña (por seguridad)
  - Propiedades computadas: `fullName` y `status`
  - Serialización automática JSON con enlaces HATEOAS

### 2. UserModelAssembler
- **Propósito**: Convierte entidades User a UserRepresentation con enlaces apropiados
- **Ubicación**: `cl.duoc.lunari.api.user.assembler.UserModelAssembler`
- **Enlaces Agregados**:
  - `self`: Enlace al recurso del usuario
  - `users`: Enlace a la colección de todos los usuarios
  - `update`: Enlace para actualizar el usuario
  - `delete`: Enlace para eliminar el usuario
  - `activate`/`deactivate`: Enlaces basados en el estado del usuario
  - `change-password`: Enlace para cambiar contraseña
  - `company-users`: Enlace a usuarios de la misma empresa (si aplica)
  - `role-users`: Enlace a usuarios con el mismo rol (si aplica)
  - `assign-role`: Enlace para asignar rol al usuario
  - `verify`: Enlace para verificar usuario (si no está verificado)

### 3. PagedUserRepresentation
- **Propósito**: Representación de colección paginada con enlaces de navegación
- **Ubicación**: `cl.duoc.lunari.api.user.dto.PagedUserRepresentation`
- **Características**:
  - Metadatos de página (elementos totales, páginas, página actual, etc.)
  - Enlaces de navegación (primero, último, anterior, siguiente)

### 4. PagedUserModelAssembler
- **Propósito**: Crea representaciones paginadas con enlaces de navegación
- **Ubicación**: `cl.duoc.lunari.api.user.assembler.PagedUserModelAssembler`
- **Enlaces Agregados**:
  - `self`: Enlace de página actual
  - `first`: Enlace de primera página
  - `last`: Enlace de última página
  - `previous`: Enlace de página anterior (si está disponible)
  - `next`: Enlace de página siguiente (si está disponible)
  - `roles`: Enlace a todos los roles
  - `stats`: Enlace a estadísticas de usuarios
  - `search`: Enlace a funcionalidad de búsqueda

### 5. Endpoint Raíz de la API
- **Endpoint**: `GET /api/v1/users/api`
- **Propósito**: Proporciona endpoint de descubrimiento para la API
- **Retorna**: Todos los endpoints disponibles de la API con sus URLs

## Endpoints de la API con HATEOAS

### 1. Obtener Todos los Usuarios (Simple)
```http
GET /api/v1/users
```
**Formato de Respuesta**:
```json
{
  "success": true,
  "data": {
    "_embedded": {
      "userRepresentationList": [...]
    },
    "_links": {
      "self": {"href": "http://localhost:8080/api/v1/users"},
      "paginated": {"href": "http://localhost:8080/api/v1/users/paginated?page=0&size=10&sort=createdAt,desc"},
      "roles": {"href": "http://localhost:8080/api/v1/users/roles"},
      "stats": {"href": "http://localhost:8080/api/v1/users/stats"}
    }
  }
}
```

### 2. Obtener Usuarios Paginados
```http
GET /api/v1/users/paginated?page=0&size=10&sort=firstName,asc
```
**Formato de Respuesta**:
```json
{
  "success": true,
  "data": {
    "users": [
      {
        "id": "uuid",
        "firstName": "John",
        "lastName": "Doe",
        "email": "john@example.com",
        "fullName": "John Doe",
        "status": "active",
        "_links": {
          "self": {"href": "http://localhost:8080/api/v1/users/uuid"},
          "users": {"href": "http://localhost:8080/api/v1/users"},
          "update": {"href": "http://localhost:8080/api/v1/users/uuid"},
          "delete": {"href": "http://localhost:8080/api/v1/users/uuid"},
          "deactivate": {"href": "http://localhost:8080/api/v1/users/uuid/status?active=false"},
          "change-password": {"href": "http://localhost:8080/api/v1/users/uuid/password"}
        }
      }
    ],
    "page": {
      "totalElements": 100,
      "totalPages": 10,
      "currentPage": 0,
      "pageSize": 10,
      "hasNext": true,
      "hasPrevious": false,
      "isFirst": true,
      "isLast": false
    },
    "_links": {
      "self": {"href": "http://localhost:8080/api/v1/users/paginated?page=0&size=10"},
      "first": {"href": "http://localhost:8080/api/v1/users/paginated?page=0&size=10"},
      "last": {"href": "http://localhost:8080/api/v1/users/paginated?page=9&size=10"},
      "next": {"href": "http://localhost:8080/api/v1/users/paginated?page=1&size=10"}
    }
  }
}
```

### 3. Obtener Usuario Individual
```http
GET /api/v1/users/{id}
```
**Formato de Respuesta**:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "status": "active",
    "_links": {
      "self": {"href": "http://localhost:8080/api/v1/users/uuid"},
      "users": {"href": "http://localhost:8080/api/v1/users"},
      "update": {"href": "http://localhost:8080/api/v1/users/uuid"},
      "delete": {"href": "http://localhost:8080/api/v1/users/uuid"},
      "deactivate": {"href": "http://localhost:8080/api/v1/users/uuid/status?active=false"},
      "change-password": {"href": "http://localhost:8080/api/v1/users/uuid/password"}
    }
  }
}
```

### 4. Endpoint de Descubrimiento de la API
```http
GET /api/v1/users/api
```
**Formato de Respuesta**:
```json
{
  "success": true,
  "data": {
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
  }
}
```

## Configuración

### Formato HAL
La API utiliza formato HAL (Hypertext Application Language), configurado en `HateoasConfig.java`:
```java
@Configuration
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class HateoasConfig {
    // Habilita formato HAL para hipervínculos consistentes
}
```

### Dependencias Agregadas
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```

## Beneficios de Esta Implementación

1. **Auto-descriptivo**: Los clientes pueden descubrir acciones disponibles a través de enlaces
2. **Desacoplamiento**: Los clientes no necesitan hardcodear URLs
3. **Evolucionabilidad**: El servidor puede cambiar URLs sin romper los clientes
4. **Descubribilidad**: El endpoint raíz de la API ayuda con la exploración
5. **Enlaces Condicionales**: Los enlaces aparecen basados en el estado del usuario (ej. enlace verify para usuarios no verificados)

## Ejemplos de Uso

### Cliente Siguiendo Enlaces
```javascript
// Obtener usuario
const userResponse = await fetch('/api/v1/users/123');
const user = userResponse.json().data;

// Seguir enlace self para refrescar
const refreshedUser = await fetch(user._links.self.href);

// Seguir enlace update para modificar usuario
await fetch(user._links.update.href, {
    method: 'PUT',
    body: JSON.stringify(updatedUserData)
});

// Seguir enlace deactivate si está disponible
if (user._links.deactivate) {
    await fetch(user._links.deactivate.href, { method: 'PATCH' });
}
```

### Navegación de Paginación
```javascript
// Obtener usuarios paginados
const response = await fetch('/api/v1/users/paginated');
const pagedUsers = response.json().data;

// Navegar por páginas usando enlaces
if (pagedUsers._links.next) {
    const nextPage = await fetch(pagedUsers._links.next.href);
}
```

Esta implementación HATEOAS hace que la API de Usuario sea más descubrible, mantenible, y siga más de cerca los principios REST.
