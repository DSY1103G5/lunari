-- ==================== CATEGORÍAS DE SERVICIOS ====================

INSERT INTO Categoria (nombre_categoria, descripcion) VALUES
('Desarrollo Web', 'Servicios de desarrollo y creación de sitios web desde landing pages hasta e-commerce completos'),
('Diseño UI/UX', 'Servicios de diseño de interfaz de usuario y experiencia de usuario para sitios web'),
('Marketing Digital', 'Servicios de posicionamiento SEO, SEM y estrategias de marketing digital'),
('Hosting y Dominio', 'Servicios de alojamiento web, registro de dominios y configuración de servidores'),
('Mantenimiento Web', 'Servicios de mantenimiento, actualizaciones y soporte técnico de sitios web'),
('E-commerce', 'Servicios especializados en tiendas online y comercio electrónico'),
('Consultoría Digital', 'Servicios de consultoría y estrategia digital para empresas');

-- ==================== CATÁLOGO DE SERVICIOS ====================

-- Servicios de Desarrollo Web
INSERT INTO Catalogo (id_categoria, nombre_servicio, descripcion, precio_base, is_activo, duracion_estimada_dias) VALUES
(1, 'Landing Page Básica', 'Página web de una sola página optimizada para conversión con diseño responsive', 150000.00, TRUE, 7),
(1, 'Sitio Web Corporativo', 'Sitio web profesional de hasta 5 páginas con diseño personalizado y CMS', 350000.00, TRUE, 14),
(1, 'Portal Web Avanzado', 'Sitio web complejo con funcionalidades avanzadas, panel de administración y múltiples secciones', 750000.00, TRUE, 30),
(1, 'Aplicación Web', 'Desarrollo de aplicación web con funcionalidades específicas y base de datos', 1200000.00, TRUE, 45),
(1, 'Blog Profesional', 'Blog con sistema de gestión de contenidos, categorías y optimización SEO', 250000.00, TRUE, 10);

-- Servicios de Diseño UI/UX
INSERT INTO Catalogo (id_categoria, nombre_servicio, descripcion, precio_base, is_activo, duracion_estimada_dias) VALUES
(2, 'Diseño de Interfaz Básico', 'Diseño visual de interfaz para sitio web básico con hasta 3 pantallas', 180000.00, TRUE, 5),
(2, 'Experiencia de Usuario Completa', 'Análisis UX completo con wireframes, prototipos y testing de usuario', 420000.00, TRUE, 15),
(2, 'Rediseño de Sitio Existente', 'Modernización del diseño de sitio web existente manteniendo la funcionalidad', 300000.00, TRUE, 12),
(2, 'Prototipo Interactivo', 'Creación de prototipo clickeable para validación de conceptos', 200000.00, TRUE, 8);

-- Servicios de Marketing Digital
INSERT INTO Catalogo (id_categoria, nombre_servicio, descripcion, precio_base, is_activo, duracion_estimada_dias) VALUES
(3, 'Optimización SEO Básica', 'Optimización on-page, meta tags, velocidad de carga y estructura básica SEO', 120000.00, TRUE, 5),
(3, 'Estrategia SEO Avanzada', 'Análisis de keywords, optimización técnica avanzada y estrategia de contenidos', 280000.00, TRUE, 20),
(3, 'Configuración Google Analytics', 'Instalación y configuración de herramientas de analítica web', 80000.00, TRUE, 2),
(3, 'Campaña SEM Inicial', 'Configuración inicial de campaña de Google Ads con keywords y landing page', 200000.00, TRUE, 7);

-- Servicios de Hosting y Dominio
INSERT INTO Catalogo (id_categoria, nombre_servicio, descripcion, precio_base, is_activo, duracion_estimada_dias) VALUES
(4, 'Registro de Dominio .cl', 'Registro de dominio nacional chileno por 1 año', 25000.00, TRUE, 1),
(4, 'Hosting Básico', 'Alojamiento web básico para sitios pequeños con 5GB de espacio', 45000.00, TRUE, 1),
(4, 'Hosting Premium', 'Alojamiento web premium con SSD, CDN y certificado SSL incluido', 95000.00, TRUE, 1),
(4, 'Configuración de Servidor', 'Configuración personalizada de servidor para aplicaciones específicas', 180000.00, TRUE, 3);

