CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    birthday DATE,
    role_id BIGINT,
    FOREIGN KEY (role_id) REFERENCES roles(id)
    );

MERGE INTO roles (id, name) VALUES (1, 'ADMIN');
MERGE INTO roles (id, name) VALUES (2, 'USER');