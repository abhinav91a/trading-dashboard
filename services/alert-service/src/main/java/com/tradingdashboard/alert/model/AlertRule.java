package com.tradingdashboard.alert.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_rules")
public class AlertRule {

    public enum Direction {
        ABOVE, BELOW
    }

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Direction direction;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal threshold;

    @Column(name = "cooldown_seconds", nullable = false)
    private int cooldownSeconds;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "last_triggered_at")
    private Instant lastTriggeredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AlertRule() {
    }

    public AlertRule(String userId, String symbol, Direction direction,
                     BigDecimal threshold, int cooldownSeconds) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.symbol = symbol;
        this.direction = direction;
        this.threshold = threshold;
        this.cooldownSeconds = cooldownSeconds;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getSymbol() {
        return symbol;
    }

    public Direction getDirection() {
        return direction;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
