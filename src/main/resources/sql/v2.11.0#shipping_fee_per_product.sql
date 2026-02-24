-- v2.11.0 : 배송비를 템플릿 방식에서 상품별 수량 기반으로 변경
-- 공식: ceil(주문수량 / combinedShippingQuantity) × shippingFee

-- 1. courier_products: 템플릿 FK 제거
ALTER TABLE courier_products DROP FOREIGN KEY fk_cp_shipping_template;
ALTER TABLE courier_products DROP COLUMN shipping_fee_template_id;

-- 2. courier_products: combined_shipping_fee 제거
ALTER TABLE courier_products DROP COLUMN combined_shipping_fee;

-- 3. courier_products: 상품별 배송비 / 합배송 수량 추가
ALTER TABLE courier_products
    ADD COLUMN shipping_fee DECIMAL(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN combined_shipping_quantity INT NOT NULL DEFAULT 1;

-- 4. courier_config: base_shipping_fee 제거
ALTER TABLE courier_config DROP COLUMN base_shipping_fee;

-- 5. shipping_fee_templates 테이블 삭제
DROP TABLE IF EXISTS shipping_fee_templates;
