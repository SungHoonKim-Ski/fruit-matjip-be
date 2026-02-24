ALTER TABLE courier_claims
    ADD COLUMN point_amount DECIMAL(12,2) NULL,
    ADD COLUMN return_status VARCHAR(20) NOT NULL DEFAULT 'NONE';
