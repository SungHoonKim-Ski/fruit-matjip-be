alter table delivery_orders add column idempotency_key varchar(64);
create unique index uq_delivery_orders_user_idempotency on delivery_orders (user_uid, idempotency_key);