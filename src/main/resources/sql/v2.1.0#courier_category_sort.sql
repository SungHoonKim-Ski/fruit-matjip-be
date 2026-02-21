-- v2.1.0 : 카테고리별 상품 노출 순서
ALTER TABLE courier_product_category_mapping
    ADD COLUMN id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT FIRST,
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0,
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT NOW(),
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (id),
    ADD UNIQUE KEY uix_cpcm_product_category (courier_product_id, category_id);
