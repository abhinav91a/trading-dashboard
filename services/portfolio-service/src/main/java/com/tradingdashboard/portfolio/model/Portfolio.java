package com.tradingdashboard.portfolio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "portfolios")
public class Portfolio {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false, length = 64)
    private String ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Portfolio() {
    }

    public Portfolio(String ownerId, String name) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.name = name;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
