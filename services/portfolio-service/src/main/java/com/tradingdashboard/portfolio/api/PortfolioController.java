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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Portfolio create(@Valid @RequestBody CreatePortfolioRequest request) {
        return service.createPortfolio(request.name());
    }

    @PostMapping("/{id}/trades")
    @ResponseStatus(HttpStatus.CREATED)
    public Trade trade(@PathVariable UUID id, @Valid @RequestBody TradeRequest request) {
        return service.recordTrade(id, request.symbol(), request.side(),
                request.quantity(), request.price());
    }

    @GetMapping("/{id}")
    public PortfolioView get(@PathVariable UUID id) {
        return service.getPortfolio(id);
    }
}