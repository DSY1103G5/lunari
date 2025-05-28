-- ==================== ROLES Y PERMISOS ====================

INSERT INTO Rol (nombre_rol, descripcion) VALUES
('ADMINISTRADOR', 'Administrador del sistema con acceso completo a todas las funcionalidades'),
('PRODUCT_OWNER', 'Propietario del producto con permisos de gestión de proyectos y equipos'),
('CLIENTE', 'Cliente con acceso limitado a consultas y reportes de sus proyectos'),
('DEVOP', 'Desarrollador con permisos técnicos para despliegue y configuración');

INSERT INTO Permiso (nombre_permiso, descripcion) VALUES
('CREAR_USUARIO', 'Crear nuevos usuarios en el sistema'),
('EDITAR_USUARIO', 'Modificar información de usuarios existentes'),
('ELIMINAR_USUARIO', 'Eliminar usuarios del sistema'),
('VER_USUARIOS', 'Visualizar lista y detalles de usuarios'),
('CREAR_EMPRESA', 'Crear nuevas empresas cliente'),
('EDITAR_EMPRESA', 'Modificar información de empresas'),
('ELIMINAR_EMPRESA', 'Eliminar empresas del sistema'),
('VER_EMPRESAS', 'Visualizar lista y detalles de empresas'),
('CREAR_PROYECTO', 'Crear nuevos proyectos'),
('EDITAR_PROYECTO', 'Modificar proyectos existentes'),
('ELIMINAR_PROYECTO', 'Eliminar proyectos'),
('VER_PROYECTOS', 'Visualizar proyectos'),
('ASIGNAR_EQUIPO', 'Asignar miembros a equipos de proyecto'),
('CONFIGURAR_SISTEMA', 'Acceso a configuraciones generales del sistema'),
('VER_LOGS', 'Visualizar logs del sistema'),
('GESTIONAR_ROLES', 'Crear, editar y eliminar roles'),
('GESTIONAR_PERMISOS', 'Asignar y desasignar permisos'),
('VER_REPORTES', 'Acceso a reportes y estadísticas'),
('EXPORTAR_DATOS', 'Exportar información del sistema'),
('ACCESO_BD', 'Acceso directo a base de datos'),
('DESPLEGAR_APLICACION', 'Realizar despliegues de aplicaciones'),
('CONFIGURAR_SERVIDOR', 'Configurar servidores y infraestructura');

INSERT INTO Rol_Permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso 
FROM Rol r, Permiso p 
WHERE r.nombre_rol = 'ADMINISTRADOR';

INSERT INTO Rol_Permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso 
FROM Rol r, Permiso p 
WHERE r.nombre_rol = 'PRODUCT_OWNER' 
AND p.nombre_permiso IN (
    'VER_USUARIOS',
    'CREAR_PROYECTO',
    'EDITAR_PROYECTO',
    'VER_PROYECTOS',
    'ASIGNAR_EQUIPO',
    'VER_EMPRESAS',
    'VER_REPORTES',
    'EXPORTAR_DATOS'
);

INSERT INTO Rol_Permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso 
FROM Rol r, Permiso p 
WHERE r.nombre_rol = 'CLIENTE' 
AND p.nombre_permiso IN (
    'VER_PROYECTOS',
    'VER_REPORTES'
);

INSERT INTO Rol_Permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso 
FROM Rol r, Permiso p 
WHERE r.nombre_rol = 'DEVOP' 
AND p.nombre_permiso IN (
    'VER_PROYECTOS',
    'EDITAR_PROYECTO',
    'VER_LOGS',
    'ACCESO_BD',
    'DESPLEGAR_APLICACION',
    'CONFIGURAR_SERVIDOR',
    'VER_REPORTES'
);

-- ==================== EMPRESAS CLIENTE ====================

