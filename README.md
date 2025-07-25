# Microservicios LUNARi



Este proyecto contiene el desarrollo de 3 microservicios para la plataforma LUNARi, empresa dedicada a empoderar emprendedores del comercio minorista mediante soluciones de comercio electrónico.

## Descripción del Proyecto

LUNARi permite a los usuarios contar con un portal donde pueden ver el avance en el desarrollo de su aplicación, ver maquetas, requerimientos y monitorear el estado del sistema una vez desplegado. Este sistema de microservicios forma parte de la modernización de su arquitectura para mejorar el rendimiento y escalabilidad.

## Estructura del Proyecto

```
├── usuario/                  # Microservicio de gestión de usuarios
│   ├── src/main/java/cl/duoc/lunari/api/user/
│   │   ├── assembler/        # Conversores HATEOAS
│   │   ├── config/           # Configuraciones (DotEnv, OpenAPI)
│   │   ├── controller/       # Controladores REST
│   │   ├── dto/              # Objetos de transferencia de datos
│   │   ├── model/            # Entidades JPA
│   │   ├── repository/       # Repositorios de datos
│   │   └── service/          # Lógica de negocio
│   ├── src/test/java/cl/duoc/lunari/api/user/
│   │   ├── assembler/        # Tests de HATEOAS
│   │   ├── controller/       # Tests de controladores
│   │   ├── model/            # Tests de entidades
│   │   ├── repository/       # Tests de repositorios
│   │   └── service/          # Tests de servicios
│   └── src/main/resources/
│       ├── .env              # Variables de entorno
│       └── application.properties
├── inventario/       # Microservicio de gestión de inventario
│   ├── src/main/java/cl/duoc/lunari/api/cart/
│   │   ├── config/           # Configuraciones (DotEnv, OpenAPI)
│   │   ├── controller/       # Controladores REST
│   │   ├── model/            # Entidades JPA
│   │   ├── repository/       # Repositorios de datos
│   │   └── service/          # Lógica de negocio y clientes externos
│   └── src/main/resources/
│       ├── .env              # Variables de entorno
│       └── application.properties
├── carrito/          # Microservicio de gestión de carrito de compras
│   ├── src/main/java/cl/duoc/lunari/api/cart/
│   │   ├── config/           # Configuraciones (DotEnv, OpenAPI, RestTemplate)
│   │   ├── controller/       # Controladores REST
│   │   ├── dto/              # Objetos de transferencia de datos
│   │   ├── model/            # Entidades JPA
│   │   ├── repository/       # Repositorios de datos
│   │   └── service/          # Lógica de negocio y clientes externos
│   └── src/main/resources/
│       ├── .env              # Variables de entorno
│       └── application.properties
├── script_creacion_tablas.sql    # Script SQL para crear las tablas
├── informe.pdf       # Informe técnico del proyecto
├── README.md         # Este archivo
└── seeds/          # Microservicio de gestión de carrito de compras
    ├── cart_seed.sql  # Datos de ejemplo para el carrito
    ├── inventory_seed.sql  # Datos de ejemplo para el inventario
    └── user_seed.sql  # Datos de ejemplo para los usuarios
```

## Microservicios

### 1. Usuario
- **Propósito**: Gestión de usuarios del sistema (administradores, product owners, clientes, devops)
- **Funcionalidades**:
  - Obtener usuarios con paginación
  - Obtener usuario por ID
  - Obtener usuarios por email
  - Obtener usuarios por empresa
  - Obtener usuarios por rol
  - Registrar nuevos usuarios
  - Actualizar usuarios
  - Activar/Desactivar usuarios
  - Cambiar contraseña de usuarios
  - Eliminar usuarios
- **Características técnicas**:
  - Implementación completa de HATEOAS (Hypermedia as the Engine of Application State)
  - Enlaces dinámicos basados en el estado del usuario
  - Cobertura completa de tests unitarios
  - Documentación automática con Swagger/OpenAPI

