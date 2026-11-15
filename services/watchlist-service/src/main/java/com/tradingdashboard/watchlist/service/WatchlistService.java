package com.tradingdashboard.watchlist.service;

import com.tradingdashboard.watchlist.model.Watchlist;
import com.tradingdashboard.watchlist.model.WatchlistItem;
import com.tradingdashboard.watchlist.repository.WatchlistItemRepository;
import com.tradingdashboard.watchlist.repository.WatchlistRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Service
public class WatchlistService {

    private static final String CACHE = "watchlistViews";

    // Cache key includes the owner, so a cache hit can't skip the ownership check for another user
    private static final String KEY = "#userId + ':' + #watchlistId.toString()";

    public record WatchlistView(UUID id, String name, List<String> symbols) implements Serializable {
    }

    private final WatchlistRepository watchlists;
    private final WatchlistItemRepository items;

    public WatchlistService(WatchlistRepository watchlists, WatchlistItemRepository items) {
        this.watchlists = watchlists;
        this.items = items;
    }

    public List<Watchlist> listForUser(String userId) {
        return watchlists.findByUserIdOrderByNameAsc(userId);
    }

    @Transactional
    public Watchlist create(String userId, String name) {
        if (watchlists.existsByUserIdAndName(userId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A watchlist with that name already exists");
        }
        return watchlists.save(new Watchlist(userId, name));
    }

    @Cacheable(cacheNames = CACHE, key = KEY)
    public WatchlistView getView(String userId, UUID watchlistId) {
        Watchlist watchlist = requireOwned(userId, watchlistId);
        List<String> symbols = items.findByWatchlistIdOrderByAddedAtAsc(watchlistId).stream()
                .map(WatchlistItem::getSymbol)
                .toList();
        return new WatchlistView(watchlist.getId(), watchlist.getName(), symbols);
    }

    @Transactional
    @CacheEvict(cacheNames = CACHE, key = KEY)
    public WatchlistItem addSymbol(String userId, UUID watchlistId, String symbol) {
        requireOwned(userId, watchlistId);
        String normalized = symbol.trim().toUpperCase();
        if (items.existsByWatchlistIdAndSymbol(watchlistId, normalized)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, normalized + " is already in this watchlist");
        }
        return items.save(new WatchlistItem(watchlistId, normalized));
    }

    @Transactional
    @CacheEvict(cacheNames = CACHE, key = KEY)
    public void removeSymbol(String userId, UUID watchlistId, String symbol) {
        requireOwned(userId, watchlistId);
        long removed = items.deleteByWatchlistIdAndSymbol(watchlistId, symbol.trim().toUpperCase());
        if (removed == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Symbol not in this watchlist");
        }
    }

    @Transactional
    @CacheEvict(cacheNames = CACHE, key = KEY)
    public void delete(String userId, UUID watchlistId) {
        // The database deletes the items too, because of ON DELETE CASCADE in V1
        watchlists.delete(requireOwned(userId, watchlistId));
    }

    private Watchlist requireOwned(String userId, UUID watchlistId) {
        return watchlists.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watchlist not found"));
    }
}