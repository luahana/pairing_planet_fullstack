-- =============================================================================
-- FOOD CATEGORIES - Repeatable Migration
-- Purpose: Seed data that can be updated and re-deployed
-- =============================================================================

-- Clear and repopulate (idempotent)
TRUNCATE food_categories CASCADE;

-- Reset sequence
ALTER SEQUENCE food_categories_id_seq RESTART WITH 1;

-- Top-level categories
INSERT INTO food_categories (public_id, code, depth, name, created_at, updated_at) VALUES
(gen_random_uuid(), 'DISH', 1, '{"ko-KR": "요리", "en-US": "Dish"}'::jsonb, NOW(), NOW()),
(gen_random_uuid(), 'DESSERT', 1, '{"ko-KR": "디저트", "en-US": "Dessert"}'::jsonb, NOW(), NOW()),
(gen_random_uuid(), 'DRINK', 1, '{"ko-KR": "음료", "en-US": "Drink"}'::jsonb, NOW(), NOW()),
(gen_random_uuid(), 'SNACK', 1, '{"ko-KR": "간식", "en-US": "Snack"}'::jsonb, NOW(), NOW()),
(gen_random_uuid(), 'SAUCE', 1, '{"ko-KR": "소스", "en-US": "Sauce"}'::jsonb, NOW(), NOW()),
(gen_random_uuid(), 'ALCOHOL', 1, '{"ko-KR": "주류", "en-US": "Alcohol"}'::jsonb, NOW(), NOW());

-- Subcategories (depth 2) - can add more as needed
INSERT INTO food_categories (public_id, code, depth, name, parent_id, created_at, updated_at) VALUES
(gen_random_uuid(), 'KOREAN', 2, '{"ko-KR": "한식", "en-US": "Korean"}'::jsonb, (SELECT id FROM food_categories WHERE code = 'DISH'), NOW(), NOW()),
(gen_random_uuid(), 'JAPANESE', 2, '{"ko-KR": "일식", "en-US": "Japanese"}'::jsonb, (SELECT id FROM food_categories WHERE code = 'DISH'), NOW(), NOW()),
(gen_random_uuid(), 'CHINESE', 2, '{"ko-KR": "중식", "en-US": "Chinese"}'::jsonb, (SELECT id FROM food_categories WHERE code = 'DISH'), NOW(), NOW()),
(gen_random_uuid(), 'WESTERN', 2, '{"ko-KR": "양식", "en-US": "Western"}'::jsonb, (SELECT id FROM food_categories WHERE code = 'DISH'), NOW(), NOW());
