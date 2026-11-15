package com.tradingdashboard.watchlist.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "watchlist_items")
public class WatchlistItem {

    @Id
    private UUID id;

    @Column(name = "watchlist_id", nullable = false)
    private UUID watchlistId;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    protected WatchlistItem() {
    }

    public WatchlistItem(UUID watchlistId, String symbol) {
        this.id = UUID.randomUUID();
        this.watchlistId = watchlistId;
        this.symbol = symbol;
        this.addedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getWatchlistId() {
        return watchlistId;
    }

    public String getSymbol() {
        return symbol;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}