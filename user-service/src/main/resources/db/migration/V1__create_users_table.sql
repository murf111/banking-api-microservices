CREATE TABLE IF NOT EXISTS users (
    id               BIGSERIAL PRIMARY KEY,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,
    role             VARCHAR(50)  NOT NULL,
    failed_attempts  INTEGER      NOT NULL DEFAULT 0,
    lock_time        TIMESTAMP,
    is_locked        BOOLEAN      NOT NULL DEFAULT FALSE
);