-- Empresas que contratan servicios de desarrollo web
INSERT INTO Empresa_Cliente (
    id_empresa,
    nombre_empresa,
    razon_social,
    industria,
    sitio_web,
    creado_el,
    actualizado_el
) VALUES 
(
    '550e8400-e29b-41d4-a716-446655440100',
    'TechStart Chile',
    'TechStart Chile SpA',
    'Tecnología',
    'https://techstart.cl',
    CURRENT_TIMESTAMP - INTERVAL '6 months',
    CURRENT_TIMESTAMP - INTERVAL '3 months'
),
(
    '550e8400-e29b-41d4-a716-446655440101',
    'Comercial La Esquina',
    'Comercial La Esquina Limitada',
    'Retail',
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '4 months',
    CURRENT_TIMESTAMP - INTERVAL '1 month'
),
(
    '550e8400-e29b-41d4-a716-446655440102',
    'Consultora Legal Valdés',
    'Consultora Legal Valdés y Asociados Ltda',
    'Servicios Legales',
    'https://valdeslaw.cl',
    CURRENT_TIMESTAMP - INTERVAL '8 months',
    CURRENT_TIMESTAMP - INTERVAL '2 weeks'
),
(
    '550e8400-e29b-41d4-a716-446655440103',
    'Restaurant El Buen Sabor',
    'Restaurant El Buen Sabor SPA',
    'Gastronomía',
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '2 months',
    CURRENT_TIMESTAMP - INTERVAL '1 week'
),
(
    '550e8400-e29b-41d4-a716-446655440104',
    'Clínica Dental Sonrisa',
    'Clínica Dental Sonrisa Limitada',
    'Salud',
    'https://clinicasonrisa.cl',
    CURRENT_TIMESTAMP - INTERVAL '5 months',
    CURRENT_TIMESTAMP - INTERVAL '2 months'
);

-- ==================== USUARIOS ====================

-- ADMINISTRADORES DEL SISTEMA
INSERT INTO Usuario (
    id_usuario,
    nombre_usuario,
    apellido_usuario,
    email,
    contrasena,
    telefono,
    imagen_perfil,
    id_rol,
    id_empresa,
    activo,
    ultimo_login,
    verificado,
    token_verificacion,
    token_expiracion,
    creado_el,
    actualizado_el
) VALUES 
(
    '550e8400-e29b-41d4-a716-446655440001',
    'Carlos',
    'Rodriguez',
    'carlos.rodriguez@lunari.cl',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z',
    '+56912345678',
    'https://api.lunari.cl/avatars/carlos.jpg',
    1, -- ADMINISTRADOR
    NULL,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    TRUE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '1 year',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
),
(
    '550e8400-e29b-41d4-a716-446655440002',
    'María',
    'González',
    'maria.gonzalez@lunari.cl',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z',
    '+56987654321',
    'https://api.lunari.cl/avatars/maria.jpg',
    1, -- ADMINISTRADOR
    NULL,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '5 hours',
    TRUE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '10 months',
    CURRENT_TIMESTAMP - INTERVAL '5 hours'
);

-- PRODUCT OWNERS
INSERT INTO Usuario (
    id_usuario,
    nombre_usuario,
    apellido_usuario,
    email,
    contrasena,
    telefono,
    imagen_perfil,
    id_rol,
    id_empresa,
    activo,
    ultimo_login,
    verificado,
    token_verificacion,
    token_expiracion,
    creado_el,
    actualizado_el
) VALUES 
(
    '550e8400-e29b-41d4-a716-446655440003',
    'Luis',
    'Fernández',
    'luis.fernandez@lunari.cl',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z',
    '+56911223344',
    'https://api.lunari.cl/avatars/luis.jpg',
    2, -- PRODUCT_OWNER
    NULL,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    TRUE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '8 months',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
),
(
    '550e8400-e29b-41d4-a716-446655440004',
    'Ana',
    'Morales',
    'ana.morales@lunari.cl',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z',
    '+56955667788',
    'https://api.lunari.cl/avatars/ana.jpg',
    2, -- PRODUCT_OWNER
    NULL,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '3 hours',
    TRUE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '6 months',
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
);

