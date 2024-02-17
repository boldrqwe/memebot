--liquibase formatted sql

--changeset boldinegor:user
CREATE TABLE user_detail (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       phone_number VARCHAR(255) NOT NULL
);