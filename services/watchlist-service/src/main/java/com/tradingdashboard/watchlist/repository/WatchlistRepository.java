package com.tradingdashboard.watchlist.repository;

import com.tradingdashboard.watchlist.model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WatchlistRepository extends JpaRepository<Watchlist, UUID> {

    List<Watchlist> findByUserIdOrderByNameAsc(String userId);

    Optional<Watchlist> findByIdAndUserId(UUID id, String userId);

    boolean existsByUserIdAndName(String userId, String name);
}