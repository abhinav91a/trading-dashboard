package com.tradingdashboard.portfolio.repository;

import com.tradingdashboard.portfolio.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
}