-- Servicios de E-commerce
INSERT INTO Catalogo (id_categoria, nombre_servicio, descripcion, precio_base, is_activo, duracion_estimada_dias) VALUES
(6, 'Tienda Online Básica', 'E-commerce básico con carrito de compras y pasarela de pago', 450000.00, TRUE, 21),
(6, 'E-commerce Avanzado', 'Tienda online completa con gestión de inventario, múltiples métodos de pago', 850000.00, TRUE, 35),
(6, 'Marketplace', 'Plataforma de marketplace con múltiples vendedores y comisiones', 1500000.00, TRUE, 60),
(6, 'Integración Pasarela de Pago', 'Configuración de WebPay, Mercado Pago u otras pasarelas chilenas', 120000.00, TRUE, 3);

-- Servicios de Mantenimiento
INSERT INTO Catalogo (id_categoria, nombre_servicio, descripcion, precio_base, is_activo, duracion_estimada_dias) VALUES
(5, 'Mantenimiento Mensual Básico', 'Actualizaciones, backups y monitoreo básico del sitio web', 35000.00, TRUE, 30),
(5, 'Soporte Técnico Premium', 'Soporte técnico 24/7 con tiempo de respuesta garantizado', 85000.00, TRUE, 30),
(5, 'Migración de Sitio Web', 'Migración completa de sitio web a nuevo hosting o dominio', 150000.00, TRUE, 5),
(5, 'Optimización de Velocidad', 'Mejora del rendimiento y velocidad de carga del sitio web', 180000.00, TRUE, 7);

-- Servicios de Consultoría
INSERT INTO Catalogo (id_categoria, nombre_servicio, descripcion, precio_base, is_activo, duracion_estimada_dias) VALUES
(7, 'Consultoría Digital Básica', 'Evaluación digital y recomendaciones estratégicas para presencia online', 120000.00, TRUE, 5),
(7, 'Auditoría Web Completa', 'Análisis técnico, SEO, UX y performance con informe detallado', 250000.00, TRUE, 10),
(7, 'Estrategia de Transformación Digital', 'Plan integral de digitalización para empresas tradicionales', 480000.00, TRUE, 20);

-- ==================== TIPOS DE RECURSOS ====================

INSERT INTO TipoRecurso (nombre_tipo_recurso, unidad_medida, tarifa_base_por_hora, descripcion) VALUES
('Desarrollador Frontend', 'Hora', 25000.00, 'Especialista en HTML, CSS, JavaScript y frameworks frontend'),
('Desarrollador Backend', 'Hora', 28000.00, 'Especialista en bases de datos, APIs y lógica de servidor'),
('Desarrollador Fullstack', 'Hora', 30000.00, 'Desarrollador con conocimientos tanto frontend como backend'),
('Diseñador UI/UX', 'Hora', 22000.00, 'Especialista en diseño de interfaces y experiencia de usuario'),
('Especialista SEO', 'Hora', 20000.00, 'Experto en optimización para motores de búsqueda'),
('Project Manager', 'Hora', 35000.00, 'Gestor de proyectos y coordinación de equipos'),
('DevOps Engineer', 'Hora', 32000.00, 'Especialista en infraestructura, despliegue y automatización'),
('Content Manager', 'Hora', 18000.00, 'Gestor de contenidos y redacción web'),
('QA Tester', 'Hora', 20000.00, 'Especialista en testing y control de calidad');

-- ==================== PAQUETES DE RECURSOS POR SERVICIO ====================

-- Landing Page Básica (Servicio ID 1)
INSERT INTO PaqueteRecursoServicio (id_servicio, id_tipo_recurso, cantidad_estimado, notas) VALUES
(1, 1, 12.0, 'Desarrollo frontend y maquetación responsive'),
(1, 4, 8.0, 'Diseño visual y optimización UX'),
(1, 5, 4.0, 'Optimización SEO básica'),
(1, 9, 3.0, 'Testing de funcionalidad y responsive');

-- Sitio Web Corporativo (Servicio ID 2)
INSERT INTO PaqueteRecursoServicio (id_servicio, id_tipo_recurso, cantidad_estimado, notas) VALUES
(2, 3, 25.0, 'Desarrollo fullstack con CMS'),
(2, 4, 15.0, 'Diseño completo de todas las páginas'),
(2, 5, 8.0, 'SEO técnico y on-page'),
(2, 6, 6.0, 'Gestión y coordinación del proyecto'),
(2, 8, 5.0, 'Creación y carga de contenidos'),
(2, 9, 6.0, 'Testing completo del sitio');

-- Portal Web Avanzado (Servicio ID 3)
INSERT INTO PaqueteRecursoServicio (id_servicio, id_tipo_recurso, cantidad_estimado, notas) VALUES
(3, 1, 35.0, 'Frontend avanzado con interactividad'),
(3, 2, 40.0, 'Backend complejo con base de datos'),
(3, 4, 25.0, 'Diseño UX/UI de sistema complejo'),
(3, 6, 15.0, 'Gestión de proyecto complejo'),
(3, 7, 8.0, 'Configuración de infraestructura'),
(3, 9, 12.0, 'Testing exhaustivo y QA');

