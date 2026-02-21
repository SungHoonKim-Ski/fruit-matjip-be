-- v2.4.0 : 추천 상품 및 검색

ALTER TABLE courier_products
    ADD COLUMN recommended TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN recommend_order INT NOT NULL DEFAULT 0;

CREATE INDEX idx_courier_products_recommended ON courier_products(recommended);
