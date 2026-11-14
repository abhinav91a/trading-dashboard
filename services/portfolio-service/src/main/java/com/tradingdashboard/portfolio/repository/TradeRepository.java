package com.tradingdashboard.portfolio.repository;

import com.tradingdashboard.portfolio.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {

    List<Trade> findByPortfolioIdOrderByExecutedAtAsc(UUID portfolioId);
}