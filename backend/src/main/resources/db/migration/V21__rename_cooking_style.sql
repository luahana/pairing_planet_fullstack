-- =============================================================================
-- RENAME COOKING STYLE FIELDS
-- =============================================================================
-- Renames:
--   users.default_food_style -> default_cooking_style
--   recipes.culinary_locale -> cooking_style
--   bot_personas.culinary_locale -> cooking_style
-- Expands column size to VARCHAR(15) to accommodate "international"
-- Removes CHECK constraint to allow any country code
-- =============================================================================

-- 1. Normalize existing data: convert full locales (ko-KR) to country codes (KR)
UPDATE users SET default_food_style = UPPER(SPLIT_PART(default_food_style, '-', 2))
WHERE default_food_style LIKE '%-%';

UPDATE recipes SET culinary_locale = UPPER(SPLIT_PART(culinary_locale, '-', 2))
WHERE culinary_locale LIKE '%-%';

UPDATE bot_personas SET culinary_locale = UPPER(SPLIT_PART(culinary_locale, '-', 2))
WHERE culinary_locale LIKE '%-%';

-- 2. Rename columns
ALTER TABLE users RENAME COLUMN default_food_style TO default_cooking_style;
ALTER TABLE recipes RENAME COLUMN culinary_locale TO cooking_style;
ALTER TABLE bot_personas RENAME COLUMN culinary_locale TO cooking_style;

-- 3. Expand column size for "international"
ALTER TABLE users ALTER COLUMN default_cooking_style TYPE VARCHAR(15);
ALTER TABLE recipes ALTER COLUMN cooking_style TYPE VARCHAR(15);
ALTER TABLE bot_personas ALTER COLUMN cooking_style TYPE VARCHAR(15);

-- 4. Drop CHECK constraint (accept any country code)
ALTER TABLE recipes DROP CONSTRAINT IF EXISTS chk_recipes_culinary_locale;

-- 5. Recreate indexes with new column name
DROP INDEX IF EXISTS idx_recipes_locale;
DROP INDEX IF EXISTS idx_recipes_discovery;
CREATE INDEX idx_recipes_cooking_style ON recipes(cooking_style);
CREATE INDEX idx_recipes_discovery ON recipes(cooking_style, created_at DESC)
    WHERE deleted_at IS NULL AND is_private = FALSE;

-- 6. Update comments
COMMENT ON COLUMN users.default_cooking_style IS 'User preferred cooking style (ISO country code or international)';
COMMENT ON COLUMN recipes.cooking_style IS 'Recipe cuisine style (ISO country code or international)';
COMMENT ON COLUMN bot_personas.cooking_style IS 'Bot cuisine style (ISO country code or international)';
