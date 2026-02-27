-- Backfill delivery_daily_agg from existing DELIVERED delivery orders
-- Safe to re-run: uses ON DUPLICATE KEY UPDATE
INSERT INTO delivery_daily_agg (sell_date, order_count, quantity, amount, delivery_fee, created_at, updated_at)
SELECT
    do2.delivery_date,
    COUNT(DISTINCT do2.id),
    COALESCE(SUM(r.quantity), 0),
    COALESCE(SUM(r.amount), 0),
    (SELECT COALESCE(SUM(d.delivery_fee), 0)
     FROM delivery_orders d
     WHERE d.status = 'DELIVERED'
       AND d.delivery_date = do2.delivery_date),
    NOW(6),
    NOW(6)
FROM delivery_orders do2
         JOIN delivery_order_reservations dor ON dor.delivery_order_id = do2.id
         JOIN reservations r ON r.id = dor.reservation_id
WHERE do2.status = 'DELIVERED'
GROUP BY do2.delivery_date
ON DUPLICATE KEY UPDATE
    order_count   = VALUES(order_count),
    quantity      = VALUES(quantity),
    amount        = VALUES(amount),
    delivery_fee  = VALUES(delivery_fee),
    updated_at    = NOW(6);
