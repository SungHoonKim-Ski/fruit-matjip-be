create table delivery_config (
    id bigint auto_increment primary key,
    enabled boolean not null,
    max_distance_km double not null,
    fee_distance_km double not null,
    min_amount decimal(12, 2) not null,
    fee_near decimal(12, 2) not null,
    fee_per_100m decimal(12, 2) not null,
    created_at datetime(6),
    updated_at datetime(6),
    start_hour int not null,
    start_minute int not null,
    end_hour int not null,
    end_minute int not null
);
