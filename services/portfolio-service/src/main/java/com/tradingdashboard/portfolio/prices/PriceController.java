package com.tradingdashboard.portfolio.prices;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prices")
public class PriceController {

    private final LatestPriceCache cache;

    public PriceController(LatestPriceCache cache) {
        this.cache = cache;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<PriceTick> latest(@PathVariable String symbol) {
        return cache.find(symbol.toUpperCase())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}