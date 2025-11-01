alter table users
    add total_revenue decimal(10) not null default(0);

UPDATE users t1
    LEFT JOIN (
    SELECT user_uid, SUM(amount) AS total_revenue
    FROM reservations
    WHERE status IN ('SELF_PICK_READY', 'PICKED')
    GROUP BY user_uid
    ) t2 ON t1.uid = t2.user_uid
    SET t1.total_revenue = IFNULL(t2.total_revenue, 0)
WHERE t1.total_revenue = 0;
