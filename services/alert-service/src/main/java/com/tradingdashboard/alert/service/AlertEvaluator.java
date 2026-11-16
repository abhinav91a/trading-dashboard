package com.tradingdashboard.alert.service;

import com.tradingdashboard.alert.events.AlertTriggered;
import com.tradingdashboard.alert.model.AlertRule;
import com.tradingdashboard.alert.prices.PriceTick;
import com.tradingdashboard.alert.repository.AlertRuleRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@Service
public class AlertEvaluator {

    private static final String TOPIC = "alert-triggered";

    private final AlertRuleRepository rules;
    private final KafkaTemplate<String, AlertTriggered> kafka;

    public AlertEvaluator(AlertRuleRepository rules, KafkaTemplate<String, AlertTriggered> kafka) {
        this.rules = rules;
        this.kafka = kafka;
    }

    @Transactional
    public void evaluate(PriceTick tick) {
        Instant now = Instant.now();

        for (AlertRule rule : rules.findBySymbolAndActiveTrue(tick.symbol())) {
            if (!conditionMet(rule, tick.price())) {
                continue;
            }
            if (inCooldown(rule, now)) {
                continue;
            }

            rules.markTriggered(rule.getId(), now);
            kafka.send(TOPIC, rule.getUserId(), new AlertTriggered(
                    rule.getId(), rule.getUserId(), rule.getSymbol(),
                    rule.getDirection(), rule.getThreshold(), tick.price(), now));
        }
    }

    private boolean conditionMet(AlertRule rule, BigDecimal price) {
        return switch (rule.getDirection()) {
            case ABOVE -> price.compareTo(rule.getThreshold()) >= 0;
            case BELOW -> price.compareTo(rule.getThreshold()) <= 0;
        };
    }

    private boolean inCooldown(AlertRule rule, Instant now) {
        Instant last = rule.getLastTriggeredAt();
        return last != null && Duration.between(last, now).getSeconds() < rule.getCooldownSeconds();
    }
}
