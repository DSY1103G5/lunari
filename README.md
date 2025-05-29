# Microservicios LUNARi


https://github.com/user-attachments/assets/4946f639-ffbc-43bf-85b2-8491a84599d7


Este proyecto contiene el desarrollo de 3 microservicios para la plataforma LUNARi, empresa dedicada a empoderar emprendedores del comercio minorista mediante soluciones de comercio electrónico.

## Descripción del Proyecto

LUNARi permite a los usuarios contar con un portal donde pueden ver el avance en el desarrollo de su aplicación, ver maquetas, requerimientos y monitorear el estado del sistema una vez desplegado. Este sistema de microservicios forma parte de la modernización de su arquitectura para mejorar el rendimiento y escalabilidad.

## Estructura del Proyecto

```
├── usuario/                  # Microservicio de gestión de usuarios
│   ├── src/main/java/cl/duoc/lunari/api/cart/
│   │   ├── config/           # Configuraciones (DotEnv, OpenAPI)
│   │   ├── controller/       # Controladores REST
│   │   ├── model/            # Entidades JPA
│   │   ├── repository/       # Repositorios de datos
│   │   └── service/          # Lógica de negocio y clientes externos
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
  - Obtener usuarios
  - Obtener usuario por ID
  - Obtener usuarios por email
  - Registrar nuevos usuarios
  - Actualizar usuarios
  - Eliminar usuarios

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
- **Config**: Configuraciones de la aplicación (seguridad, OpenAPI, etc.)

El microservicio de **carrito** incluye clientes para comunicación con otros servicios (UsuarioServiceClient, InventarioServiceClient). Para ello se requiere que los microservicios de Usuario e Inventario estén en ejecución y accesibles.

## Tecnologías Utilizadas

- **Framework**: Spring Boot
- **Gestión de dependencias**: Maven
- **Base de datos**: SQL
- **ORM**: JPA/Hibernate
- **Control de versiones**: Git
- **Testing**: Postman

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

## Pruebas

Las pruebas se realizan utilizando Postman. Se compartirá una colección de Postman con ejemplos de uso para cada endpoint.


## Arquitectura

## Contribución

Este proyecto utiliza Git Flow:

- **main**: Rama principal con código estable
- **development**: Rama de desarrollo donde se integran las características

Todos los integrantes del equipo deben trabajar en la rama `develop` y crear pull requests hacia `main`.

## Entregables

- [x] 3 microservicios funcionales (usuarios, inventario, carrito)
- [x] Script SQL de creación de tablas
- [x] Colección de Postman compartida
- [x] Excel con ejemplos de llamadas
- [x] Informe técnico completo
- [x] README.md

## Equipo de Desarrollo

- Oscar Muñoz
- Angelo Millán
- Cristobal Águila
- Rigoberto Ávila

## Licencia

Este proyecto es desarrollado como parte de la evaluación académica para el curso DSY1103.
