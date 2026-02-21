-- =============================================
-- v2.0.0 : 택배 쇼핑몰 (courier shop) 스키마
-- =============================================

-- 1. courier_products (택배 상품)
CREATE TABLE courier_products (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    product_url VARCHAR(500) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    visible TINYINT(1) NOT NULL DEFAULT 1,
    weight_gram INT,
    description TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    total_sold BIGINT NOT NULL DEFAULT 0,
    admin_id BIGINT UNSIGNED,
    deleted_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_courier_products_visible (visible),
    CONSTRAINT fk_courier_products_admin
      FOREIGN KEY (admin_id)
          REFERENCES admins(id),
    CHECK (stock >= 0),
    CHECK (total_sold >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. courier_product_detail_images (택배 상품 상세 이미지)
CREATE TABLE courier_product_detail_images (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    courier_product_id BIGINT UNSIGNED NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_cpdi_product (courier_product_id),
    CONSTRAINT fk_cpdi_product
      FOREIGN KEY (courier_product_id)
          REFERENCES courier_products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. courier_product_category_mapping (택배 상품 ↔ 카테고리 M:N)
CREATE TABLE courier_product_category_mapping (
    courier_product_id BIGINT UNSIGNED NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (courier_product_id, category_id),
    FOREIGN KEY (courier_product_id) REFERENCES courier_products(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES product_category(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. courier_orders (택배 주문)
CREATE TABLE courier_orders (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_uid CHAR(36) NOT NULL,
    display_code VARCHAR(18) NOT NULL,
    status VARCHAR(20) NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(30) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    address1 VARCHAR(200) NOT NULL,
    address2 VARCHAR(200),
    shipping_memo VARCHAR(500),
    is_island TINYINT(1) NOT NULL DEFAULT 0,
    product_amount DECIMAL(12, 2) NOT NULL,
    shipping_fee DECIMAL(12, 2) NOT NULL,
    island_surcharge DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12, 2) NOT NULL,
    pg_tid VARCHAR(100),
    paid_at DATETIME,
    waybill_number VARCHAR(50),
    courier_company VARCHAR(30) DEFAULT 'LOGEN',
    shipped_at DATETIME,
    delivered_at DATETIME,
    idempotency_key VARCHAR(64),
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uix_courier_orders_display_code (display_code),
    KEY idx_courier_orders_user (user_uid),
    KEY idx_courier_orders_status (status),
    KEY idx_courier_orders_idempotency (idempotency_key),
    CONSTRAINT fk_courier_orders_user
      FOREIGN KEY (user_uid)
          REFERENCES users(uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. courier_order_items (주문 상품)
CREATE TABLE courier_order_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    courier_order_id BIGINT UNSIGNED NOT NULL,
    courier_product_id BIGINT UNSIGNED,
    product_name VARCHAR(100) NOT NULL,
    product_price DECIMAL(12, 2) NOT NULL,
    quantity INT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    item_status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_courier_order_items_order (courier_order_id),
    KEY idx_courier_order_items_product (courier_product_id),
    CONSTRAINT fk_courier_order_items_order
      FOREIGN KEY (courier_order_id)
          REFERENCES courier_orders(id),
    CONSTRAINT fk_courier_order_items_product
      FOREIGN KEY (courier_product_id)
          REFERENCES courier_products(id)
          ON DELETE SET NULL,
    CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. courier_payments (결제)
CREATE TABLE courier_payments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    courier_order_id BIGINT UNSIGNED NOT NULL,
    pg_provider VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    tid VARCHAR(100),
    aid VARCHAR(100),
    approved_at DATETIME,
    canceled_at DATETIME,
    canceled_amount DECIMAL(12, 2),
    failed_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_courier_payments_order (courier_order_id),
    KEY idx_courier_payments_tid (tid),
    CONSTRAINT fk_courier_payments_order
      FOREIGN KEY (courier_order_id)
          REFERENCES courier_orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. shipping_fee_policies (배송비 정책)
CREATE TABLE shipping_fee_policies (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    min_quantity INT NOT NULL,
    max_quantity INT NOT NULL,
    fee DECIMAL(12, 2) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. courier_claims (CS/반품)
CREATE TABLE courier_claims (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    courier_order_id BIGINT UNSIGNED,
    courier_order_item_id BIGINT UNSIGNED,
    claim_type VARCHAR(20) NOT NULL,
    claim_status VARCHAR(20) NOT NULL,
    reason TEXT NOT NULL,
    admin_note TEXT,
    refund_amount DECIMAL(12, 2),
    reship_order_id BIGINT UNSIGNED,
    return_shipping_fee_bearer VARCHAR(20),
    resolved_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_courier_claims_order (courier_order_id),
    KEY idx_courier_claims_status (claim_status),
    CONSTRAINT fk_courier_claims_order
      FOREIGN KEY (courier_order_id)
          REFERENCES courier_orders(id),
    CONSTRAINT fk_courier_claims_item
      FOREIGN KEY (courier_order_item_id)
          REFERENCES courier_order_items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. courier_config (택배 설정 - single row)
CREATE TABLE courier_config (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    enabled TINYINT(1) NOT NULL DEFAULT 0,
    island_surcharge DECIMAL(12, 2) NOT NULL DEFAULT 3000.00,
    notice_text TEXT,
    sender_name VARCHAR(50),
    sender_phone VARCHAR(30),
    sender_phone2 VARCHAR(30),
    sender_address VARCHAR(200),
    sender_detail_address VARCHAR(200),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Seed data
INSERT INTO shipping_fee_policies (min_quantity, max_quantity, fee, sort_order, active, created_at, updated_at)
VALUES
    (1,  3,  4000.00,  1, 1, NOW(), NOW()),
    (4,  6,  8000.00,  2, 1, NOW(), NOW()),
    (7,  9,  12000.00, 3, 1, NOW(), NOW()),
    (10, 99, 16000.00, 4, 1, NOW(), NOW());

INSERT INTO courier_config (enabled, island_surcharge, notice_text, created_at, updated_at)
VALUES (0, 3000.00, NULL, NOW(), NOW());
