-- v2.3.0 : 상품 Variant 옵션

CREATE TABLE courier_product_option_groups (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    courier_product_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(50) NOT NULL,
    required TINYINT(1) NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_cpog_product (courier_product_id),
    CONSTRAINT fk_cpog_product
      FOREIGN KEY (courier_product_id)
          REFERENCES courier_products(id)
          ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE courier_product_options (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    option_group_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(50) NOT NULL,
    additional_price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_cpo_group (option_group_id),
    CONSTRAINT fk_cpo_group
      FOREIGN KEY (option_group_id)
          REFERENCES courier_product_option_groups(id)
          ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- courier_order_items에 선택 옵션 JSON 필드 추가
ALTER TABLE courier_order_items
    ADD COLUMN selected_options TEXT AFTER amount;
