CREATE TABLE delivery_daily_agg (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sell_date DATE NOT NULL,
    order_count INT NOT NULL DEFAULT 0,
    quantity INT NOT NULL DEFAULT 0,
    amount DECIMAL(18,0) NOT NULL DEFAULT 0,
    delivery_fee DECIMAL(18,0) NOT NULL DEFAULT 0,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    UNIQUE KEY uq_sell_date (sell_date)
);
