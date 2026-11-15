package com.tradingdashboard.watchlist.repository;

import com.tradingdashboard.watchlist.model.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, UUID> {

    List<WatchlistItem> findByWatchlistIdOrderByAddedAtAsc(UUID watchlistId);

    boolean existsByWatchlistIdAndSymbol(UUID watchlistId, String symbol);

    long deleteByWatchlistIdAndSymbol(UUID watchlistId, String symbol);
}