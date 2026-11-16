package com.tradingdashboard.marketdata.alpaca;

import com.tradingdashboard.marketdata.model.PriceTick;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// Streams live trades from Alpaca and publishes each one to price-ticks, keyed by symbol.
// Trade messages look like {"T":"t","S":"AAPL","p":189.5,"t":"2026-11-16T14:30:00.123Z"}.
@Component
@ConditionalOnProperty(name = "market-data.provider", havingValue = "alpaca")
public class AlpacaPriceStream {

    private static final Logger log = LoggerFactory.getLogger(AlpacaPriceStream.class);
    private static final String TOPIC = "price-ticks";
    private static final long MAX_BACKOFF_SECONDS = 60;

    private final String key;
    private final String secret;
    private final String feed;
    private final List<String> symbols;
    private final KafkaTemplate<String, PriceTick> kafkaTemplate;
    private final ObjectMapper json;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean reconnectPending = new AtomicBoolean(false);
    private final StringBuilder partial = new StringBuilder();

    private volatile WebSocket socket;
    private volatile boolean stopped;
    private volatile int failures;

    public AlpacaPriceStream(
            @Value("${alpaca.key:}") String key,
            @Value("${alpaca.secret:}") String secret,
            @Value("${alpaca.feed:iex}") String feed,
            @Value("${market-data.symbols}") String symbols,
            KafkaTemplate<String, PriceTick> kafkaTemplate,
            ObjectMapper json) {
        this.key = key;
        this.secret = secret;
        this.feed = feed;
        this.symbols = Arrays.stream(symbols.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        this.kafkaTemplate = kafkaTemplate;
        this.json = json;
    }

    @PostConstruct
    void start() {
        if (key.isBlank() || secret.isBlank()) {
            log.error("ALPACA_KEY or ALPACA_SECRET is empty, so live prices are off. Check .env in the project root.");
            return;
        }
        connect();
    }

    @PreDestroy
    void stop() {
        stopped = true;
        WebSocket current = socket;
        if (current != null) {
            current.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown");
        }
        scheduler.shutdownNow();
    }

    private void connect() {
        if (stopped) return;
        partial.setLength(0);
        URI uri = URI.create("wss://stream.data.alpaca.markets/v2/" + feed);
        http.newWebSocketBuilder()
                .buildAsync(uri, new Listener())
                .whenComplete((ws, err) -> {
                    if (err != null) {
                        log.warn("Alpaca connect failed: {}", err.getMessage());
                        scheduleReconnect();
                    } else {
                        socket = ws;
                    }
                });
    }

    // Backs off 2, 4, 8... up to 60 seconds. The flag stops two failure callbacks from
    // scheduling two reconnects.
    private void scheduleReconnect() {
        if (stopped || !reconnectPending.compareAndSet(false, true)) return;
        failures++;
        long delay = Math.min(MAX_BACKOFF_SECONDS, 1L << Math.min(failures, 6));
        log.info("Reconnecting to Alpaca in {} seconds", delay);
        scheduler.schedule(() -> {
            reconnectPending.set(false);
            connect();
        }, delay, TimeUnit.SECONDS);
    }

    private class Listener implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket ws) {
            ws.request(1);
            send(ws, Map.of("action", "auth", "key", key, "secret", secret));
        }

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            // Long messages arrive in pieces, so collect them until the last piece
            partial.append(data);
            if (last) {
                String payload = partial.toString();
                partial.setLength(0);
                handle(ws, payload);
            }
            ws.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
            log.warn("Alpaca stream closed ({} {})", statusCode, reason);
            scheduleReconnect();
            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            log.warn("Alpaca stream error: {}", error.getMessage());
            scheduleReconnect();
        }
    }

    @SuppressWarnings("unchecked")
    private void handle(WebSocket ws, String payload) {
        List<Map<String, Object>> messages = json.readValue(payload, List.class);
        for (Map<String, Object> m : messages) {
            switch (String.valueOf(m.get("T"))) {
                case "success" -> onSuccess(ws, String.valueOf(m.get("msg")));
                case "error" -> log.error("Alpaca error {}: {}", m.get("code"), m.get("msg"));
                case "t" -> publish(m);
                default -> { }
            }
        }
    }

    private void onSuccess(WebSocket ws, String msg) {
        if ("authenticated".equals(msg)) {
            failures = 0;
            log.info("Alpaca stream authenticated, subscribing to trades for {}", symbols);
            send(ws, Map.of("action", "subscribe", "trades", symbols));
        }
    }

    private void publish(Map<String, Object> trade) {
        String symbol = String.valueOf(trade.get("S"));
        BigDecimal price = new BigDecimal(String.valueOf(trade.get("p")))
                .setScale(2, RoundingMode.HALF_UP);
        Instant timestamp = Instant.parse(String.valueOf(trade.get("t")));
        kafkaTemplate.send(TOPIC, symbol, new PriceTick(symbol, price, timestamp));
    }

    private void send(WebSocket ws, Object message) {
        ws.sendText(json.writeValueAsString(message), true);
    }
}
