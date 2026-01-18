-- =============================================================================
-- Rename food1_master_id to food_master_id in recipes table
-- Fixes column name mismatch between database and application code
-- =============================================================================

-- Rename the column
ALTER TABLE recipes RENAME COLUMN food1_master_id TO food_master_id;

-- Recreate index with correct column name (if it exists with old name)
DROP INDEX IF EXISTS idx_recipes_food1_master;
CREATE INDEX IF NOT EXISTS idx_recipes_food_master ON recipes(food_master_id);
