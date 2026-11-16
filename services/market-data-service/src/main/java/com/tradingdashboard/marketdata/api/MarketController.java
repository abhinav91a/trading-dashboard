package com.tradingdashboard.marketdata.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final List<String> symbols;

    public MarketController(@Value("${market-data.symbols}") String symbols) {
        this.symbols = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @GetMapping("/symbols")
    public List<String> symbols() {
        return symbols;
    }
}
