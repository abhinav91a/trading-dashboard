CREATE TABLE alert_rules (
    id                UUID PRIMARY KEY,
    user_id           VARCHAR(64)   NOT NULL,
    symbol            VARCHAR(10)   NOT NULL,
    direction         VARCHAR(5)    NOT NULL CHECK (direction IN ('ABOVE', 'BELOW')),
    threshold         NUMERIC(18,4) NOT NULL CHECK (threshold > 0),
    cooldown_seconds  INTEGER       NOT NULL DEFAULT 300 CHECK (cooldown_seconds >= 0),
    active            BOOLEAN       NOT NULL DEFAULT TRUE,
    last_triggered_at TIMESTAMPTZ,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_alert_rules_symbol ON alert_rules (symbol) WHERE active;
CREATE INDEX idx_alert_rules_user ON alert_rules (user_id);