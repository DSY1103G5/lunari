-- =====================================================
-- LUNARi Carrito - Update Carrito Table
-- Migration Script: Add order number reference to carrito table
-- Version: 1.0
-- Date: 2025-11-30
-- =====================================================

-- Add column to link carrito to generated order
ALTER TABLE carrito
ADD COLUMN IF NOT EXISTS numero_orden VARCHAR(50);

-- Add comment to new column
COMMENT ON COLUMN carrito.numero_orden IS 'Reference to generated order number (from pedido.numero_pedido)';

-- Create index for efficient lookups
CREATE INDEX IF NOT EXISTS idx_carrito_numero_orden ON carrito(numero_orden);

-- =====================================================
-- Verify column added
-- =====================================================

-- Show carrito table structure
SELECT
    column_name,
    data_type,
    character_maximum_length,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'carrito'
  AND table_schema = 'public'
ORDER BY ordinal_position;
