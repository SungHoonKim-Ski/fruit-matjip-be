-- 1. Create courier_product_category table (independent from product_category)
CREATE TABLE IF NOT EXISTS courier_product_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    image_url VARCHAR(500),
    sort_order INT DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. Drop FK on category_id → product_category
SET @fk_cat = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'courier_product_category_mapping'
      AND COLUMN_NAME = 'category_id'
      AND REFERENCED_TABLE_NAME = 'product_category'
    LIMIT 1
);
SET @drop_fk_cat = IF(@fk_cat IS NOT NULL,
    CONCAT('ALTER TABLE courier_product_category_mapping DROP FOREIGN KEY `', @fk_cat, '`'),
    'SELECT 1');
PREPARE stmt FROM @drop_fk_cat;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. Drop FK on courier_product_id → courier_products (index backs this FK too)
SET @fk_prod = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'courier_product_category_mapping'
      AND COLUMN_NAME = 'courier_product_id'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @drop_fk_prod = IF(@fk_prod IS NOT NULL,
    CONCAT('ALTER TABLE courier_product_category_mapping DROP FOREIGN KEY `', @fk_prod, '`'),
    'SELECT 1');
PREPARE stmt FROM @drop_fk_prod;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. Drop unique index (safe after both FKs removed)
ALTER TABLE courier_product_category_mapping DROP INDEX uix_cpcm_product_category;

-- 5. Drop old category_id column (was referencing product_category)
ALTER TABLE courier_product_category_mapping DROP COLUMN category_id;

-- 6. Add new category_id column referencing courier_product_category
ALTER TABLE courier_product_category_mapping ADD COLUMN category_id BIGINT NOT NULL;

-- 7. Re-add all constraints
ALTER TABLE courier_product_category_mapping ADD CONSTRAINT fk_cpcm_courier_product FOREIGN KEY (courier_product_id) REFERENCES courier_products(id);
ALTER TABLE courier_product_category_mapping ADD CONSTRAINT fk_cpcm_courier_category FOREIGN KEY (category_id) REFERENCES courier_product_category(id);
ALTER TABLE courier_product_category_mapping ADD UNIQUE INDEX uix_cpcm_product_category (courier_product_id, category_id);
