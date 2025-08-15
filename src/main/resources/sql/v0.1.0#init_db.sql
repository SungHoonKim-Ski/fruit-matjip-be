CREATE TABLE users (
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    social_id        VARCHAR(255) NOT NULL,
    uid     CHAR(36) NOT NULL,
    name         VARCHAR(255)  NOT NULL,
    last_order_date  DATE NULL,
    total_orders  BIGINT NOT NULL DEFAULT 0,
    created_at       DATETIME NOT NULL,
    updated_at       DATETIME NOT NULL,
    deleted_at       DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_social_id   (social_id),
    UNIQUE KEY uq_users_internal_uid(internal_uid),
    UNIQUE KEY uq_users_name    (name),
    CHECK (total_orders >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admins (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name           VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    password       VARCHAR(255) NOT NULL,
    role           ENUM('MANAGER','OWNER','NONE') NOT NULL DEFAULT 'NONE',
    created_at     DATETIME NOT NULL,
    updated_at     DATETIME NOT NULL,
    deleted_at       DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uq_admins_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE products (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_url         TEXT NULL,
    name              VARCHAR(255) NOT NULL,
    stock             INT NOT NULL DEFAULT 0,
    price             INT NOT NULL,
    sell_date         DATE NOT NULL,
    description       TEXT,
    is_visible        TINYINT(1) DEFAULT 1,
    total_sold        BIGINT NOT NULL DEFAULT 0,
    registered_admin  BIGINT UNSIGNED NOT NULL,
    created_at        DATETIME NOT NULL,
    updated_at        DATETIME NOT NULL,
    deleted_at       DATETIME,
    PRIMARY KEY (id),
    KEY idx_products_sell_date (sell_date),
    KEY idx_products_visible   (is_visible),
    CONSTRAINT fk_products_admin
      FOREIGN KEY (registered_admin)
          REFERENCES admins(id),
    CHECK (total_sold >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_detail_images (
    id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_id  BIGINT UNSIGNED NOT NULL,
    detail_url                TEXT NOT NULL,
    created_at        DATETIME NOT NULL,
    updated_at        DATETIME NOT NULL,
    deleted_at        DATETIME,
    PRIMARY KEY (id),
    KEY idx_pdi_detail (product_id),
    CONSTRAINT fk_pdi_detail
       FOREIGN KEY (product_id)
           REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reservations (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_uid          CHAR(36) NOT NULL,
    order_date        DATE NOT NULL,
    product_id        BIGINT UNSIGNED NOT NULL,
    quantity          INT NOT NULL,
    amount            DECIMAL NOT NULL,
    status            ENUM('PENDING','CANCELED','PICKED') NOT NULL DEFAULT 'PENDING',
    status_changed_at DATETIME NOT NULL,
    created_at        DATETIME NOT NULL,
    updated_at        DATETIME NOT NULL,
    deleted_at        DATETIME,
    PRIMARY KEY (id),
    KEY idx_res_user_date (user_uid, order_date),
    KEY idx_res_status    (status, status_changed_at),
    CONSTRAINT fk_res_user
      FOREIGN KEY (user_uid)
          REFERENCES users(internal_uid),
    CONSTRAINT fk_res_product
      FOREIGN KEY (product_id)
          REFERENCES products(id),
    CHECK (quantity >= 0),
    CHECK (amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admin_product_logs (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    admin_id    BIGINT UNSIGNED NOT NULL,
    product_id  BIGINT UNSIGNED NOT NULL,
    action      VARCHAR(255) NOT NULL,
    created_at  DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_apl_product (product_id),
    KEY idx_apl_admin   (admin_id),
    CONSTRAINT fk_apl_admin
        FOREIGN KEY (admin_id)
            REFERENCES admins(id),
    CONSTRAINT fk_apl_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admin_logs (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    admin_id    BIGINT UNSIGNED NULL,
    request_api VARCHAR(255) NOT NULL,
    created_at  DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_al_admin (admin_id),
    CONSTRAINT fk_al_admin
        FOREIGN KEY (admin_id)
            REFERENCES admins(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE name_pool (
   id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
   base_name    VARCHAR(255) NOT NULL,
   next_seq     INT NOT NULL DEFAULT 1,
   PRIMARY KEY (id),
   CHECK (next_seq >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO name_pool(base_name)
VALUES
    ('포도'),
    ('사과'),
    ('키위'),
    ('바나나'),
    ('복숭아'),
    ('딸기'),
    ('수박'),
    ('자몽'),
    ('망고');

CREATE TABLE user_name_logs (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name_before       VARCHAR(255) NOT NULL,
    name_after        VARCHAR(255) NOT NULL,
    user_uid      CHAR(36) NULL,
    created_at   DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
) ENGINE=InnoDB;


CREATE TABLE SPRING_SESSION_ATTRIBUTES (
   SESSION_PRIMARY_ID CHAR(36) NOT NULL,
   ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
   ATTRIBUTE_BYTES BLOB NOT NULL,
   CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
   CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID)
) ENGINE=InnoDB;

CREATE TABLE refresh_tokens (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_uid   CHAR(36) NOT NULL,
    token_hash  CHAR(64) NOT NULL,
    issued_at   TIMESTAMP NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    revoked     TINYINT(1) NOT NULL DEFAULT 0,
    replaced_by CHAR(64) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_token (token_hash),
    KEY idx_user_exp (user_uid, expires_at),
    KEY idx_revoked (revoked, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;