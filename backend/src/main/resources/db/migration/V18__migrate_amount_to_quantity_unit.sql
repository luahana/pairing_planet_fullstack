-- Migration: Parse legacy amount strings into quantity + unit
-- Only update rows where quantity IS NULL (not already migrated)

-- Pattern: "X cup(s)" or "X CUP"
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'CUP'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*cups?$';

-- Pattern: "X tbsp" or "X tablespoon(s)"
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'TBSP'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*(tbsp|tablespoons?)$';

-- Pattern: "X tsp" or "X teaspoon(s)"
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'TSP'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*(tsp|teaspoons?)$';

-- Pattern: "Xg" or "X g" or "X grams"
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'G'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*(g|grams?)$';

-- Pattern: "Xkg" or "X kg"
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'KG'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*(kg|kilograms?)$';

-- Pattern: "Xml" or "X ml"
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'ML'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*(ml|milliliters?)$';

-- Pattern: "XL" or "X L" or "X liter(s)"
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'L'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*(l|liters?)$';

-- Pattern: "X oz" or "X ounce(s)"
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'OZ'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*(oz|ounces?)$';

-- Pattern: "X lb" or "X pound(s)"
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'LB'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*(lb|lbs|pounds?)$';

-- Pattern: "X piece(s)" or just "X" (number only)
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'PIECE'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*(pieces?)?$';

-- Pattern: "a pinch" or "pinch"
UPDATE recipe_ingredients
SET quantity = 1,
    unit = 'PINCH'
WHERE quantity IS NULL
  AND amount ~* '^(a\s+)?pinch$';

-- Pattern: "a dash" or "dash"
UPDATE recipe_ingredients
SET quantity = 1,
    unit = 'DASH'
WHERE quantity IS NULL
  AND amount ~* '^(a\s+)?dash$';

-- Pattern: "to taste"
UPDATE recipe_ingredients
SET quantity = 1,
    unit = 'TO_TASTE'
WHERE quantity IS NULL
  AND amount ~* '^to\s+taste$';

-- Pattern: "X clove(s)"
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'CLOVE'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*cloves?$';

-- Pattern: "X bunch(es)"
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'BUNCH'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*bunch(es)?$';

-- Pattern: "X can(s)"
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'CAN'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*cans?$';

-- Pattern: "X package(s)" or "X pkg"
UPDATE recipe_ingredients
SET quantity = CAST(regexp_replace(amount, '[^0-9.]', '', 'g') AS DOUBLE PRECISION),
    unit = 'PACKAGE'
WHERE quantity IS NULL
  AND amount ~* '^\d+\.?\d*\s*(packages?|pkgs?)$';

-- Fallback: Any remaining with amount but no quantity -> set to 1 PIECE
UPDATE recipe_ingredients
SET quantity = 1,
    unit = 'PIECE'
WHERE quantity IS NULL
  AND amount IS NOT NULL
  AND amount != '';
