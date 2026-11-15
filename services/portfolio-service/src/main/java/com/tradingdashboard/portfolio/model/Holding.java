package com.tradingdashboard.portfolio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;
import java.math.RoundingMode;

@Entity
@Table(name = "holdings")
public class Holding {

    @Id
    private UUID id;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal quantity;

    @Column(name = "average_cost", nullable = false, precision = 18, scale = 4)
    private BigDecimal averageCost;

    protected Holding() {
    }

    public Holding(UUID portfolioId, String symbol, BigDecimal quantity, BigDecimal averageCost) {
        this.id = UUID.randomUUID();
        this.portfolioId = portfolioId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageCost = averageCost;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPortfolioId() {
        return portfolioId;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void applyBuy(BigDecimal buyQuantity, BigDecimal buyPrice) {
        BigDecimal newQuantity = quantity.add(buyQuantity);
        BigDecimal totalCost = quantity.multiply(averageCost).add(buyQuantity.multiply(buyPrice));
        this.averageCost = totalCost.divide(newQuantity, 4, RoundingMode.HALF_UP);
        this.quantity = newQuantity;
    }

    public void applySell(BigDecimal sellQuantity) {
        this.quantity = quantity.subtract(sellQuantity);
    }
}