CREATE TABLE portfolios (
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE holdings (
    id           UUID PRIMARY KEY,
    portfolio_id UUID          NOT NULL REFERENCES portfolios (id),
    symbol       VARCHAR(10)   NOT NULL,
    quantity     NUMERIC(18,6) NOT NULL CHECK (quantity >= 0),
    average_cost NUMERIC(18,4) NOT NULL CHECK (average_cost >= 0),
    CONSTRAINT uq_holdings_portfolio_symbol UNIQUE (portfolio_id, symbol)
);

CREATE TABLE trades (
    id           UUID PRIMARY KEY,
    portfolio_id UUID          NOT NULL REFERENCES portfolios (id),
    symbol       VARCHAR(10)   NOT NULL,
    side         VARCHAR(4)    NOT NULL CHECK (side IN ('BUY', 'SELL')),
    quantity     NUMERIC(18,6) NOT NULL CHECK (quantity > 0),
    price        NUMERIC(18,4) NOT NULL CHECK (price > 0),
    executed_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_trades_portfolio ON trades (portfolio_id);