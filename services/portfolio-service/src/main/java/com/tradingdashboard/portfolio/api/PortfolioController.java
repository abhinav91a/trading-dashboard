package com.tradingdashboard.portfolio.api;

import com.tradingdashboard.portfolio.model.Portfolio;
import com.tradingdashboard.portfolio.model.Trade;
import com.tradingdashboard.portfolio.service.PortfolioService;
import com.tradingdashboard.portfolio.service.PortfolioService.PortfolioView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    public record CreatePortfolioRequest(@NotBlank @Size(max = 100) String name) {
    }

    public record TradeRequest(
            @NotBlank @Size(max = 10) String symbol,
            @NotNull Trade.Side side,
            @NotNull @Positive BigDecimal quantity,
            @NotNull @Positive BigDecimal price) {
    }

    private final PortfolioService service;

    public PortfolioController(PortfolioService service) {
        this.service = service;
    }

    @GetMapping
    public List<Portfolio> list(@AuthenticationPrincipal String userId) {
        return service.listForOwner(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Portfolio create(@AuthenticationPrincipal String userId,
                            @Valid @RequestBody CreatePortfolioRequest request) {
        return service.createPortfolio(userId, request.name());
    }

    @PostMapping("/{id}/trades")
    @ResponseStatus(HttpStatus.CREATED)
    public Trade trade(@AuthenticationPrincipal String userId,
                       @PathVariable UUID id,
                       @Valid @RequestBody TradeRequest request) {
        return service.recordTrade(userId, id, request.symbol(), request.side(),
                request.quantity(), request.price());
    }

    @GetMapping("/{id}")
    public PortfolioView get(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        return service.getPortfolio(userId, id);
    }
}
