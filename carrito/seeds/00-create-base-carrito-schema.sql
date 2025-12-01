-- =====================================================
-- LUNARi Carrito - Base Schema
-- Migration Script: Create base carrito tables
-- Version: 1.0
-- Date: 2025-11-30
-- =====================================================

-- Drop existing tables if they exist (for clean migration)
DROP TABLE IF EXISTS carrito_servicio_adicional CASCADE;
DROP TABLE IF EXISTS carrito_item CASCADE;
DROP TABLE IF EXISTS carrito CASCADE;

-- =====================================================
-- Table: carrito (Shopping Cart)
-- Description: Main cart entity tracking user shopping session
-- =====================================================
CREATE TABLE carrito (
    id_carrito UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario_ext UUID NOT NULL,
    estado_carrito VARCHAR(20) NOT NULL CHECK (estado_carrito IN (
        'ACTIVO',
        'PROCESADO',
        'ABANDONADO',
        'EXPIRADO'
    )),
    total_estimado DECIMAL(12,2) DEFAULT 0.00 CHECK (total_estimado >= 0),
    notas_cliente TEXT,
    fecha_expiracion TIMESTAMP WITH TIME ZONE,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Add comments
COMMENT ON TABLE carrito IS 'Shopping cart table - tracks user shopping sessions';
COMMENT ON COLUMN carrito.id_usuario_ext IS 'External reference to usuario service user ID';
COMMENT ON COLUMN carrito.estado_carrito IS 'Cart status: ACTIVO, PROCESADO, ABANDONADO, EXPIRADO';
COMMENT ON COLUMN carrito.total_estimado IS 'Estimated total in CLP for all items in cart';
COMMENT ON COLUMN carrito.fecha_expiracion IS 'Cart expiration date (default 30 days from creation)';

-- Create indexes
CREATE INDEX idx_carrito_usuario ON carrito(id_usuario_ext);
CREATE INDEX idx_carrito_estado ON carrito(estado_carrito);
CREATE INDEX idx_carrito_creado ON carrito(creado_el DESC);

-- =====================================================
-- Table: carrito_item (Cart Line Item)
-- Description: Items added to shopping cart
-- =====================================================
CREATE TABLE carrito_item (
    id_carrito_item UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_carrito UUID NOT NULL,
    id_servicio_ext INTEGER NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0),
    personalizaciones TEXT,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Foreign key constraint
    CONSTRAINT fk_carrito_item_carrito
        FOREIGN KEY (id_carrito)
        REFERENCES carrito(id_carrito)
        ON DELETE CASCADE
);

-- Add comments
COMMENT ON TABLE carrito_item IS 'Cart line items - products/services added to cart';
COMMENT ON COLUMN carrito_item.id_servicio_ext IS 'External reference to inventario service product/service ID';
COMMENT ON COLUMN carrito_item.personalizaciones IS 'Custom notes or specifications for this item';
COMMENT ON COLUMN carrito_item.subtotal IS 'Calculated as precio_unitario * cantidad';

-- Create indexes
CREATE INDEX idx_carrito_item_carrito ON carrito_item(id_carrito);
CREATE INDEX idx_carrito_item_servicio ON carrito_item(id_servicio_ext);

-- =====================================================
-- Table: carrito_servicio_adicional (Cart Additional Services)
-- Description: Add-on services linked to cart items
-- =====================================================
CREATE TABLE carrito_servicio_adicional (
    id_carrito_servicio_adicional UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_carrito_item UUID NOT NULL,
    id_servicio_adicional_ext INTEGER NOT NULL,
    precio_adicional DECIMAL(10,2) NOT NULL CHECK (precio_adicional >= 0),
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Foreign key constraint
    CONSTRAINT fk_carrito_servicio_adicional_item
        FOREIGN KEY (id_carrito_item)
        REFERENCES carrito_item(id_carrito_item)
        ON DELETE CASCADE
);

-- Add comments
COMMENT ON TABLE carrito_servicio_adicional IS 'Additional services/add-ons linked to cart items';
COMMENT ON COLUMN carrito_servicio_adicional.id_servicio_adicional_ext IS 'External reference to inventario service additional service ID';

-- Create indexes
CREATE INDEX idx_carrito_servicio_adicional_item ON carrito_servicio_adicional(id_carrito_item);

-- =====================================================
-- Triggers for automatic timestamp updates
-- =====================================================

-- Function to update actualizado_el timestamp
CREATE OR REPLACE FUNCTION update_carrito_actualizado_el()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_el = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for carrito table
CREATE TRIGGER trg_carrito_actualizado
    BEFORE UPDATE ON carrito
    FOR EACH ROW
    EXECUTE FUNCTION update_carrito_actualizado_el();

-- Trigger for carrito_item table
CREATE TRIGGER trg_carrito_item_actualizado
    BEFORE UPDATE ON carrito_item
    FOR EACH ROW
    EXECUTE FUNCTION update_carrito_actualizado_el();

-- =====================================================
-- End of migration script
-- =====================================================

-- Verify tables created
SELECT
    tablename,
    schemaname
FROM pg_tables
WHERE tablename IN ('carrito', 'carrito_item', 'carrito_servicio_adicional')
ORDER BY tablename;
