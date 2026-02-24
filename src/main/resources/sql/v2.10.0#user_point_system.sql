ALTER TABLE users ADD COLUMN point_balance DECIMAL(12,2) NOT NULL DEFAULT 0;

CREATE TABLE user_point_transactions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    balance_after DECIMAL(12,2) NOT NULL,
    description VARCHAR(100) NOT NULL,
    reference_type VARCHAR(30) NULL,
    reference_id BIGINT NULL,
    created_by VARCHAR(50) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_upt_user_created (user_id, created_at DESC),
    INDEX idx_upt_ref (reference_type, reference_id)
);
