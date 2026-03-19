CREATE TABLE IF NOT EXISTS transactions (
    id                     BIGSERIAL     PRIMARY KEY,
    source_account_id      BIGINT        NOT NULL,
    destination_account_id BIGINT        NOT NULL,
    amount                 NUMERIC(19,2) NOT NULL,
    status                 VARCHAR(50)   NOT NULL,
    timestamp              TIMESTAMP     NOT NULL
);

CREATE INDEX idx_transactions_source      ON transactions (source_account_id);
CREATE INDEX idx_transactions_destination ON transactions (destination_account_id);
CREATE INDEX idx_transactions_timestamp   ON transactions (timestamp);