### 2. Inventario
- **Propósito**: Gestión de recursos y servicios disponibles
- **Funcionalidades**:
  - Obtener inventario
  - Obtener servicio por ID
  - Obtener servicios por nombre
  - Obtener servicios activos
  - Obtener servicios por categoría
  - Crear servicios
  - Actualizar servicios
  - Activar/Desactivar servicios
  - Eliminar servicios

### 3. Carrito
- **Propósito**: Gestión del carrito de compras y proyectos
- **Funcionalidades**:
  - Obtener/Crear carrito por usuario
  - Obtener carrito por ID
  - Obtener todos los carritos del usuario
  - Agregar items al carrito
  - Actualizar la cantidad de items del carrito
  - Eliminar items del carrito
  - Procesar el carrito (crear proyecto)
  - Vaciar el carrito
  - Ver estadísticas de los carritos

### Arquitectura Interna de Microservicios

Cada microservicio sigue una arquitectura en capas estándar de Spring Boot:

- **Controllers**: Manejo de peticiones HTTP y endpoints REST
- **Services**: Lógica de negocio y orquestación
- **Repositories**: Acceso a datos mediante JPA
- **Models**: Entidades de base de datos
- **DTOs**: Objetos de transferencia para comunicación externa
- **Assemblers**: Conversión de entidades a representaciones HATEOAS
- **Config**: Configuraciones de la aplicación (seguridad, OpenAPI, etc.)

El microservicio de **carrito** incluye clientes para comunicación con otros servicios (UsuarioServiceClient, InventarioServiceClient). Para ello se requiere que los microservicios de Usuario e Inventario estén en ejecución y accesibles.

## Tecnologías Utilizadas

- **Framework**: Spring Boot 3.x
- **Gestión de dependencias**: Maven
- **Base de datos**: SQL (PostgreSQL para producción)
- **ORM**: JPA/Hibernate
- **HATEOAS**: Spring HATEOAS para APIs RESTful hipermedia
- **Documentación API**: 
  - OpenAPI 3.0 para especificación de API
  - SwaggerUI como framework de visualización interactiva
- **Testing Framework**:
  - JUnit 5 para estructura de pruebas unitarias
  - Mockito para mocking y simulación de dependencias
  - Spring Boot Test para testing de integración
  - H2 Database como base de datos en memoria para pruebas
- **Control de versiones**: Git

## Requisitos Previos

- Java 11 o superior
- Maven 3.6+
- Base de datos PostgresSQL compatible
- **Tener configurado los .env con las variables de entorno necesarias para cada microservicio**

## Instalación y Ejecución

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/DSY1103G5/lunari.git
   cd lunari
   ```

2. **Crear las tablas de la base de datos**:
    Utilizar el script sql proporcionado para crear las tablas necesarias en la base de datos.

3. **Ejecutar cada microservicio** (en ventanas separadas):
   
   **Terminal 1 - Usuario**:
   ```bash
   cd usuario/
   mvn spring-boot:run
   ```
   
   **Terminal 2 - Inventario**:
   ```bash
   cd inventario/
   mvn spring-boot:run
   ```
   
   **Terminal 3 - Carrito**:
   ```bash
   cd carrito/
   mvn spring-boot:run
   ```

## Endpoints

Cada microservicio expone un endpoint principal que maneja su respectivo recurso:

- **Usuario**: `http://localhost:8080/api/v1/users`
- **Inventario**: `http://localhost:8081/api/v1/inventory`
- **Carrito**: `http://localhost:8082/api/v1/cart`

### Documentación API

Cada microservicio incluye documentación automática de la API utilizando **SwaggerUI** como framework de visualización:

- **Usuario - SwaggerUI**: `http://localhost:8080/swagger-ui/index.html`
- **Usuario - OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **Inventario - SwaggerUI**: `http://localhost:8081/swagger-ui/index.html`
- **Carrito - SwaggerUI**: `http://localhost:8082/swagger-ui/index.html`

**SwaggerUI** proporciona:
- Interfaz interactiva para probar endpoints
- Documentación automática basada en anotaciones
- Especificación OpenAPI 3.0 completa
- Validación de esquemas en tiempo real

### Características HATEOAS

El microservicio de **Usuario** implementa HATEOAS completamente:

