package com.tradingdashboard.watchlist.api;

import com.tradingdashboard.watchlist.model.Watchlist;
import com.tradingdashboard.watchlist.model.WatchlistItem;
import com.tradingdashboard.watchlist.service.WatchlistService;
import com.tradingdashboard.watchlist.service.WatchlistService.WatchlistView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/watchlists")
public class WatchlistController {

    public record CreateWatchlistRequest(@NotBlank @Size(max = 100) String name) {
    }

    public record AddSymbolRequest(@NotBlank @Size(max = 10) String symbol) {
    }

    private final WatchlistService service;

    public WatchlistController(WatchlistService service) {
        this.service = service;
    }

    @GetMapping
    public List<Watchlist> list(@AuthenticationPrincipal String userId) {
        return service.listForUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Watchlist create(@AuthenticationPrincipal String userId,
                            @Valid @RequestBody CreateWatchlistRequest request) {
        return service.create(userId, request.name());
    }

    @GetMapping("/{id}")
    public WatchlistView get(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        return service.getView(userId, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        service.delete(userId, id);
    }

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public WatchlistItem addSymbol(@AuthenticationPrincipal String userId,
                                   @PathVariable UUID id,
                                   @Valid @RequestBody AddSymbolRequest request) {
        return service.addSymbol(userId, id, request.symbol());
    }

    @DeleteMapping("/{id}/items/{symbol}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSymbol(@AuthenticationPrincipal String userId,
                             @PathVariable UUID id,
                             @PathVariable String symbol) {
        service.removeSymbol(userId, id, symbol);
    }
}