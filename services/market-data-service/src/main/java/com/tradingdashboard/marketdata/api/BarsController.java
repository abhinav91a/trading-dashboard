package com.tradingdashboard.marketdata.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.regex.Pattern;

// Serves historical bars for charts. The Alpaca keys stay on the server, and the browser
// receives Alpaca's JSON unchanged.
@RestController
@RequestMapping("/api/market")
public class BarsController {

    private static final Pattern SYMBOL = Pattern.compile("[A-Z]{1,6}(\\.[A-Z]{1,2})?");
    private static final Set<String> TIMEFRAMES = Set.of("1Min", "5Min", "15Min", "1Hour", "1Day", "1Week");

    private final String key;
    private final String secret;
    private final HttpClient http = HttpClient.newHttpClient();

    public BarsController(@Value("${alpaca.key:}") String key, @Value("${alpaca.secret:}") String secret) {
        this.key = key;
        this.secret = secret;
    }

    @GetMapping("/bars/{symbol}")
    public ResponseEntity<String> bars(@PathVariable String symbol,
                                       @RequestParam(defaultValue = "1Day") String timeframe,
                                       @RequestParam(defaultValue = "365") int days) {
        String ticker = symbol.toUpperCase();
        if (!SYMBOL.matcher(ticker).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid symbol");
        }
        if (!TIMEFRAMES.contains(timeframe)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported timeframe");
        }
        if (days < 1 || days > 1095) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "days must be between 1 and 1095");
        }
        if (key.isBlank() || secret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Alpaca keys are not configured");
        }

        String start = Instant.now().minus(Duration.ofDays(days)).truncatedTo(ChronoUnit.SECONDS).toString();
        String query = "timeframe=" + enc(timeframe) + "&start=" + enc(start) + "&limit=1000&feed=iex";
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("https://data.alpaca.markets/v2/stocks/" + ticker + "/bars?" + query))
                .header("APCA-API-KEY-ID", key)
                .header("APCA-API-SECRET-KEY", secret)
                .GET()
                .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Alpaca returned " + response.statusCode());
            }
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response.body());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not reach Alpaca");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Request interrupted");
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
