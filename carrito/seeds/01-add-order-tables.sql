-- =====================================================
-- LUNARi Carrito - Order Management Tables
-- Migration Script: Add Order, Order Item, and Payment tables
-- Version: 1.0
-- Date: 2025-11-30
-- =====================================================

-- Drop existing tables if they exist (for clean migration)
DROP TABLE IF EXISTS pago CASCADE;
DROP TABLE IF EXISTS pedido_item CASCADE;
DROP TABLE IF EXISTS pedido CASCADE;

-- =====================================================
-- Table: pedido (Order)
-- Description: Main order entity tracking order lifecycle
-- =====================================================
CREATE TABLE pedido (
    id_pedido UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_pedido VARCHAR(50) NOT NULL UNIQUE,
    id_carrito UUID NOT NULL,
    id_usuario_ext UUID NOT NULL,
    estado_pedido VARCHAR(20) NOT NULL CHECK (estado_pedido IN (
        'CREADO',
        'PAGO_PENDIENTE',
        'PAGO_COMPLETADO',
        'PROCESANDO',
        'COMPLETADO',
        'CANCELADO',
        'FALLIDO'
    )),
    total_productos DECIMAL(12,2) NOT NULL CHECK (total_productos >= 0),
    total_puntos_ganados INTEGER DEFAULT 0 CHECK (total_puntos_ganados >= 0),
    notas_cliente TEXT,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completado_el TIMESTAMP WITH TIME ZONE
);

-- Add comment to table
COMMENT ON TABLE pedido IS 'Orders table - tracks order lifecycle from creation to completion';

-- Add column comments
COMMENT ON COLUMN pedido.numero_pedido IS 'Human-readable order number (e.g., ORD-20251130-00001)';
COMMENT ON COLUMN pedido.id_carrito IS 'Foreign key reference to carrito table';
COMMENT ON COLUMN pedido.id_usuario_ext IS 'External reference to usuario service user ID';
COMMENT ON COLUMN pedido.estado_pedido IS 'Order status: CREADO, PAGO_PENDIENTE, PAGO_COMPLETADO, PROCESANDO, COMPLETADO, CANCELADO, FALLIDO';
COMMENT ON COLUMN pedido.total_productos IS 'Total amount in CLP for all products in order';
COMMENT ON COLUMN pedido.total_puntos_ganados IS 'Loyalty points awarded to user (1 point per 100 CLP)';

-- Create indexes for performance
CREATE INDEX idx_pedido_usuario ON pedido(id_usuario_ext);
CREATE INDEX idx_pedido_estado ON pedido(estado_pedido);
CREATE INDEX idx_pedido_numero ON pedido(numero_pedido);
CREATE INDEX idx_pedido_carrito ON pedido(id_carrito);
CREATE INDEX idx_pedido_creado ON pedido(creado_el DESC);

-- =====================================================
-- Table: pedido_item (Order Line Item)
-- Description: Snapshot of products purchased in order
-- =====================================================
CREATE TABLE pedido_item (
    id_pedido_item UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_pedido UUID NOT NULL,
    id_producto_ext BIGINT NOT NULL,
    codigo_producto VARCHAR(20) NOT NULL,
    nombre_producto VARCHAR(255) NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(12,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0),
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Foreign key constraint
    CONSTRAINT fk_pedido_item_pedido
        FOREIGN KEY (id_pedido)
        REFERENCES pedido(id_pedido)
        ON DELETE CASCADE
);

-- Add comment to table
COMMENT ON TABLE pedido_item IS 'Order line items - snapshot of products at purchase time';

-- Add column comments
COMMENT ON COLUMN pedido_item.id_producto_ext IS 'External reference to inventario service product ID';
COMMENT ON COLUMN pedido_item.codigo_producto IS 'Product code snapshot (e.g., JM001, AC002)';
COMMENT ON COLUMN pedido_item.nombre_producto IS 'Product name snapshot at time of purchase';
COMMENT ON COLUMN pedido_item.subtotal IS 'Calculated as precio_unitario * cantidad';

-- Create indexes
CREATE INDEX idx_pedido_item_pedido ON pedido_item(id_pedido);
CREATE INDEX idx_pedido_item_producto ON pedido_item(id_producto_ext);

-- =====================================================
-- Table: pago (Payment)
-- Description: Payment transaction details and Transbank integration
-- =====================================================
CREATE TABLE pago (
    id_pago UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_pedido UUID NOT NULL UNIQUE,
    metodo_pago VARCHAR(20) NOT NULL CHECK (metodo_pago IN (
        'WEBPAY_PLUS',
        'TRANSFERENCIA',
        'EFECTIVO'
    )),
    estado_pago VARCHAR(20) NOT NULL CHECK (estado_pago IN (
        'PENDIENTE',
        'APROBADO',
        'RECHAZADO',
        'ANULADO',
        'EXPIRADO'
    )),
    monto_total DECIMAL(12,2) NOT NULL CHECK (monto_total >= 0),

    -- Transbank WebPay Plus fields
    transbank_token VARCHAR(100),
    transbank_buy_order VARCHAR(100) NOT NULL,
    transbank_session_id VARCHAR(100) NOT NULL,
    payment_url TEXT,
    authorization_code VARCHAR(50),
    response_code INTEGER,

    -- Timestamps
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    confirmado_el TIMESTAMP WITH TIME ZONE,

    -- Foreign key constraint
    CONSTRAINT fk_pago_pedido
        FOREIGN KEY (id_pedido)
        REFERENCES pedido(id_pedido)
        ON DELETE CASCADE
);

-- Add comment to table
COMMENT ON TABLE pago IS 'Payment transactions - Transbank WebPay Plus integration';

-- Add column comments
COMMENT ON COLUMN pago.metodo_pago IS 'Payment method: WEBPAY_PLUS, TRANSFERENCIA, EFECTIVO';
COMMENT ON COLUMN pago.estado_pago IS 'Payment status: PENDIENTE, APROBADO, RECHAZADO, ANULADO, EXPIRADO';
COMMENT ON COLUMN pago.transbank_token IS 'Transbank WebPay token for transaction tracking';
COMMENT ON COLUMN pago.transbank_buy_order IS 'Unique buy order identifier for Transbank';
COMMENT ON COLUMN pago.transbank_session_id IS 'Session identifier for Transbank transaction';
COMMENT ON COLUMN pago.payment_url IS 'Transbank payment redirect URL';
COMMENT ON COLUMN pago.authorization_code IS 'Transbank authorization code upon approval';
COMMENT ON COLUMN pago.response_code IS 'Transbank response code (0 = approved)';

-- Create indexes
CREATE INDEX idx_pago_pedido ON pago(id_pedido);
CREATE INDEX idx_pago_token ON pago(transbank_token);
CREATE INDEX idx_pago_estado ON pago(estado_pago);
CREATE INDEX idx_pago_buy_order ON pago(transbank_buy_order);

-- =====================================================
-- Triggers for automatic timestamp updates
-- =====================================================

-- Function to update actualizado_el timestamp
CREATE OR REPLACE FUNCTION update_actualizado_el()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_el = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for pedido table
CREATE TRIGGER trg_pedido_actualizado
    BEFORE UPDATE ON pedido
    FOR EACH ROW
    EXECUTE FUNCTION update_actualizado_el();

-- =====================================================
-- End of migration script
-- =====================================================

-- Verify tables created
SELECT
    tablename,
    schemaname
FROM pg_tables
WHERE tablename IN ('pedido', 'pedido_item', 'pago')
ORDER BY tablename;
