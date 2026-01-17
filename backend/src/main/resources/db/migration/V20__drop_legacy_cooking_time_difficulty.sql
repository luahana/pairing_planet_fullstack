-- Drop legacy cooking_time and difficulty columns from recipes table
-- These have been replaced by the cooking_time_range enum field
ALTER TABLE recipes DROP COLUMN IF EXISTS cooking_time;
ALTER TABLE recipes DROP COLUMN IF EXISTS difficulty;
