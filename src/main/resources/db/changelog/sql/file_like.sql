--liquibase formatted sql

--changeset boldinegor:1
CREATE TABLE IF NOT EXISTS file_like
(
    id BIGSERIAL NOT NULL CONSTRAINT file_like_pkey PRIMARY KEY,
    cool_file_id BIGINT  CONSTRAINT fk_cool_file_id  REFERENCES cool_file (id),
    media_file_id BIGINT CONSTRAINT fk_media_file_id  REFERENCES media_file (id),
    user_id BIGINT,
    telegram_user_id TEXT,
    like_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_id CHECK ((user_id IS NOT NULL AND telegram_user_id IS NULL) OR (user_id IS NULL AND telegram_user_id IS NOT NULL))
    );
