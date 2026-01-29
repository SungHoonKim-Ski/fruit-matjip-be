ALTER TABLE delivery_orders
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER status;