S (Usuarios que contratan servicios)
INSERT INTO Usuario (
    id_usuario,
    nombre_usuario,
    apellido_usuario,
    email,
    contrasena,
    telefono,
    imagen_perfil,
    id_rol,
    id_empresa,
    activo,
    ultimo_login,
    verificado,
    token_verificacion,
    token_expiracion,
    creado_el,
    actualizado_el
) VALUES 
(
    '550e8400-e29b-41d4-a716-446655440005',
    'Roberto',
    'Silva',
    'roberto.silva@techstart.cl',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z',
    '+56933445566',
    NULL,
    3, 
    '550e8400-e29b-41d4-a716-446655440100',
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '6 hours',
    TRUE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '6 months',
    CURRENT_TIMESTAMP - INTERVAL '6 hours'
),
(
    '550e8400-e29b-41d4-a716-446655440006',
    'Carmen',
    'Pérez',
    'carmen.perez@comerciallaesquina.cl',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z',
    '+56977889900',
    NULL,
    3, 
    '550e8400-e29b-41d4-a716-446655440101',
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    TRUE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '4 months',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    '550e8400-e29b-41d4-a716-446655440007',
    'Jorge',
    'Valdés',
    'jorge.valdes@valdeslaw.cl',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z',
    '+56944556677',
    'https://valdeslaw.cl/team/jorge.jpg',
    3, 
    '550e8400-e29b-41d4-a716-446655440102',
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '1 week',
    TRUE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '8 months',
    CURRENT_TIMESTAMP - INTERVAL '1 week'
),
(
    '550e8400-e29b-41d4-a716-446655440008',
    'Patricia',
    'Moreno',
    'patricia.moreno@buensabor.cl',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z',
    '+56966778899',
    NULL,
    3, 
    '550e8400-e29b-41d4-a716-446655440103',
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '12 hours',
    TRUE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '2 months',
    CURRENT_TIMESTAMP - INTERVAL '12 hours'
),
(
    '550e8400-e29b-41d4-a716-446655440009',
    'Dr. Eduardo',
    'Ramírez',
    'eduardo.ramirez@clinicasonrisa.cl',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z',
    '+56988990011',
    'https://clinicasonrisa.cl/staff/eduardo.jpg',
    3, 
    '550e8400-e29b-41d4-a716-446655440104', 
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '4 days',
    TRUE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '5 months',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
);

-- DESARROLLADORES (DevOps)
INSERT INTO Usuario (
    id_usuario,
    nombre_usuario,
    apellido_usuario,
    email,
    contrasena,
    telefono,
    imagen_perfil,
    id_rol,
    id_empresa,
    activo,
    ultimo_login,
    verificado,
    token_verificacion,
    token_expiracion,
    creado_el,
    actualizado_el
) VALUES 
(
    '550e8400-e29b-41d4-a716-446655440010',
    'Diego',
    'Herrera',
    'diego.herrera@lunari.cl',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z',
    '+56922334455',
    'https://api.lunari.cl/avatars/diego.jpg',
    4, 
    NULL,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '30 minutes',
    TRUE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '7 months',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes'
),
(
    '550e8400-e29b-41d4-a716-446655440011',
    'Valentina',
    'Castro',
    'valentina.castro@lunari.cl',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z', 
    '+56955443322',
    'https://api.lunari.cl/avatars/valentina.jpg',
    4,
    NULL,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    TRUE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '5 months',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
),
(
    '550e8400-e29b-41d4-a716-446655440012',
    'Sebastián',
    'Ortega',
    'sebastian.ortega@lunari.cl',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z', 
    '+56966554433',
    NULL,
    4, 
    NULL,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    TRUE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '4 months',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
);

-- USUARIOS ADICIONALES PARA PRUEBAS (sin empresa asignada)
INSERT INTO Usuario (
    id_usuario,
    nombre_usuario,
    apellido_usuario,
    email,
    contrasena,
    telefono,
    imagen_perfil,
    id_rol,
    id_empresa,
    activo,
    ultimo_login,
    verificado,
    token_verificacion,
    token_expiracion,
    creado_el,
    actualizado_el
) VALUES 
(
    '550e8400-e29b-41d4-a716-446655440013',
    'Camila',
    'Torres',
    'camila.torres@gmail.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z',
    '+56911998877',
    NULL,
    3,
    NULL,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '1 week',
    FALSE,
    'VERIFY_TOKEN_001',
    CURRENT_TIMESTAMP + INTERVAL '24 hours',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    '550e8400-e29b-41d4-a716-446655440014',
    'Ricardo',
    'Vega',
    'ricardo.vega@hotmail.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Uo76h3QP6.c2FJV2Hf8Jz7Qz7Z8Q7Z',
    '+56977665544',
    NULL,
    3, 
    NULL,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '2 months',
    TRUE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '3 months',
    CURRENT_TIMESTAMP - INTERVAL '2 months'
);

