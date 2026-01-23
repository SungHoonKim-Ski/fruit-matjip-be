CREATE TABLE user_delivery_info (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_uid CHAR(36) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    address1 VARCHAR(200) NOT NULL,
    address2 VARCHAR(200),
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_delivery_info_user (user_uid),
    CONSTRAINT fk_user_delivery_info_user
      FOREIGN KEY (user_uid)
          REFERENCES users(uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE delivery_orders (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_uid CHAR(36) NOT NULL,
    reservation_id BIGINT UNSIGNED NOT NULL,
    status ENUM('PENDING_PAYMENT','PAID','OUT_FOR_DELIVERY','DELIVERED','CANCELED','FAILED') NOT NULL,
    delivery_date DATE NOT NULL,
    delivery_hour INT NOT NULL,
    delivery_fee DECIMAL(12,2) NOT NULL,
    distance_km DECIMAL(6,3) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    address1 VARCHAR(200) NOT NULL,
    address2 VARCHAR(200),
    phone VARCHAR(30) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    kakao_tid VARCHAR(100),
    paid_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_delivery_orders_reservation (reservation_id),
    KEY idx_delivery_orders_date (delivery_date),
    CONSTRAINT fk_delivery_orders_user
      FOREIGN KEY (user_uid)
          REFERENCES users(uid),
    CONSTRAINT fk_delivery_orders_reservation
      FOREIGN KEY (reservation_id)
          REFERENCES reservations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE delivery_orders
    DROP FOREIGN KEY fk_delivery_orders_reservation,
    DROP INDEX uq_delivery_orders_reservation,
    DROP COLUMN reservation_id;

CREATE TABLE delivery_order_reservations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    delivery_order_id BIGINT UNSIGNED NOT NULL,
    reservation_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_delivery_order_reservation_reservation (reservation_id),
    KEY idx_delivery_order_reservations_order (delivery_order_id),
    CONSTRAINT fk_delivery_order_reservations_order
      FOREIGN KEY (delivery_order_id)
          REFERENCES delivery_orders(id),
    CONSTRAINT fk_delivery_order_reservations_reservation
      FOREIGN KEY (reservation_id)
          REFERENCES reservations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE delivery_payments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    delivery_order_id BIGINT UNSIGNED NOT NULL,
    pg_provider ENUM('KAKAOPAY') NOT NULL,
    status ENUM('READY','APPROVED','CANCELED','FAILED') NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    tid VARCHAR(100),
    aid VARCHAR(100),
    approved_at DATETIME,
    canceled_at DATETIME,
    failed_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_delivery_payments_order (delivery_order_id),
    KEY idx_delivery_payments_tid (tid),
    CONSTRAINT fk_delivery_payments_order
      FOREIGN KEY (delivery_order_id)
          REFERENCES delivery_orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
