-- USUARIO
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS membresia_equipo CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;
DROP TABLE IF EXISTS empresa_cliente CASCADE;
DROP TABLE IF EXISTS rol_permiso CASCADE;
DROP TABLE IF EXISTS permiso CASCADE;
DROP TABLE IF EXISTS rol CASCADE;

CREATE TABLE Rol (
    id_rol SERIAL PRIMARY KEY,
    nombre_rol VARCHAR(50) UNIQUE NOT NULL,
    descripcion TEXT,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Permiso (
    id_permiso SERIAL PRIMARY KEY,
    nombre_permiso VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Rol_Permiso (
    id_rol INT NOT NULL,
    id_permiso INT NOT NULL,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_rol, id_permiso),
    FOREIGN KEY (id_rol) REFERENCES Rol(id_rol) ON DELETE CASCADE,
    FOREIGN KEY (id_permiso) REFERENCES Permiso(id_permiso) ON DELETE CASCADE
);

CREATE TABLE Empresa_Cliente (
    id_empresa UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre_empresa VARCHAR(255) NOT NULL,
    razon_social VARCHAR(100) UNIQUE,
    industria VARCHAR(100),
    sitio_web VARCHAR(255),
    id_usuario_contacto UUID,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Usuario (
    id_usuario UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre_usuario VARCHAR(100) NOT NULL,
    apellido_usuario VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    imagen_perfil VARCHAR(255),
    id_rol INT NOT NULL,
    id_empresa UUID,
    activo BOOLEAN DEFAULT TRUE,
    ultimo_login TIMESTAMP WITH TIME ZONE,
    verificado BOOLEAN DEFAULT FALSE,
    token_verificacion VARCHAR(100),
    token_expiracion TIMESTAMP WITH TIME ZONE,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rol) REFERENCES Rol(id_rol) ON DELETE RESTRICT,
    FOREIGN KEY (id_empresa) REFERENCES Empresa_Cliente(id_empresa) ON DELETE SET NULL
);

ALTER TABLE Empresa_Cliente
ADD CONSTRAINT fk_usuario_contacto
FOREIGN KEY (id_usuario_contacto) REFERENCES Usuario(id_usuario) ON DELETE SET NULL;

CREATE TABLE Membresia_Equipo (
    id_membresia UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_proyecto_ext UUID NOT NULL,
    id_usuario UUID NOT NULL,
    rol_proyecto VARCHAR(50) NOT NULL,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario) ON DELETE CASCADE
);

-- Indices para optimización
CREATE INDEX idx_usuario_email ON Usuario(email);
CREATE INDEX idx_usuario_rol ON Usuario(id_rol);
CREATE INDEX idx_usuario_empresa ON Usuario(id_empresa);
CREATE INDEX idx_empresa_cliente_nombre ON Empresa_Cliente(nombre_empresa);
CREATE INDEX idx_membresia_equipo_proyecto ON Membresia_Equipo(id_proyecto_ext);
CREATE INDEX idx_membresia_equipo_usuario ON Membresia_Equipo(id_usuario);


-- INVENTARIO
CREATE TABLE Categoria (
    id_categoria SERIAL PRIMARY KEY,
    nombre_categoria VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Catalogo (
    id_servicio SERIAL PRIMARY KEY,
    id_categoria INT,
    nombre_servicio VARCHAR(255) UNIQUE NOT NULL,
    descripcion TEXT,
    precio_base DECIMAL(10, 2) CHECK (precio_base >= 0),
    is_activo BOOLEAN DEFAULT TRUE, 
    duracion_estimada_dias INT CHECK (duracion_estimada_dias > 0),
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_categoria) REFERENCES Categoria(id_categoria) ON DELETE SET NULL
);

CREATE TABLE TipoRecurso (
    id_tipo_recurso SERIAL PRIMARY KEY,
    nombre_tipo_recurso VARCHAR(100) UNIQUE NOT NULL,
    unidad_medida VARCHAR(50) NOT NULL DEFAULT 'Hour',
    tarifa_base_por_hora DECIMAL(10, 2) CHECK (tarifa_base_por_hora >= 0),
    descripcion TEXT,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
    
CREATE TABLE PaqueteRecursoServicio (
    id_paquete_recurso_servicio SERIAL PRIMARY KEY,
    id_servicio INT NOT NULL,
    id_tipo_recurso INT NOT NULL,
    cantidad_estimado DECIMAL(8, 2) NOT NULL CHECK (cantidad_estimado > 0), 
    notas TEXT,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_servicio) REFERENCES Catalogo(id_servicio) ON DELETE CASCADE,
    FOREIGN KEY (id_tipo_recurso) REFERENCES TipoRecurso(id_tipo_recurso) ON DELETE RESTRICT, 
    UNIQUE (id_servicio, id_tipo_recurso) 
);