- **Enlaces dinámicos**: Cada respuesta incluye enlaces relevantes basados en el estado del recurso
- **Navegabilidad**: Los clientes pueden descubrir acciones disponibles a través de los enlaces
- **Enlaces condicionales**: 
  - `activate`/`deactivate` según el estado del usuario
  - `verify` para usuarios no verificados
  - `company-users` para usuarios con empresa asignada
  - `role-users` para usuarios con rol específico

## Pruebas

### Tests Unitarios

El proyecto incluye una suite completa de tests unitarios utilizando **JUnit 5**, **Mockito** y **H2 Database**:

```bash
# Ejecutar tests del microservicio Usuario
cd usuario/
mvn test

# Ver reporte de cobertura
mvn clean test jacoco:report
```

**Stack de Testing implementado:**
- **JUnit 5**: Framework principal para estructura y ejecución de pruebas
- **Mockito**: Framework de mocking para simular dependencias y aislar unidades de código
- **H2 Database**: Base de datos en memoria para pruebas de repositorio e integración
- **Spring Boot Test**: Anotaciones y utilidades para testing de Spring Boot
- **MockMvc**: Para testing de controladores REST sin levantar servidor

**Cobertura de tests implementada:**
- **UserRepositoryTest**: Tests de repository con H2 como base de datos en memoria
- **UserTest**: Tests de la entidad User (validaciones, lógica de negocio, constructores)
- **UserServiceImplTest**: Tests del service con mocks de Mockito para aislar dependencias
- **UserControllerTest**: Tests del controller con MockMvc para simulación de peticiones HTTP
- **UserModelAssemblerTest**: Tests de HATEOAS y enlaces dinámicos con mocks

**Configuración de Testing:**
- **H2 Database**: Configurada en `application-test.properties` para tests rápidos en memoria
- **Profile de Test**: Configuración específica que no interfiere con el entorno de desarrollo
- **Datos de Prueba**: Creación automática de esquemas y datos mediante Hibernate DDL

### Tests de Integración

Las pruebas de integración se realizan utilizando **Postman** para validar el comportamiento end-to-end:

**Herramientas de Testing de Integración:**
- **Postman**: Colecciones de pruebas para validar endpoints completos
- **SwaggerUI**: Testing interactivo directo desde la interfaz de documentación
- **Curl**: Scripts de prueba para automatización

**Características del entorno de testing:**
- **H2 Database**: Base de datos en memoria completamente aislada para tests
- **Configuración específica**: `application-test.properties` para entorno de pruebas
- **Tests bilingües**: Comentarios en español para mejor comprensión del equipo
- **Cobertura integral**: Casos de uso normales, edge cases y manejo de errores

### Coleccion de Usuario
https://github.com/user-attachments/assets/4946f639-ffbc-43bf-85b2-8491a84599d7

### Coleccion de Inventario
https://github.com/user-attachments/assets/23afb466-9f81-45f6-8de2-69d9503ea8b1

### Coleccion de Carrito
https://github.com/user-attachments/assets/1db80e01-047f-4bc6-8b5f-24a0ddb2246b



## Arquitectura

## Contribución

Este proyecto utiliza Git Flow:

- **main**: Rama principal con código estable
- **development**: Rama de desarrollo donde se integran las características

Todos los integrantes del equipo deben trabajar en la rama `develop` y crear pull requests hacia `main`.

## Entregables

- [x] 3 microservicios funcionales (usuarios, inventario, carrito)
- [x] Implementación completa de HATEOAS en microservicio Usuario
- [x] Suite completa de tests unitarios con cobertura integral
- [x] Documentación automática con Swagger/OpenAPI
- [x] Configuración de base de datos H2 para testing
- [x] Script SQL de creación de tablas
- [x] Colección de Postman compartida
- [x] Excel con ejemplos de llamadas
- [x] Informe técnico completo
- [x] README.md actualizado

## Equipo de Desarrollo

- Oscar Muñoz
- Angelo Millán
- Cristobal Águila
- Rigoberto Ávila

## Licencia

Este proyecto es desarrollado como parte de la evaluación académica para el curso DSY1103.
