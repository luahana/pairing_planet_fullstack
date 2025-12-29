-- 1. 대분류 삽입 (depth = 1, parent_id IS NULL)
INSERT INTO food_categories (id, code, name, depth, parent_id)
    OVERRIDING SYSTEM VALUE
VALUES
    (1, 'DISH', jsonb_build_object('ko', '요리', 'en', 'Dish', 'ja', '料理'), 1, NULL),
    (2, 'DESSERT', jsonb_build_object('ko', '디저트', 'en', 'Dessert', 'ja', 'デザート'), 1, NULL),
    (3, 'DRINK', jsonb_build_object('ko', '음료', 'en', 'Drink', 'ja', '飲み物'), 1, NULL),
    (4, 'ALCOHOL', jsonb_build_object('ko', '주류', 'en', 'Alcohol', 'ja', 'お酒'), 1, NULL),
    (5, 'SNACK', jsonb_build_object('ko', '간식', 'en', 'Snack', 'ja', 'おやつ'), 1, NULL),
    (6, 'SAUCE', jsonb_build_object('ko', '소스', 'en', 'Sauce', 'ja', 'ソース'), 1, NULL)
ON CONFLICT (id) DO NOTHING;

-- 2. 중분류 삽입 (depth = 2, parent_id 참조)
INSERT INTO food_categories (id, code, name, depth, parent_id)
    OVERRIDING SYSTEM VALUE
VALUES
-- 요리 하위 (Parent: 1)
(101, 'RICE', jsonb_build_object('ko', '밥', 'en', 'Rice', 'ja', 'ご飯'), 2, 1),
(102, 'NOODLE', jsonb_build_object('ko', '면', 'en', 'Noodle', 'ja', '麺'), 2, 1),
(103, 'MEAT', jsonb_build_object('ko', '고기', 'en', 'Meat', 'ja', '肉'), 2, 1),
(104, 'SEAFOOD', jsonb_build_object('ko', '해산물', 'en', 'Seafood', 'ja', '海鮮'), 2, 1),
(105, 'VEGETABLE', jsonb_build_object('ko', '채소', 'en', 'Vegetable', 'ja', '野菜'), 2, 1),
(106, 'SOUP', jsonb_build_object('ko', '국물요리', 'en', 'Soup', 'ja', 'スープ'), 2, 1),
(107, 'SANDWICH', jsonb_build_object('ko', '샌드위치', 'en', 'Sandwich', 'ja', 'サンドイッチ'), 2, 1), -- 신규
(107, 'SANDWICH', jsonb_build_object('ko', '샌드위치', 'en', 'Sandwich', 'ja', 'サンドイッチ'), 2, 1), -- 신규
(108, 'SALAD', jsonb_build_object('ko', '샐러드', 'en', 'Salad', 'ja', 'サラダ'), 2, 1),         -- 신규
(109, 'PIZZA', jsonb_build_object('ko', '피자', 'en', 'Pizza', 'ja', 'ピザ'), 2, 1),           -- 신규

-- 디저트 하위 (Parent: 2)
(201, 'BAKERY', jsonb_build_object('ko', '베이커리', 'en', 'Bakery', 'ja', 'ベーカリー'), 2, 2),
(202, 'FRUIT', jsonb_build_object('ko', '과일', 'en', 'Fruit', 'ja', '果物'), 2, 2),
(203, 'ICECREAM', jsonb_build_object('ko', '아이스크림', 'en', 'Icecream', 'ja', 'アイスクリーム'), 2, 2),
(204, 'RICECAKE', jsonb_build_object('ko', '떡', 'en', 'Ricecake', 'ja', '餅'), 2, 2),

-- 음료 하위 (Parent: 3)
(301, 'COFFEE', jsonb_build_object('ko', '커피', 'en', 'Coffee', 'ja', 'コーヒー'), 2, 3),
(302, 'TEA', jsonb_build_object('ko', '차', 'en', 'Tea', 'ja', 'お茶'), 2, 3),
(303, 'SODA', jsonb_build_object('ko', '탄산음료', 'en', 'Soda', 'ja', '탄산음료'), 2, 3),
(304, 'JUICE', jsonb_build_object('ko', '주스', 'en', 'Juice', 'ja', 'ジュース'), 2, 3),
(305, 'DAIRY', jsonb_build_object('ko', '유제품', 'en', 'Dairy', 'ja', '乳製品'), 2, 3),
(306, 'SMOOTHIE', jsonb_build_object('ko', '스무디', 'en', 'Smoothie', 'ja', 'スムージー'), 2, 3), -- 신규
(307, 'VEGAN_MILK', jsonb_build_object('ko', '대체유', 'en', 'Veganmilk', 'ja', '代替乳'), 2, 3),   -- 신규

-- 주류 하위 (Parent: 4)
(401, 'BEER', jsonb_build_object('ko', '맥주', 'en', 'Beer', 'ja', 'ビール'), 2, 4),
(402, 'WINE', jsonb_build_object('ko', '와인', 'en', 'Wine', 'ja', 'ワイン'), 2, 4),
(403, 'TRADITIONAL', jsonb_build_object('ko', '전통주', 'en', 'Traditional', 'ja', '伝統酒'), 2, 4),
(404, 'SPIRITS', jsonb_build_object('ko', '증류주', 'en', 'Spirits', 'ja', '蒸留酒'), 2, 4),
(405, 'CIDER', jsonb_build_object('ko', '사이더', 'en', 'Cider', 'ja', 'サイダー'), 2, 4),          -- 신규

-- 간식 하위 (Parent: 5)
(501, 'SNACK_CRACKER', jsonb_build_object('ko', '과자', 'en', 'Snack', 'ja', 'スナック菓子'), 2, 5),
(502, 'CANDY', jsonb_build_object('ko', '사탕젤리', 'en', 'Candy', 'ja', 'キャン디'), 2, 5),
(503, 'NUTS', jsonb_build_object('ko', '견과류', 'en', 'Nuts', 'ja', 'ナッツ'), 2, 5),
(504, 'DRIED', jsonb_build_object('ko', '마른안주', 'en', 'Dried', 'ja', '干物'), 2, 5),
(505, 'CEREAL', jsonb_build_object('ko', '시리얼', 'en', 'Cereal', 'ja', 'シリアル'), 2, 5),        -- 신규
(506, 'PROTEIN_BAR', jsonb_build_object('ko', '에너지바', 'en', 'Proteinbar', 'ja', 'プロテインバー'), 2, 5), -- 신규

-- 소스 하위 (Parent: 6)
(601, 'OIL', jsonb_build_object('ko', '오일', 'en', 'Oil', 'ja', 'オイル'), 2, 6),
(602, 'PASTE', jsonb_build_object('ko', '장류', 'en', 'Paste', 'ja', '味噌'), 2, 6),
(603, 'SYRUP', jsonb_build_object('ko', '시럽', 'en', 'Syrup', 'ja', 'シロップ'), 2, 6),
(604, 'SEASONING', jsonb_build_object('ko', '양념', 'en', 'Seasoning', 'ja', '調味料'), 2, 6),
(605, 'DRESSING', jsonb_build_object('ko', '드레싱', 'en', 'Dressing', 'ja', 'ドレッシング'), 2, 6), -- 신규
(606, 'SPREAD', jsonb_build_object('ko', '스프레드', 'en', 'Spread', 'ja', 'スプレッド'), 2, 6)     -- 신규
ON CONFLICT (id) DO NOTHING;

-- 3. ID 시퀀스 동기화
SELECT setval(pg_get_serial_sequence('food_categories', 'id'), COALESCE(MAX(id), 1)) FROM food_categories;