CREATE TABLE ServicioAdicional (
    id_servicio_adicional SERIAL PRIMARY KEY,
    nombre_adicional VARCHAR(150) UNIQUE NOT NULL,
    descripcion TEXT,
    precio_adicional DECIMAL(10, 2) NOT NULL CHECK (precio_adicional >= 0),
    is_activo BOOLEAN DEFAULT TRUE,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ServicioServicioAdicional (
    id_servicio INT NOT NULL,
    id_servicio_adicional INT NOT NULL,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_servicio, id_servicio_adicional),
    FOREIGN KEY (id_servicio) REFERENCES Catalogo(id_servicio) ON DELETE CASCADE,
    FOREIGN KEY (id_servicio_adicional) REFERENCES ServicioAdicional(id_servicio_adicional) ON DELETE CASCADE
);

CREATE INDEX idx_servicio_catalogo_categoriaid ON Catalogo(id_categoria);
CREATE INDEX idx_servicio_catalogo_servicename ON Catalogo(nombre_servicio);
CREATE INDEX idx_tipo_recurso_nombre ON TipoRecurso(nombre_tipo_recurso);
CREATE INDEX idx_paquete_recurso_servicioid ON PaqueteRecursoServicio(id_servicio);
CREATE INDEX idx_paquete_recurso_resourcetypeid ON PaqueteRecursoServicio(id_tipo_recurso);
CREATE INDEX idx_servicio_adicional_nombre ON ServicioAdicional(nombre_adicional);

-- CARRITO
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS carrito_servicio_adicional CASCADE;
DROP TABLE IF EXISTS carrito_item CASCADE;
DROP TABLE IF EXISTS carrito CASCADE;

CREATE TABLE Carrito (
    id_carrito UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_usuario_ext UUID NOT NULL,
    estado_carrito VARCHAR(20) NOT NULL DEFAULT 'ACTIVO' CHECK (estado_carrito IN ('ACTIVO', 'PROCESADO', 'ABANDONADO', 'EXPIRADO')),
    total_estimado DECIMAL(12, 2) DEFAULT 0.00 CHECK (total_estimado >= 0),
    notas_cliente TEXT,
    fecha_expiracion TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP + INTERVAL '30 days'),
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Carrito_Item (
    id_carrito_item UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_carrito UUID NOT NULL,
    id_servicio_ext INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1 CHECK (cantidad > 0),
    precio_unitario DECIMAL(10, 2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal DECIMAL(12, 2) NOT NULL CHECK (subtotal >= 0),
    personalizaciones TEXT, 
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_carrito) REFERENCES Carrito(id_carrito) ON DELETE CASCADE,
    UNIQUE (id_carrito, id_servicio_ext)
);

CREATE TABLE Carrito_Servicio_Adicional (
    id_carrito_servicio_adicional UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_carrito_item UUID NOT NULL,
    id_servicio_adicional_ext INT NOT NULL,
    precio_adicional DECIMAL(10, 2) NOT NULL CHECK (precio_adicional >= 0),
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_carrito_item) REFERENCES Carrito_Item(id_carrito_item) ON DELETE CASCADE,
    UNIQUE (id_carrito_item, id_servicio_adicional_ext)
);

CREATE INDEX idx_carrito_usuario ON Carrito(id_usuario_ext);
CREATE INDEX idx_carrito_estado ON Carrito(estado_carrito);
CREATE INDEX idx_carrito_fecha_expiracion ON Carrito(fecha_expiracion);
CREATE INDEX idx_carrito_item_carrito ON Carrito_Item(id_carrito);
CREATE INDEX idx_carrito_item_servicio ON Carrito_Item(id_servicio_ext);
CREATE INDEX idx_carrito_servicio_adicional_item ON Carrito_Servicio_Adicional(id_carrito_item);