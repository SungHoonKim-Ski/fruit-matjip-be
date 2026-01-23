ALTER TABLE delivery_orders
    ADD COLUMN delivery_minute INT NOT NULL DEFAULT 0 AFTER delivery_hour;
