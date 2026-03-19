CREATE TABLE IF NOT EXISTS accounts (
    id             BIGSERIAL     PRIMARY KEY,
    user_id        BIGINT        NOT NULL,
    account_number BIGINT        NOT NULL UNIQUE,
    balance        NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    currency       VARCHAR(10)   NOT NULL,
    status         VARCHAR(50)   NOT NULL,
    version        INTEGER       NOT NULL DEFAULT 0
);

CREATE INDEX idx_accounts_user_id ON accounts (user_id);