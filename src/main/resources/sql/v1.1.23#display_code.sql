-- 1) 컬럼 추가
ALTER TABLE reservations ADD COLUMN display_code VARCHAR(18) NULL;
ALTER TABLE delivery_orders ADD COLUMN display_code VARCHAR(18) NULL;

-- 2) 기존 데이터 backfill (prefix + id)
UPDATE reservations SET display_code = CONCAT('R-', id);
UPDATE delivery_orders SET display_code = CONCAT('D-', id);

-- 3) 기존 미결제 배달 주문 안전 정리 (KakaoPay partnerOrderId 불일치 방지)
UPDATE delivery_orders SET status = 'CANCELED'
    WHERE status = 'PENDING_PAYMENT';

-- 4) NOT NULL + UNIQUE INDEX
ALTER TABLE reservations MODIFY COLUMN display_code VARCHAR(18) NOT NULL;
ALTER TABLE delivery_orders MODIFY COLUMN display_code VARCHAR(18) NOT NULL;

CREATE UNIQUE INDEX uix_reservations_display_code
    ON reservations (display_code);
CREATE UNIQUE INDEX uix_delivery_orders_display_code
    ON delivery_orders (display_code);