-- E-commerce Básico (Servicio ID 17)
INSERT INTO PaqueteRecursoServicio (id_servicio, id_tipo_recurso, cantidad_estimado, notas) VALUES
(17, 1, 20.0, 'Frontend del e-commerce'),
(17, 2, 25.0, 'Backend con carrito y pagos'),
(17, 4, 12.0, 'Diseño de la tienda online'),
(17, 6, 8.0, 'Coordinación del proyecto'),
(17, 9, 10.0, 'Testing de compras y pagos');

-- ==================== SERVICIOS ADICIONALES ====================

INSERT INTO ServicioAdicional (nombre_adicional, descripcion, precio_adicional, is_activo) VALUES
('Certificado SSL Premium', 'Certificado de seguridad SSL de validación extendida', 45000.00, TRUE),
('CDN Global', 'Red de distribución de contenidos para mejorar velocidad mundial', 35000.00, TRUE),
('Backup Automático Diario', 'Sistema de respaldo automático diario con retención de 30 días', 25000.00, TRUE),
('Dominio .com Internacional', 'Registro de dominio internacional .com por 1 año', 18000.00, TRUE),
('Integración Google Workspace', 'Configuración de emails corporativos con Google Workspace', 50000.00, TRUE),
('Chatbot Básico', 'Bot de atención al cliente básico para sitio web', 120000.00, TRUE),
('Formulario Avanzado', 'Formularios complejos con validaciones y notificaciones', 35000.00, TRUE),
('Galería de Imágenes Avanzada', 'Galería interactiva con lightbox y categorización', 40000.00, TRUE),
('Integración Redes Sociales', 'Conexión automática con perfiles de redes sociales', 25000.00, TRUE),
('Mapa Interactivo', 'Mapa de Google Maps personalizado e interactivo', 30000.00, TRUE),
('Newsletter Sistema', 'Sistema de newsletters con gestión de suscriptores', 65000.00, TRUE),
('Reviews y Testimonios', 'Sistema de reseñas y testimonios de clientes', 45000.00, TRUE);

-- ==================== RELACIONES SERVICIO-SERVICIOS ADICIONALES ====================

-- Landing Page Básica puede tener estos adicionales
INSERT INTO ServicioServicioAdicional (id_servicio, id_servicio_adicional) VALUES
(1, 1), (1, 3), (1, 7), (1, 9), (1, 10);

-- Sitio Web Corporativo puede tener estos adicionales
INSERT INTO ServicioServicioAdicional (id_servicio, id_servicio_adicional) VALUES
(2, 1), (2, 2), (2, 3), (2, 5), (2, 7), (2, 8), (2, 9), (2, 10), (2, 11), (2, 12);

-- Portal Web Avanzado puede tener todos los adicionales
INSERT INTO ServicioServicioAdicional (id_servicio, id_servicio_adicional) VALUES
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 10), (3, 11), (3, 12);

-- E-commerce Básico adicionales específicos
INSERT INTO ServicioServicioAdicional (id_servicio, id_servicio_adicional) VALUES
(17, 1), (17, 2), (17, 3), (17, 5), (17, 6), (17, 12);

-- E-commerce Avanzado todos los adicionales
INSERT INTO ServicioServicioAdicional (id_servicio, id_servicio_adicional) VALUES
(18, 1), (18, 2), (18, 3), (18, 4), (18, 5), (18, 6), (18, 7), (18, 8), (18, 9), (18, 10), (18, 11), (18, 12);

-- Blog Profesional adicionales
INSERT INTO ServicioServicioAdicional (id_servicio, id_servicio_adicional) VALUES
(5, 1), (5, 3), (5, 9), (5, 11), (5, 12);

-- Servicios de Diseño
INSERT INTO ServicioServicioAdicional (id_servicio, id_servicio_adicional) VALUES
(6, 8), (6, 10);

-- Servicios de Marketing
INSERT INTO ServicioServicioAdicional (id_servicio, id_servicio_adicional) VALUES
(9, 9), (9, 11);

-- Servicios de Hosting
INSERT INTO ServicioServicioAdicional (id_servicio, id_servicio_adicional) VALUES
(13, 1), (13, 2), (13, 3),
(14, 1), (14, 2), (14, 3), (14, 5),
(15, 1), (15, 2), (15, 3), (15, 4), (15, 5);