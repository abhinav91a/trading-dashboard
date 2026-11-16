package com.tradingdashboard.gateway.config;

import com.tradingdashboard.gateway.feed.FeedHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final FeedHandler feedHandler;

    public WebSocketConfig(FeedHandler feedHandler) {
        this.feedHandler = feedHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // "*" allows any origin during development. Restrict it once the frontend has a fixed address.
        registry.addHandler(feedHandler, "/ws/feed")
                .setAllowedOrigins("*");
    }
}
