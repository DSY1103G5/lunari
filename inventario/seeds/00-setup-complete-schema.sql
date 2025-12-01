-- ============================================
-- LUNARi Inventory Database Setup - COMPLETE
-- ============================================
-- This script creates a fresh schema for the gaming products catalog
-- Run this on your NeonDB PostgreSQL database

-- ============================================
-- 1. DROP OLD TABLES (if they exist)
-- ============================================

DROP TABLE IF EXISTS producto CASCADE;
DROP TABLE IF EXISTS categoria CASCADE;

-- Also drop old service catalog tables if they exist
DROP TABLE IF EXISTS servicio_servicio_adicional CASCADE;
DROP TABLE IF EXISTS paquete_recurso_servicio CASCADE;
DROP TABLE IF EXISTS catalogo CASCADE;
DROP TABLE IF EXISTS tipo_recurso CASCADE;
DROP TABLE IF EXISTS servicio_adicional CASCADE;

-- ============================================
-- 2. CREATE NEW TABLES
-- ============================================

-- Categories table (using String IDs for category codes)
CREATE TABLE categoria (
    id_categoria VARCHAR(10) PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Products table with JSONB support for specs and tags
CREATE TABLE producto (
    id_producto SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    categoria_id VARCHAR(10) NOT NULL,
    precio_clp INTEGER NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    marca VARCHAR(100),
    rating DECIMAL(2,1),
    specs JSONB,
    descripcion TEXT,
    tags JSONB,
    imagen VARCHAR(500),
    is_activo BOOLEAN DEFAULT TRUE,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id_categoria)
);

-- ============================================
-- 3. CREATE INDEXES
-- ============================================

CREATE INDEX idx_producto_categoria ON producto(categoria_id);
CREATE INDEX idx_producto_code ON producto(code);
CREATE INDEX idx_producto_activo ON producto(is_activo);
CREATE INDEX idx_producto_marca ON producto(marca);

-- GIN index for JSONB columns (for better performance on tag queries)
CREATE INDEX idx_producto_tags ON producto USING GIN (tags);
CREATE INDEX idx_producto_specs ON producto USING GIN (specs);

-- ============================================
-- 4. INSERT CATEGORIES (10 gaming categories)
-- ============================================

