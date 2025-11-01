CREATE TABLE message_template (
    id           BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    message_type VARCHAR(50) NOT NULL UNIQUE,
    title        VARCHAR(255) NOT NULL,
    body         TEXT NOT NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at   DATETIME NULL
    );

    CREATE TABLE user_message_queue (
    id           BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_uid     CHAR(36) NOT NULL,
    message_template_id  BIGINT UNSIGNED NOT NULL,
    status       ENUM('PENDING', 'SENT', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    valid_from   DATETIME NULL,
    valid_until  DATETIME NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at      DATETIME NULL,
    received_at  DATETIME NULL,

    CONSTRAINT fk_umq_user
        FOREIGN KEY (user_uid) REFERENCES users(uid),
    CONSTRAINT fk_umq_template
        FOREIGN KEY (message_template_id) REFERENCES message_template(id)
);
CREATE INDEX idx_umq_user_status ON user_message_queue (user_uid, status);

INSERT INTO message_template(title, body, message_type)
values ('노쇼 경고 안내',
        '항상 이용해주셔서 감사합니다.
예약 후 <b>노쇼가 여러 건 발생</b>할 경우,
추후 과일맛집 예약 <b>사이트 이용에 제한</b>이 있을 수 있습니다.',
        'USER_NO_SHOW');