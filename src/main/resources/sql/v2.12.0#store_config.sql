CREATE TABLE store_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_deadline_hour    INT NOT NULL DEFAULT 19,
    reservation_deadline_minute  INT NOT NULL DEFAULT 30,
    cancellation_deadline_hour   INT NOT NULL DEFAULT 19,
    cancellation_deadline_minute INT NOT NULL DEFAULT 0,
    pickup_deadline_hour         INT NOT NULL DEFAULT 20,
    pickup_deadline_minute       INT NOT NULL DEFAULT 0,
    created_at DATETIME(6),
    updated_at DATETIME(6)
);
