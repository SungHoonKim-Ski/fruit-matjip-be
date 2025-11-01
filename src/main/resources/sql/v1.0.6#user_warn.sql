CREATE TABLE user_warn (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_uid CHAR(36) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_warn__user
    FOREIGN KEY (user_uid) REFERENCES users(uid)
);

CREATE INDEX idx_user_warn__user_uid ON user_warn(user_uid);