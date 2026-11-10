package com.tradingdashboard.marketdata.simulator;

import com.tradingdashboard.marketdata.model.PriceTick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
@ConditionalOnProperty(name = "market-data.simulator.enabled", havingValue = "true", matchIfMissing = true)
public class PriceSimulator {

    private static final Logger log = LoggerFactory.getLogger(PriceSimulator.class);
    private static final String TOPIC = "price-ticks";

    private final Map<String, BigDecimal> prices = new LinkedHashMap<>();
    private final KafkaTemplate<String, PriceTick> kafkaTemplate;

    public PriceSimulator(KafkaTemplate<String, PriceTick> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        prices.put("AAPL", new BigDecimal("189.42"));
        prices.put("MSFT", new BigDecimal("415.10"));
        prices.put("GOOGL", new BigDecimal("170.25"));
        prices.put("TSLA", new BigDecimal("245.80"));
        prices.put("AMZN", new BigDecimal("185.30"));
    }

    @Scheduled(fixedRateString = "${market-data.tick-interval-ms:1000}")
    public void tick() {
        for (Map.Entry<String, BigDecimal> entry : prices.entrySet()) {
            String symbol = entry.getKey();
            BigDecimal next = randomWalk(entry.getValue());
            entry.setValue(next);

            PriceTick tick = new PriceTick(symbol, next, Instant.now());
            kafkaTemplate.send(TOPIC, symbol, tick);
        }
        log.debug("Published ticks for {} symbols", prices.size());
    }

    private BigDecimal randomWalk(BigDecimal current) {
        // Gaussian noise centered on zero, about 0.1% per tick
        double changeFraction = ThreadLocalRandom.current().nextGaussian() * 0.001;
        BigDecimal change = current.multiply(BigDecimal.valueOf(changeFraction));
        return current.add(change).setScale(2, RoundingMode.HALF_UP);
    }
}