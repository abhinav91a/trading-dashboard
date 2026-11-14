package com.tradingdashboard.portfolio.prices;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LatestPriceCache {

    private final Map<String, PriceTick> latest = new ConcurrentHashMap<>();

    public void update(PriceTick tick) {
        latest.put(tick.symbol(), tick);
    }

    public Optional<PriceTick> find(String symbol) {
        return Optional.ofNullable(latest.get(symbol));
    }
}