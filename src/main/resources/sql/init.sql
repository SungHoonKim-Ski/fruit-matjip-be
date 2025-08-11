CREATE TABLE users (
       id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
       social_id        VARCHAR(255) NOT NULL,              -- 소셜id
       internal_uid     CHAR(36) NOT NULL,              -- 내부고유id
       nickname         VARCHAR(255)  NOT NULL,
       joined_date      DATE NOT NULL,                      -- 가입일
       last_order_date  DATE NULL,                          -- 최근주문일
       total_orders  BIGINT NOT NULL DEFAULT 0,          -- 누적주문량
       created_at       DATETIME NOT NULL,
       updated_at       DATETIME NOT NULL,
       deleted_at       DATETIME,
       PRIMARY KEY (id),
       UNIQUE KEY uq_users_social_id   (social_id),
       UNIQUE KEY uq_users_internal_uid(internal_uid),
       UNIQUE KEY uq_users_nickname    (nickname),
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
      image_url         TEXT NULL,
      name              VARCHAR(255) NOT NULL,
      stock             INT NOT NULL DEFAULT 0,
      price             INT NOT NULL,
      sell_date         DATE NOT NULL,
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

CREATE TABLE product_details (
     id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
     product_id  BIGINT UNSIGNED NOT NULL,
     description TEXT NOT NULL,
     created_at        DATETIME NOT NULL,
     updated_at        DATETIME NOT NULL,
     deleted_at        DATETIME,
     PRIMARY KEY (id),
     UNIQUE KEY idx_pd_product (product_id),
     CONSTRAINT fk_pd_product
         FOREIGN KEY (product_id)
             REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_detail_images (
    id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_detail_id  BIGINT UNSIGNED NOT NULL,
    url                TEXT NOT NULL,
    created_at        DATETIME NOT NULL,
    updated_at        DATETIME NOT NULL,
    deleted_at        DATETIME,
    PRIMARY KEY (id),
    KEY idx_pdi_detail (product_detail_id),
    CONSTRAINT fk_pdi_detail
       FOREIGN KEY (product_detail_id)
           REFERENCES product_details(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reservations (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id           BIGINT UNSIGNED NOT NULL,
    order_date        DATE NOT NULL,
    product_id        BIGINT UNSIGNED NOT NULL,
    quantity          INT NOT NULL,
    amount            INT NOT NULL,
    status            ENUM('PENDING','CANCELED','PICKED') NOT NULL DEFAULT 'PENDING',
    status_changed_at DATETIME NOT NULL,
    created_at        DATETIME NOT NULL,
    updated_at        DATETIME NOT NULL,
    deleted_at        DATETIME,
    PRIMARY KEY (id),
    KEY idx_res_user_date (user_id, order_date),
    KEY idx_res_status    (status, status_changed_at),
    CONSTRAINT fk_res_user
      FOREIGN KEY (user_id)
          REFERENCES users(id)
          ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_res_product
      FOREIGN KEY (product_id)
          REFERENCES products(id)
          ON DELETE RESTRICT ON UPDATE CASCADE,
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

CREATE TABLE nickname_pool (
   id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
   base_name    VARCHAR(50) NOT NULL,
   next_seq     INT NOT NULL DEFAULT 1,
   PRIMARY KEY (id),
   CHECK (next_seq >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO nickname_pool(base_name)
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