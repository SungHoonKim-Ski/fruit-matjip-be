-- v2.7.0: stock, active 컬럼 제거
-- 택배 상품의 stock 컬럼 제거
ALTER TABLE courier_products DROP COLUMN stock;

-- 배송 정책 템플릿의 active 컬럼 제거
ALTER TABLE shipping_fee_templates DROP COLUMN active;

-- 수량별 배송비의 active 컬럼 제거
ALTER TABLE shipping_fee_policies DROP COLUMN active;
