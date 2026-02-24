-- v2.11.2 : 택배 주문에 배송 추적 위치/시각 추가
ALTER TABLE courier_orders
    ADD COLUMN tracking_location VARCHAR(200) NULL,
    ADD COLUMN tracking_updated_at DATETIME NULL;
