--liquibase formatted sql

--changeset boldinegor:1
CREATE TABLE media_file
(
    id         BIGSERIAL PRIMARY KEY,
    file_path  text        NOT NULL,
    file_type  VARCHAR(50) NOT NULL,
    comment    TEXT,
    file_url   text,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
--changeset boldinegor:2
alter table media_file add column tread_downloaded boolean default false;
--changeset boldinegor:3
alter table media_file add column parent_id bigint;
