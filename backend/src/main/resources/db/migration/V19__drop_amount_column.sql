-- Drop the legacy amount column after migration to structured quantity + unit
ALTER TABLE recipe_ingredients DROP COLUMN IF EXISTS amount;
