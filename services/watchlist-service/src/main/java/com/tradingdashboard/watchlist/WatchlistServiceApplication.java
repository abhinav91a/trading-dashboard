package com.tradingdashboard.watchlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WatchlistServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WatchlistServiceApplication.class, args);
	}

}