package com.tradingdashboard.portfolio.service;

import com.tradingdashboard.portfolio.model.Holding;
import com.tradingdashboard.portfolio.model.Portfolio;
import com.tradingdashboard.portfolio.model.Trade;
import com.tradingdashboard.portfolio.prices.LatestPriceCache;
import com.tradingdashboard.portfolio.prices.PriceTick;
import com.tradingdashboard.portfolio.repository.HoldingRepository;
import com.tradingdashboard.portfolio.repository.PortfolioRepository;
import com.tradingdashboard.portfolio.repository.TradeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioService {

    public record Position(String symbol, BigDecimal quantity, BigDecimal averageCost,
                           BigDecimal lastPrice, BigDecimal marketValue, BigDecimal unrealizedPnl) {
    }

    public record PortfolioView(UUID id, String name, List<Position> positions) {
    }

    private final PortfolioRepository portfolios;
    private final HoldingRepository holdings;
    private final TradeRepository trades;
    private final LatestPriceCache prices;

    public PortfolioService(PortfolioRepository portfolios, HoldingRepository holdings,
                            TradeRepository trades, LatestPriceCache prices) {
        this.portfolios = portfolios;
        this.holdings = holdings;
        this.trades = trades;
        this.prices = prices;
    }

    public List<Portfolio> listForOwner(String ownerId) {
        return portfolios.findByOwnerIdOrderByCreatedAtAsc(ownerId);
    }

    @Transactional
    public Portfolio createPortfolio(String ownerId, String name) {
        return portfolios.save(new Portfolio(ownerId, name));
    }

    @Transactional
    public Trade recordTrade(String ownerId, UUID portfolioId, String symbol,
                             Trade.Side side, BigDecimal quantity, BigDecimal price) {
        requireOwned(ownerId, portfolioId);
        String normalized = symbol.toUpperCase();

        Holding holding = holdings.findByPortfolioIdAndSymbol(portfolioId, normalized)
                .orElseGet(() -> new Holding(portfolioId, normalized, BigDecimal.ZERO, BigDecimal.ZERO));

        if (side == Trade.Side.BUY) {
            holding.applyBuy(quantity, price);
        } else {
            if (holding.getQuantity().compareTo(quantity) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot sell " + quantity + " " + normalized
                                + ", only " + holding.getQuantity() + " held");
            }
            holding.applySell(quantity);
        }

        holdings.save(holding);
        return trades.save(new Trade(portfolioId, normalized, side, quantity, price));
    }

    @Transactional(readOnly = true)
    public PortfolioView getPortfolio(String ownerId, UUID portfolioId) {
        Portfolio portfolio = requireOwned(ownerId, portfolioId);

        List<Position> positions = holdings.findByPortfolioId(portfolioId).stream()
                .filter(h -> h.getQuantity().signum() > 0)
                .map(this::toPosition)
                .toList();

        return new PortfolioView(portfolio.getId(), portfolio.getName(), positions);
    }

    private Position toPosition(Holding h) {
        BigDecimal costBasis = h.getQuantity().multiply(h.getAverageCost());
        BigDecimal lastPrice = prices.find(h.getSymbol()).map(PriceTick::price).orElse(null);

        if (lastPrice == null) {
            return new Position(h.getSymbol(), h.getQuantity(), h.getAverageCost(), null, null, null);
        }

        BigDecimal marketValue = h.getQuantity().multiply(lastPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal unrealizedPnl = marketValue.subtract(costBasis.setScale(2, RoundingMode.HALF_UP));
        return new Position(h.getSymbol(), h.getQuantity(), h.getAverageCost(),
                lastPrice, marketValue, unrealizedPnl);
    }

    // Returns 404 for portfolios owned by someone else, so IDs don't reveal what exists
    private Portfolio requireOwned(String ownerId, UUID portfolioId) {
        return portfolios.findByIdAndOwnerId(portfolioId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
    }
}
