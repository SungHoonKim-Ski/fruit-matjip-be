ALTER TABLE courier_products ADD COLUMN combined_shipping_fee DECIMAL(12,2) NULL;
ALTER TABLE courier_config DROP COLUMN combined_shipping_enabled;
ALTER TABLE courier_config DROP COLUMN combined_shipping_max_quantity;