INSERT INTO categoria (id_categoria, nombre_categoria, descripcion, creado_el, actualizado_el) VALUES
('JM', 'Juegos de Mesa', 'Juegos de mesa estratégicos y familiares', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('AC', 'Accesorios', 'Controles, auriculares y periféricos gaming', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CO', 'Consolas', 'Consolas de videojuegos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CG', 'Computadoras Gamer', 'PCs gaming de alto rendimiento', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SG', 'Sillas Gamer', 'Sillas ergonómicas para gaming', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MS', 'Mouse', 'Ratones gaming de precisión', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MP', 'Mousepad', 'Superficies para gaming', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('PP', 'Poleras', 'Ropa gamer - Camisetas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('PG', 'Polerones', 'Ropa gamer - Hoodies', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ST', 'Servicio Técnico', 'Servicios de mantenimiento y reparación', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================
-- 5. INSERT PRODUCTS (47 gaming products)
-- ============================================

-- Juegos de Mesa (8 products)
INSERT INTO producto (code, nombre, categoria_id, precio_clp, stock, marca, rating, specs, descripcion, tags, imagen, is_activo) VALUES
('JM001', 'Catan', 'JM', 29990, 25, 'Kosmos', 4.8, '["3-4 jugadores", "60-90 min"]'::jsonb, 'Clásico de estrategia de colonización y comercio.', '["familiar", "estrategia"]'::jsonb, '/assets/images/products/jm001.webp', true),
('JM002', 'Carcassonne', 'JM', 24990, 30, 'Z-Man Games', 4.7, '["2-5 jugadores", "35 min"]'::jsonb, 'Construcción de losetas en la campiña medieval.', '["familiar", "colocación"]'::jsonb, '/assets/images/products/jm002.webp', true),
('JM003', 'Wingspan', 'JM', 35990, 15, 'Stonemaier Games', 4.9, '["1-5 jugadores", "60-90 min"]'::jsonb, 'Juego de estrategia sobre aves y sus hábitats.', '["estrategia", "naturaleza"]'::jsonb, '/assets/images/products/jm003.webp', true),
('JM004', 'Azul', 'JM', 27990, 22, 'Plan B Games', 4.6, '["2-4 jugadores", "45 min"]'::jsonb, 'Hermoso juego de colocación de azulejos.', '["familiar", "abstracto"]'::jsonb, '/assets/images/products/jm004.webp', true),
('JM005', 'Ticket to Ride', 'JM', 32990, 18, 'Days of Wonder', 4.7, '["2-5 jugadores", "60 min"]'::jsonb, 'Aventura ferroviaria por todo el continente.', '["familiar", "rutas"]'::jsonb, '/assets/images/products/jm005.webp', true),
('JM006', 'Splendor', 'JM', 21990, 28, 'Space Cowboys', 4.4, '["2-4 jugadores", "30 min"]'::jsonb, 'Comercio de gemas en el Renacimiento.', '["comercio", "motor"]'::jsonb, '/assets/images/products/jm006.webp', true),
('JM007', '7 Wonders', 'JM', 33990, 16, 'Repos Production', 4.5, '["3-7 jugadores", "30 min"]'::jsonb, 'Construye tu civilización antigua.', '["civilización", "drafting"]'::jsonb, '/assets/images/products/jm007.webp', true),
('JM008', 'Pandemic', 'JM', 28990, 19, 'Z-Man Games', 4.6, '["2-4 jugadores", "45 min"]'::jsonb, 'Coopera para salvar el mundo de las enfermedades.', '["cooperativo", "estrategia"]'::jsonb, '/assets/images/products/jm008.webp', true);

-- Accesorios (7 products)
INSERT INTO producto (code, nombre, categoria_id, precio_clp, stock, marca, rating, specs, descripcion, tags, imagen, is_activo) VALUES
('AC001', 'Control Xbox Inalámbrico', 'AC', 59990, 18, 'Microsoft', 4.6, '["Bluetooth", "USB-C", "Xbox/PC"]'::jsonb, 'Comodidad mejorada con agarre texturizado y baja latencia.', '["xbox", "pc"]'::jsonb, '/assets/images/products/ac001.webp', true),
('AC002', 'HyperX Cloud II', 'AC', 79990, 14, 'HyperX', 4.7, '["Sonido 7.1", "Micrófono desmontable"]'::jsonb, 'Sonido envolvente y comodidad para largas sesiones.', '["audio", "microfono"]'::jsonb, '/assets/images/products/ac002.webp', true),
('AC003', 'Control PlayStation DualSense', 'AC', 69990, 12, 'Sony', 4.8, '["Haptic feedback", "Adaptive triggers", "PS5"]'::jsonb, 'Control de nueva generación con retroalimentación háptica.', '["ps5", "controller"]'::jsonb, '/assets/images/products/ac003.webp', true),
('AC004', 'Teclado Mecánico RGB', 'AC', 89990, 8, 'Corsair', 4.5, '["Cherry MX Red", "RGB", "Tenkeyless"]'::jsonb, 'Teclado mecánico de alto rendimiento para gaming.', '["mechanical", "rgb"]'::jsonb, '/assets/images/products/ac004.webp', true),
('AC005', 'Webcam HD 1080p', 'AC', 45990, 25, 'Logitech', 4.4, '["1080p", "Micrófono integrado", "USB"]'::jsonb, 'Perfecta para streaming y videollamadas.', '["streaming", "camera"]'::jsonb, '/assets/images/products/ac005.webp', true),
('AC006', 'Monitor Gaming 144Hz', 'AC', 299990, 7, 'ASUS', 4.7, '["24\"", "144Hz", "1ms", "G-Sync"]'::jsonb, 'Experiencia visual fluida para gaming competitivo.', '["monitor", "144hz"]'::jsonb, '/assets/images/products/ac006.webp', true),
('AC007', 'Micrófono USB Streaming', 'AC', 75990, 14, 'Blue Yeti', 4.8, '["USB", "Cardioide", "Monitoreo"]'::jsonb, 'Calidad profesional para streamers.', '["streaming", "usb"]'::jsonb, '/assets/images/products/ac007.webp', true);

-- Consolas (4 products)
INSERT INTO producto (code, nombre, categoria_id, precio_clp, stock, marca, rating, specs, descripcion, tags, imagen, is_activo) VALUES
('CO001', 'PlayStation 5', 'CO', 549990, 8, 'Sony', 4.9, '["SSD ultra-rápido", "Ray Tracing"]'::jsonb, 'Consola de nueva generación con tiempos de carga mínimos.', '["ps5", "next-gen"]'::jsonb, '/assets/images/products/co001.webp', true),
('CO002', 'Nintendo Switch OLED', 'CO', 389990, 6, 'Nintendo', 4.8, '["Pantalla OLED 7\"", "Dock incluido", "64GB"]'::jsonb, 'Consola híbrida con pantalla OLED mejorada.', '["portable", "dock"]'::jsonb, '/assets/images/products/co002.webp', true),
('CO003', 'PlayStation 5 Digital', 'CO', 499990, 3, 'Sony', 4.9, '["Sin unidad de disco", "Ultra HD", "SSD"]'::jsonb, 'La experiencia de nueva generación completamente digital.', '["digital", "nextgen"]'::jsonb, '/assets/images/products/co003.webp', true),
('CO004', 'Xbox Series S', 'CO', 299990, 8, 'Microsoft', 4.5, '["Digital", "4K upscaling", "512GB SSD"]'::jsonb, 'Gaming de nueva generación más compacto.', '["compact", "digital"]'::jsonb, '/assets/images/products/co004.webp', true);

-- Computadoras Gamer (4 products)
INSERT INTO producto (code, nombre, categoria_id, precio_clp, stock, marca, rating, specs, descripcion, tags, imagen, is_activo) VALUES
('CG001', 'PC Gamer ASUS ROG Strix', 'CG', 1299990, 5, 'ASUS', 4.8, '["CPU Ryzen", "GPU RTX", "16GB RAM"]'::jsonb, 'Rendimiento tope para juegos exigentes.', '["asus", "rtx"]'::jsonb, '/assets/images/products/cg001.webp', true),
('CG002', 'PC Gamer Pro RTX 4070', 'CG', 1299990, 4, 'ASUS', 4.7, '["RTX 4070", "32GB RAM", "1TB SSD"]'::jsonb, 'Potencia extrema para los gamers más exigentes.', '["rtx", "high-end"]'::jsonb, '/assets/images/products/cg002.webp', true),
('CG003', 'PC Gamer Budget GTX 1660', 'CG', 699990, 8, 'MSI', 4.3, '["GTX 1660", "16GB RAM", "500GB SSD"]'::jsonb, 'Excelente rendimiento a precio accesible.', '["budget", "gaming"]'::jsonb, '/assets/images/products/cg003.webp', true),
('CG004', 'Laptop Gaming ROG', 'CG', 899990, 5, 'ASUS', 4.6, '["RTX 3060", "15.6\"", "144Hz", "16GB RAM"]'::jsonb, 'Gaming portátil de alto rendimiento.', '["laptop", "portable"]'::jsonb, '/assets/images/products/cg004.webp', true);

-- Sillas Gamer (3 products)
INSERT INTO producto (code, nombre, categoria_id, precio_clp, stock, marca, rating, specs, descripcion, tags, imagen, is_activo) VALUES
('SG001', 'Silla Gamer Secretlab Titan', 'SG', 349990, 9, 'Secretlab', 4.8, '["Soporte lumbar", "Ajustes 4D"]'::jsonb, 'Confort ergonómico para sesiones prolongadas.', '["ergonomia"]'::jsonb, '/assets/images/products/sg001.webp', true),
('SG002', 'Silla Gamer Profesional', 'SG', 199990, 12, 'DXRacer', 4.6, '["Cuero PU", "Reclinable 180°", "Apoyabrazos 4D"]'::jsonb, 'Comodidad premium para sesiones de gaming extendidas.', '["profesional", "reclinable"]'::jsonb, '/assets/images/products/sg002.webp', true),
('SG003', 'Silla Gamer Económica', 'SG', 89990, 20, 'Cougar', 4.2, '["Tela mesh", "Altura ajustable", "Apoyabrazos fijos"]'::jsonb, 'Buena calidad a precio accesible.', '["economica", "mesh"]'::jsonb, '/assets/images/products/sg003.webp', true);

-- Mouse (4 products)
INSERT INTO producto (code, nombre, categoria_id, precio_clp, stock, marca, rating, specs, descripcion, tags, imagen, is_activo) VALUES
('MS001', 'Logitech G502 HERO', 'MS', 49990, 20, 'Logitech', 4.7, '["Sensor 25K", "11 botones programables"]'::jsonb, 'Precisión y personalización para FPS y MOBA.', '["logitech", "fps"]'::jsonb, '/assets/images/products/ms001.webp', true),
('MS002', 'Mouse Gaming RGB Wireless', 'MS', 65990, 15, 'Razer', 4.6, '["20000 DPI", "RGB", "Inalámbrico"]'::jsonb, 'Precisión extrema sin cables.', '["wireless", "rgb"]'::jsonb, '/assets/images/products/ms002.webp', true),
('MS003', 'Mouse Vertical Ergonómico', 'MS', 39990, 18, 'Logitech', 4.1, '["Diseño vertical", "6 botones", "USB"]'::jsonb, 'Reduce la tensión en la muñeca.', '["ergonomico", "vertical"]'::jsonb, '/assets/images/products/ms003.webp', true),
('MS004', 'Mouse Gamer Ultraligero', 'MS', 89990, 11, 'Finalmouse', 4.8, '["58g", "25600 DPI", "Paracord"]'::jsonb, 'El mouse más ligero para gaming competitivo.', '["ultralight", "pro"]'::jsonb, '/assets/images/products/ms004.webp', true);

-- Mousepad (3 products)
INSERT INTO producto (code, nombre, categoria_id, precio_clp, stock, marca, rating, specs, descripcion, tags, imagen, is_activo) VALUES
('MP001', 'Razer Goliathus Extended', 'MP', 29990, 22, 'Razer', 4.6, '["Superficie textil", "RGB"]'::jsonb, 'Gran área de deslizamiento con iluminación personalizable.', '["razer", "rgb"]'::jsonb, '/assets/images/products/mp001.webp', true),
('MP002', 'Mousepad Gaming XXL', 'MP', 24990, 30, 'SteelSeries', 4.5, '["90x40cm", "Superficie de control", "Base de goma"]'::jsonb, 'Máximo espacio para tu setup gaming.', '["xxl", "control"]'::jsonb, '/assets/images/products/mp002.webp', true),
('MP003', 'Mousepad RGB', 'MP', 34990, 22, 'Corsair', 4.4, '["Iluminación RGB", "35x25cm", "USB"]'::jsonb, 'Añade estilo luminoso a tu escritorio.', '["rgb", "illuminated"]'::jsonb, '/assets/images/products/mp003.webp', true);

-- Poleras (3 products)
INSERT INTO producto (code, nombre, categoria_id, precio_clp, stock, marca, rating, specs, descripcion, tags, imagen, is_activo) VALUES
('PP001', 'Polera Gamer Personalizada ''Level-Up''', 'PP', 14990, 40, 'Level-Up', 4.5, '["Personalización gamer tag"]'::jsonb, 'Camiseta cómoda con diseño personalizado.', '["merch"]'::jsonb, '/assets/images/products/pp001.png', true),
('PP002', 'Polera Retro Gaming', 'PP', 16990, 35, 'Pixel Tees', 4.3, '["100% Algodón", "Diseño retro", "Unisex"]'::jsonb, 'Diseños clásicos de videojuegos retro.', '["retro", "pixel"]'::jsonb, '/assets/images/products/pp002.webp', true),
('PP003', 'Polera E-Sports Team', 'PP', 19990, 28, 'Pro Gaming', 4.6, '["Dry-fit", "Logo bordado", "Slim fit"]'::jsonb, 'Representa a tu equipo favorito de e-sports.', '["esports", "team"]'::jsonb, '/assets/images/products/pp003.webp', true);

-- Polerones (1 product)
INSERT INTO producto (code, nombre, categoria_id, precio_clp, stock, marca, rating, specs, descripcion, tags, imagen, is_activo) VALUES
('PG001', 'Polerón Gaming Hoodie', 'PG', 29990, 25, 'Game Wear', 4.7, '["80% Algodón", "Capucha ajustable", "Bolsillo canguro"]'::jsonb, 'Comodidad y estilo para gamers.', '["hoodie", "comfort"]'::jsonb, '/assets/images/products/pg001.webp', true);

-- Servicio Técnico (2 products)
INSERT INTO producto (code, nombre, categoria_id, precio_clp, stock, marca, rating, specs, descripcion, tags, imagen, is_activo) VALUES
('ST001', 'Servicio Técnico Básico', 'ST', 19990, 100, 'TechFix', 4.3, '["Diagnóstico", "Limpieza", "Optimización"]'::jsonb, 'Mantenimiento básico para tu equipo.', '["servicio", "mantenimiento"]'::jsonb, '/assets/images/products/st001.webp', true),
('ST002', 'Servicio Técnico Avanzado', 'ST', 39990, 50, 'TechFix', 4.6, '["Reparación", "Upgrade", "Garantía 3 meses"]'::jsonb, 'Reparaciones complejas y mejoras de hardware.', '["reparacion", "upgrade"]'::jsonb, '/assets/images/products/st002.webp', true);

-- ============================================
-- 6. VERIFY DATA
-- ============================================

-- Count categories (should be 10)
SELECT 'Categories loaded:' as info, COUNT(*) as count FROM categoria;

-- Count products (should be 47)
SELECT 'Products loaded:' as info, COUNT(*) as count FROM producto;

-- Show products by category
SELECT
    c.id_categoria,
    c.nombre_categoria,
    COUNT(p.id_producto) as product_count
FROM categoria c
LEFT JOIN producto p ON c.id_categoria = p.categoria_id
GROUP BY c.id_categoria, c.nombre_categoria
ORDER BY c.id_categoria;

-- ============================================
-- SETUP COMPLETE! 🎮
-- ============================================
-- You now have:
-- ✅ 2 tables created (categoria, producto)
-- ✅ 6 indexes created for performance
-- ✅ 10 categories loaded
-- ✅ 47 gaming products loaded
-- ✅ JSONB support enabled for specs and tags
-- ============================================
