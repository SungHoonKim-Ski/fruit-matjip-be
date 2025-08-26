CREATE TABLE user_reservation_logs (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    created_at      DATETIME        NOT NULL,
    user_uid        CHAR(36)        NOT NULL,
    reservation_id  BIGINT UNSIGNED NOT NULL,
    action          VARCHAR(40)     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_user_reservation_logs_user (user_uid),
    KEY idx_user_reservation_logs_reservation (reservation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;