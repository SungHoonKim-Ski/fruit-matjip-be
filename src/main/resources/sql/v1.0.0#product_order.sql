CREATE TABLE product_order (
    id bigint unsigned PRIMARY KEY AUTO_INCREMENT,
    product_id bigint unsigned NOT NULL,
    sell_date DATE NOT NULL,
    order_index INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_selldate_order (sell_date, order_index)
);

ALTER TABLE product_order
ADD CONSTRAINT fk_productorder_product
FOREIGN KEY (product_id) REFERENCES products(id);