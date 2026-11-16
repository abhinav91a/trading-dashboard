ALTER TABLE portfolios ADD COLUMN owner_id VARCHAR(64);
CREATE INDEX idx_portfolios_owner ON portfolios (owner_id);
