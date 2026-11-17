# Trading Dashboard

A microservices trading dashboard with live US stock prices from Alpaca, portfolio tracking, watchlists, price alerts, and candlestick charts.

**Stack:** Java 21, Spring Boot 4, Apache Kafka, PostgreSQL, Redis, React, Docker

## Features

- Live prices streamed from Alpaca through Kafka to the browser over WebSocket
- Portfolios with average-cost accounting, market value, and unrealized P&L
- Per-user watchlists cached in Redis
- Price alerts above or below a threshold, with a cooldown per rule
- Candlestick charts with 1 week to 1 year ranges
- Login with BCrypt passwords and JWT tokens. Each user sees only their own data.

## Architecture

| Service | Port | Role |
|---|---|---|
| market-data | 8081 | Streams Alpaca trades to Kafka, serves symbols, snapshots, and bars |
| portfolio | 8082 | Portfolios, trades, positions, P&L (Postgres) |
| watchlist | 8083 | Symbol lists per user (Postgres, Redis cache) |
| alert | 8084 | Alert rules, checks every tick, publishes triggered alerts (Postgres) |
| auth | 8085 | Registration, login, JWT issuance (Postgres) |
| api-gateway | 8080 | WebSocket feed, filtered per user |
| frontend | 5173 | React dashboard served by nginx, which proxies the API |
