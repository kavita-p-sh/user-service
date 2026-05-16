DROP DATABASE IF EXISTS user_service_db;
CREATE DATABASE user_service_db;
USE user_service_db;

CREATE TABLE IF NOT EXISTS tb_roles (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tb_users (
    user_id CHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role_id BIGINT NOT NULL,
    phone_number VARCHAR(100) NOT NULL UNIQUE,

    created_by VARCHAR(50),
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_roles
        FOREIGN KEY (role_id)
        REFERENCES tb_roles(role_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

INSERT INTO tb_roles (role_name)
VALUES ('USER')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

INSERT INTO tb_roles (role_name)
VALUES ('ADMIN')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

INSERT INTO tb_roles (role_name)
VALUES ('MANAGER')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);