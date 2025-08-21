alter table reservations
    modify status enum ('PENDING', 'CANCELED', 'PICKED', 'SELF_PICK') default 'PENDING' not null;

alter table users
    ADD COLUMN warn_count SMALLINT NOT NULL DEFAULT 0;

alter table reservations
    change order_date pickup_date date not null;