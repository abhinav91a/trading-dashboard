package com.tradingdashboard.marketdata.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

// Latest trade and previous close for each symbol. This works outside market hours, because
// the latest trade is the last one of the previous session.
@RestController
@RequestMapping("/api/market")
public class SnapshotController {

    private static final Pattern SYMBOL = Pattern.compile("[A-Z]{1,6}(\\.[A-Z]{1,2})?");
    private static final int MAX_SYMBOLS = 50;

    public record Snapshot(String symbol, BigDecimal price, BigDecimal previousClose, Instant timestamp) {
    }

    private final String key;
    private final String secret;
    private final ObjectMapper json;
    private final HttpClient http = HttpClient.newHttpClient();

    public SnapshotController(@Value("${alpaca.key:}") String key,
                              @Value("${alpaca.secret:}") String secret,
                              ObjectMapper json) {
        this.key = key;
        this.secret = secret;
        this.json = json;
    }

    @GetMapping("/snapshots")
    @SuppressWarnings("unchecked")
    public List<Snapshot> snapshots(@RequestParam String symbols) {
        List<String> tickers = Arrays.stream(symbols.split(","))
                .map(s -> s.trim().toUpperCase())
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        if (tickers.isEmpty() || tickers.size() > MAX_SYMBOLS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request between 1 and 50 symbols");
        }
        for (String t : tickers) {
            if (!SYMBOL.matcher(t).matches()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid symbol");
            }
        }
        if (key.isBlank() || secret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Alpaca keys are not configured");
        }

        // Symbols were validated above, so they need no URL encoding
        HttpRequest request = HttpRequest.newBuilder(URI.create(
                        "https://data.alpaca.markets/v2/stocks/snapshots?symbols="
                                + String.join(",", tickers) + "&feed=iex"))
                .header("APCA-API-KEY-ID", key)
                .header("APCA-API-SECRET-KEY", secret)
                .GET()
                .build();

        Map<String, Map<String, Object>> bySymbol = json.readValue(send(request), Map.class);
        List<Snapshot> result = new ArrayList<>();
        for (String t : tickers) {
            Map<String, Object> snap = bySymbol.get(t);
            Map<String, Object> trade = snap == null ? null : map(snap.get("latestTrade"));
            if (trade == null || trade.get("p") == null || trade.get("t") == null) {
                continue; // Alpaca has no trade for this symbol
            }
            Map<String, Object> prevBar = map(snap.get("prevDailyBar"));
            result.add(new Snapshot(
                    t,
                    money(trade.get("p")),
                    prevBar == null || prevBar.get("c") == null ? null : money(prevBar.get("c")),
                    Instant.parse(String.valueOf(trade.get("t")))));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
    }

    private static BigDecimal money(Object value) {
        return new BigDecimal(String.valueOf(value)).setScale(2, RoundingMode.HALF_UP);
    }

    private String send(HttpRequest request) {
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Alpaca returned " + response.statusCode());
            }
            return response.body();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not reach Alpaca");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Request interrupted");
        }
    }
}
