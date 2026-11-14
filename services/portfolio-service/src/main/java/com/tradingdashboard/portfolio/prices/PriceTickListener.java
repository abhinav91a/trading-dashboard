package com.tradingdashboard.portfolio.prices;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PriceTickListener {

    private final LatestPriceCache cache;

    public PriceTickListener(LatestPriceCache cache) {
        this.cache = cache;
    }

    @KafkaListener(topics = "price-ticks")
    public void onTick(PriceTick tick) {
        cache.update(tick);
    }
}