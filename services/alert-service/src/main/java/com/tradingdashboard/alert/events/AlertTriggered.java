package com.tradingdashboard.alert.events;

import com.tradingdashboard.alert.model.AlertRule;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AlertTriggered(
        UUID ruleId,
        String userId,
        String symbol,
        AlertRule.Direction direction,
        BigDecimal threshold,
        BigDecimal price,
        Instant triggeredAt) {
}
