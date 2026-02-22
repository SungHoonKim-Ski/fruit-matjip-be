ALTER TABLE courier_product_options ADD COLUMN stock INT NULL;
ALTER TABLE courier_order_items ADD COLUMN selected_option_ids VARCHAR(500) NULL;
ALTER TABLE courier_product_options ADD CONSTRAINT chk_option_stock_non_negative CHECK (stock IS NULL OR stock >= 0);