-- ==================== ACTUALIZAR CONTACTOS DE EMPRESAS ====================

-- Asignar usuarios de contacto a las empresas
UPDATE Empresa_Cliente 
SET id_usuario_contacto = '550e8400-e29b-41d4-a716-446655440005' 
WHERE id_empresa = '550e8400-e29b-41d4-a716-446655440100';

UPDATE Empresa_Cliente 
SET id_usuario_contacto = '550e8400-e29b-41d4-a716-446655440006' 
WHERE id_empresa = '550e8400-e29b-41d4-a716-446655440101';

UPDATE Empresa_Cliente 
SET id_usuario_contacto = '550e8400-e29b-41d4-a716-446655440007' 
WHERE id_empresa = '550e8400-e29b-41d4-a716-446655440102';

UPDATE Empresa_Cliente 
SET id_usuario_contacto = '550e8400-e29b-41d4-a716-446655440008' 
WHERE id_empresa = '550e8400-e29b-41d4-a716-446655440103';

UPDATE Empresa_Cliente 
SET id_usuario_contacto = '550e8400-e29b-41d4-a716-446655440009' 
WHERE id_empresa = '550e8400-e29b-41d4-a716-446655440104';

-- ==================== MEMBRESÍAS DE EQUIPO (Proyectos ficticios) ====================

-- Simulando que algunos usuarios están asignados a proyectos
-- Estos UUIDs de proyecto son ficticios, en un sistema real vendrían de otra tabla/microservicio

INSERT INTO Membresia_Equipo (
    id_membresia,
    id_proyecto_ext,
    id_usuario,
    rol_proyecto,
    creado_el
) VALUES 
(
    '660e8400-e29b-41d4-a716-446655440001',
    '770e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440003',
    'Project Manager',
    CURRENT_TIMESTAMP - INTERVAL '5 months'
),
(
    '660e8400-e29b-41d4-a716-446655440002',
    '770e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440010',
    'Lead Developer',
    CURRENT_TIMESTAMP - INTERVAL '5 months'
),
(
    '660e8400-e29b-41d4-a716-446655440003',
    '770e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440011',
    'Frontend Developer',
    CURRENT_TIMESTAMP - INTERVAL '4 months'
),

(
    '660e8400-e29b-41d4-a716-446655440004',
    '770e8400-e29b-41d4-a716-446655440002',
    '550e8400-e29b-41d4-a716-446655440004',
    'Project Manager',
    CURRENT_TIMESTAMP - INTERVAL '3 months'
),
(
    '660e8400-e29b-41d4-a716-446655440005',
    '770e8400-e29b-41d4-a716-446655440002',
    '550e8400-e29b-41d4-a716-446655440012',
    'Backend Developer',
    CURRENT_TIMESTAMP - INTERVAL '3 months'
),
(
    '660e8400-e29b-41d4-a716-446655440006',
    '770e8400-e29b-41d4-a716-446655440002',
    '550e8400-e29b-41d4-a716-446655440010',
    'DevOps Engineer',
    CURRENT_TIMESTAMP - INTERVAL '2 months'
),

(
    '660e8400-e29b-41d4-a716-446655440007',
    '770e8400-e29b-41d4-a716-446655440003',
    '550e8400-e29b-41d4-a716-446655440003',
    'Project Manager',
    CURRENT_TIMESTAMP - INTERVAL '7 months'
),
(
    '660e8400-e29b-41d4-a716-446655440008',
    '770e8400-e29b-41d4-a716-446655440003',
    '550e8400-e29b-41d4-a716-446655440011',
    'Fullstack Developer',
    CURRENT_TIMESTAMP - INTERVAL '6 months'
);