-- ============================================================
-- STEP 0: categories table restructure (run BEFORE app startup)
-- Categories are now restaurant-private. Drop and recreate the
-- table so Hibernate can add the NOT NULL restaurant_id column.
-- WARNING: this deletes all existing category rows.
-- ============================================================
TRUNCATE TABLE categories RESTART IDENTITY CASCADE;
-- Hibernate ddl-auto=update will then add restaurant_id NOT NULL.

-- Run once against the database (requires superuser for extensions).
-- Neon / Supabase: extensions are pre-installed, just run the CREATE INDEX lines.

-- 1. Enable required extensions
CREATE EXTENSION IF NOT EXISTS unaccent;   -- strips diacritics: Phở → Pho
CREATE EXTENSION IF NOT EXISTS pg_trgm;   -- trigram index: powers LIKE '%..%' on GIN

-- 2. GIN index on restaurants.name
--    Allows: unaccent(lower(name)) LIKE '%pho%'  to use this index
DROP INDEX IF EXISTS idx_restaurant_name;   -- remove old B-tree index (replaced)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurant_name_gin
    ON restaurants USING GIN (unaccent(lower(name)) gin_trgm_ops);

-- 3. GIN index on products.name
DROP INDEX IF EXISTS idx_product_name;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_product_name_gin
    ON products USING GIN (unaccent(lower(name)) gin_trgm_ops);

-- 4. Verify (optional)
-- SELECT * FROM restaurants WHERE unaccent(lower(name)) LIKE '%' || unaccent(lower('pho')) || '%';
-- SELECT * FROM products     WHERE unaccent(lower(name)) LIKE '%' || unaccent(lower('bun')) || '%';
