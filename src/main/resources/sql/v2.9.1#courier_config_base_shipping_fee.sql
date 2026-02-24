ALTER TABLE courier_config
    ADD COLUMN base_shipping_fee DECIMAL(12, 2) NOT NULL DEFAULT 3000;
