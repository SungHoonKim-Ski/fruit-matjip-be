-- v2.2.0 : 배송비 템플릿
CREATE TABLE shipping_fee_templates (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    base_fee DECIMAL(12, 2) NOT NULL,
    per_quantity_fee DECIMAL(12, 2),
    free_shipping_min_amount DECIMAL(12, 2),
    active TINYINT(1) NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 기본 배송비 템플릿 seed
INSERT INTO shipping_fee_templates (name, base_fee, per_quantity_fee, free_shipping_min_amount, active, sort_order, created_at, updated_at)
VALUES ('기본배송', 3000.00, NULL, NULL, 1, 0, NOW(), NOW());

-- courier_products에 템플릿 FK 추가
ALTER TABLE courier_products
    ADD COLUMN shipping_fee_template_id BIGINT UNSIGNED NULL,
    ADD CONSTRAINT fk_cp_shipping_template
        FOREIGN KEY (shipping_fee_template_id)
            REFERENCES shipping_fee_templates(id)
            ON DELETE SET NULL;
