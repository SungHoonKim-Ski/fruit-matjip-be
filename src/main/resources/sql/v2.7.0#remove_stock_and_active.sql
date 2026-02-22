-- v2.7.0: stock, active 컬럼 제거 + sold_out 추가
-- 택배 상품의 stock 컬럼 제거
ALTER TABLE courier_products DROP COLUMN stock;

-- 택배 상품에 sold_out 컬럼 추가
ALTER TABLE courier_products ADD COLUMN sold_out TINYINT(1) NOT NULL DEFAULT 0 AFTER visible;

-- 배송 정책 템플릿의 active 컬럼 제거
ALTER TABLE shipping_fee_templates DROP COLUMN active;

-- 수량별 배송비의 active 컬럼 제거
ALTER TABLE shipping_fee_policies DROP COLUMN active;

-- 택배 상품 상세 이미지 테이블 삭제
DROP TABLE IF EXISTS courier_product_detail_images;
