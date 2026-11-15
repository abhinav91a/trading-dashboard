CREATE TABLE watchlists (
    id          UUID PRIMARY KEY,
    user_id     VARCHAR(64)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_watchlists_user_name UNIQUE (user_id, name)
);

CREATE TABLE watchlist_items (
    id            UUID PRIMARY KEY,
    watchlist_id  UUID         NOT NULL REFERENCES watchlists (id) ON DELETE CASCADE,
    symbol        VARCHAR(10)  NOT NULL,
    added_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_watchlist_items_symbol UNIQUE (watchlist_id, symbol)
);

CREATE INDEX idx_watchlists_user ON watchlists (user_id);