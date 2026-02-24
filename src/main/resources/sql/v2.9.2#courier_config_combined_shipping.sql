ALTER TABLE courier_config
    ADD COLUMN combined_shipping_enabled TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN combined_shipping_max_quantity INT NOT NULL DEFAULT 1;
