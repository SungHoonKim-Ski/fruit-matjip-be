CREATE TABLE product_daily_agg (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sell_date   DATE            NOT NULL,
    product_id  BIGINT UNSIGNED NOT NULL,
    quantity    INT             NOT NULL DEFAULT 0,
    amount      DECIMAL(18,0)   NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_sell_product (sell_date, product_id),
    KEY idx_product_date (product_id, sell_date)
) CHARSET=utf8mb4;

CREATE TABLE agg_applied (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    batch_uid CHAR(36) NULL,
    reservation_id BIGINT NOT NULL,
    phase ENUM('PICKED_PLUS','SELF_PICKUP_READY_PLUS','NO_SHOW_MINUS') NOT NULL,
    processed TINYINT(1) NOT NULL DEFAULT 0,
    processed_at DATETIME NULL,
    UNIQUE KEY uq_res_phase (reservation_id, phase),
    KEY idx_batch_phase (batch_uid, phase),
    KEY idx_reservation (reservation_id),
    KEY idx_processed (processed),
    KEY idx_processed_at (processed_at),
    KEY idx_created_at (created_at)
) CHARSET=utf8mb4;