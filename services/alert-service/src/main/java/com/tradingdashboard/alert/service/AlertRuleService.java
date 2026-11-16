package com.tradingdashboard.alert.service;

import com.tradingdashboard.alert.model.AlertRule;
import com.tradingdashboard.alert.repository.AlertRuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AlertRuleService {

    private static final int DEFAULT_COOLDOWN_SECONDS = 300;

    private final AlertRuleRepository rules;

    public AlertRuleService(AlertRuleRepository rules) {
        this.rules = rules;
    }

    public List<AlertRule> listForUser(String userId) {
        return rules.findByUserIdOrderByCreatedAtAsc(userId);
    }

    public AlertRule get(String userId, UUID id) {
        return requireOwned(userId, id);
    }

    @Transactional
    public AlertRule create(String userId, String symbol, AlertRule.Direction direction,
                            BigDecimal threshold, Integer cooldownSeconds) {
        int cooldown = cooldownSeconds == null ? DEFAULT_COOLDOWN_SECONDS : cooldownSeconds;
        return rules.save(new AlertRule(userId, symbol.trim().toUpperCase(),
                direction, threshold, cooldown));
    }

    @Transactional
    public void delete(String userId, UUID id) {
        rules.delete(requireOwned(userId, id));
    }

    private AlertRule requireOwned(String userId, UUID id) {
        return rules.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert rule not found"));
    }
}
