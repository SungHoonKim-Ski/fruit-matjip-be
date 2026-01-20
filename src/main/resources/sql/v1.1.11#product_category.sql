-- 1. product_keyword → product_category 테이블 이름 변경
RENAME TABLE product_keyword TO product_category;

-- 2. 컬럼명 변경 (keyword_url → image_url)
ALTER TABLE product_category CHANGE COLUMN keyword_url image_url TEXT;

-- 3. sort_order 컬럼 추가
ALTER TABLE product_category ADD COLUMN sort_order INT DEFAULT 0;

-- 4. Product ↔ ProductCategory 연결 테이블 (기존 테이블 id 타입과 일치하도록)
-- products.id = BIGINT UNSIGNED, product_category.id = BIGINT
CREATE TABLE product_category_mapping (
    product_id BIGINT UNSIGNED NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, category_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES product_category(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


