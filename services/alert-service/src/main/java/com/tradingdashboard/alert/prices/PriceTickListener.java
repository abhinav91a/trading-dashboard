package com.tradingdashboard.alert.prices;

import com.tradingdashboard.alert.service.AlertEvaluator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PriceTickListener {

    private final AlertEvaluator evaluator;

    public PriceTickListener(AlertEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @KafkaListener(topics = "price-ticks")
    public void onTick(PriceTick tick) {
        evaluator.evaluate(tick);
    }
}
