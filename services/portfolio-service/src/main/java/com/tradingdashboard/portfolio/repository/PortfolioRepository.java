package com.tradingdashboard.portfolio.repository;

import com.tradingdashboard.portfolio.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {

    List<Portfolio> findByOwnerIdOrderByCreatedAtAsc(String ownerId);

    Optional<Portfolio> findByIdAndOwnerId(UUID id, String ownerId);
}
