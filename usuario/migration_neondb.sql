-- =====================================================
-- LUNARi User Service - PostgreSQL NeonDB Migration
-- =====================================================
-- This script creates the users table schema for the
-- usuario microservice migrated from DynamoDB to NeonDB
-- =====================================================

-- Drop table if exists (for clean migration)
DROP TABLE IF EXISTS users CASCADE;

-- Create users table
CREATE TABLE users (
    -- Primary identification
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,

    -- Personal information (stored as JSONB)
    personal JSONB,

    -- Address information (stored as JSONB)
    address JSONB,

    -- Client preferences (stored as JSONB)
    preferences JSONB,

    -- Gaming profile (stored as JSONB)
    gaming JSONB,

    -- Client statistics (stored as JSONB)
    stats JSONB,

    -- Coupons (stored as JSONB array)
    coupons JSONB,

    -- Status flags
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_verified BOOLEAN NOT NULL DEFAULT false,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- =====================================================
-- Indexes for performance optimization
-- =====================================================

-- Index on email (for authentication queries)
CREATE UNIQUE INDEX idx_email ON users(email);

-- Index on username (for profile lookups)
CREATE UNIQUE INDEX idx_username ON users(username);

-- Index on is_active (for filtering active users)
CREATE INDEX idx_is_active ON users(is_active);

-- Index on is_verified (for filtering verified users)
CREATE INDEX idx_is_verified ON users(is_verified);

-- Composite index on is_active and is_verified (for combined filtering)
CREATE INDEX idx_active_verified ON users(is_active, is_verified);

-- Index on created_at (for chronological queries)
CREATE INDEX idx_created_at ON users(created_at DESC);

-- JSONB GIN indexes for searching within JSON fields
CREATE INDEX idx_personal_gin ON users USING GIN (personal);
CREATE INDEX idx_preferences_gin ON users USING GIN (preferences);
CREATE INDEX idx_stats_gin ON users USING GIN (stats);

-- =====================================================
-- Triggers for automatic updated_at management
-- =====================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger to automatically update updated_at
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- Sample data (optional - comment out if not needed)
-- =====================================================

-- Insert sample user 1
INSERT INTO users (
    id, username, email, password,
    personal, address, preferences, gaming, stats, coupons,
    is_active, is_verified
) VALUES (
    '1',
    'omunoz',
    'osca.munozs@duocuc.cl',
    '$2a$10$hashedPasswordExample123456789012345678901234567890',
    '{"firstName": "Oscar", "lastName": "Muñoz", "phone": "+56912345678", "birthdate": "1995-05-15", "bio": "Gamer enthusiast", "avatar": "https://example.com/avatar1.jpg", "memberSince": "2022"}'::jsonb,
    '{"addressLine1": "Av. Principal 123", "addressLine2": "Depto 45", "city": "Santiago", "region": "Metropolitana", "postalCode": "8320000", "country": "Chile", "deliveryNotes": "Timbre 45"}'::jsonb,
    '{"favoriteCategories": ["JM", "CG"], "preferredPlatform": "pc", "gamingHours": "16-30", "notifyOffers": true, "notifyNewProducts": true, "notifyRestocks": false, "notifyNewsletter": true}'::jsonb,
    '{"gamerTag": "OscarGamer95", "favoriteGenre": "rpg", "skillLevel": "advanced", "streamingPlatforms": ["twitch", "youtube"], "favoriteGames": "The Witcher 3, Elden Ring, Cyberpunk 2077"}'::jsonb,
    '{"level": "Gold", "points": 1500, "purchases": 12, "reviews": 8, "favorites": 25}'::jsonb,
    '[{"id": "COUP-001", "code": "GOLD-OSC-001", "description": "10% descuento en juegos RPG", "type": "percentage", "value": 10.0, "minPurchase": 50000.0, "expiresAt": "2025-12-31", "isUsed": false}]'::jsonb,
    true,
    true
);

-- Insert sample user 2
INSERT INTO users (
    id, username, email, password,
    personal, address, preferences, gaming, stats, coupons,
    is_active, is_verified
) VALUES (
    '2',
    'mgarcia',
    'maria.garcia@example.com',
    '$2a$10$hashedPasswordExample123456789012345678901234567890',
    '{"firstName": "María", "lastName": "García", "phone": "+56987654321", "birthdate": "1998-08-20", "bio": "Casual gamer", "avatar": "https://example.com/avatar2.jpg", "memberSince": "2023"}'::jsonb,
    '{"addressLine1": "Calle Secundaria 456", "city": "Valparaíso", "region": "Valparaíso", "postalCode": "2340000", "country": "Chile"}'::jsonb,
    '{"favoriteCategories": ["AC", "PS"], "preferredPlatform": "ps5", "gamingHours": "6-10", "notifyOffers": true, "notifyNewProducts": false, "notifyRestocks": true, "notifyNewsletter": false}'::jsonb,
    '{"gamerTag": "MariaPS5", "favoriteGenre": "action", "skillLevel": "intermediate", "streamingPlatforms": ["twitch"], "favoriteGames": "God of War, Horizon Zero Dawn"}'::jsonb,
    '{"level": "Silver", "points": 750, "purchases": 5, "reviews": 3, "favorites": 10}'::jsonb,
    '[]'::jsonb,
    true,
    false
);

-- Insert sample user 3 (inactive user)
INSERT INTO users (
    id, username, email, password,
    personal, stats,
    is_active, is_verified
) VALUES (
    '3',
    'jperez',
    'juan.perez@example.com',
    '$2a$10$hashedPasswordExample123456789012345678901234567890',
    '{"firstName": "Juan", "lastName": "Pérez", "phone": "+56911111111", "memberSince": "2024"}'::jsonb,
    '{"level": "Bronze", "points": 0, "purchases": 0, "reviews": 0, "favorites": 0}'::jsonb,
    false,
    false
);

-- =====================================================
-- Verification queries
-- =====================================================

-- Count total users
SELECT COUNT(*) as total_users FROM users;

-- List all users with basic info
SELECT id, username, email, is_active, is_verified, created_at
FROM users
ORDER BY created_at DESC;

-- Query active and verified users
SELECT id, username, email,
       personal->>'firstName' as first_name,
       personal->>'lastName' as last_name,
       stats->>'level' as level,
       stats->>'points' as points
FROM users
WHERE is_active = true AND is_verified = true;

-- Query users by gaming platform preference
SELECT username, email,
       preferences->>'preferredPlatform' as platform,
       gaming->>'gamerTag' as gamertag
FROM users
WHERE preferences->>'preferredPlatform' = 'pc';

-- =====================================================
-- Useful maintenance queries
-- =====================================================

-- Update user stats
-- UPDATE users
-- SET stats = jsonb_set(stats, '{points}', to_jsonb((stats->>'points')::int + 100))
-- WHERE id = '1';

-- Add a new coupon to a user
-- UPDATE users
-- SET coupons = coupons || '[{"id": "COUP-002", "code": "SPECIAL-2025", "description": "Descuento especial", "type": "fixed", "value": 5000, "minPurchase": 30000, "expiresAt": "2025-06-30", "isUsed": false}]'::jsonb
-- WHERE id = '1';

-- Search users by name (case-insensitive)
-- SELECT id, username, email,
--        personal->>'firstName' as first_name,
--        personal->>'lastName' as last_name
-- FROM users
-- WHERE LOWER(personal->>'firstName') LIKE LOWER('%oscar%')
--    OR LOWER(personal->>'lastName') LIKE LOWER('%muñoz%');
