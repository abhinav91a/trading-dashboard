package com.tradingdashboard.alert.repository;

import com.tradingdashboard.alert.model.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRuleRepository extends JpaRepository<AlertRule, UUID> {

    List<AlertRule> findByUserIdOrderByCreatedAtAsc(String userId);

    Optional<AlertRule> findByIdAndUserId(UUID id, String userId);
}
