-- v2.11.1 : 택배 주문 상태 변경 (발주 기반)
-- PREPARING → ORDERING (관리자: 발주중, 고객: 상품준비중)
-- SHIPPED → ORDER_COMPLETED (관리자: 발주완료, 고객: 상품준비완료)

-- 1. 기존 상태값 마이그레이션
UPDATE courier_orders SET status = 'ORDERING' WHERE status = 'PREPARING';
UPDATE courier_orders SET status = 'ORDER_COMPLETED' WHERE status = 'SHIPPED';

-- 2. waybill_downloaded_at 컬럼 제거
ALTER TABLE courier_orders DROP COLUMN waybill_downloaded_at;
