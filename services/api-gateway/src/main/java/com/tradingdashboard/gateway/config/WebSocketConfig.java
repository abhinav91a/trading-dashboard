package com.tradingdashboard.gateway.config;

import com.tradingdashboard.gateway.feed.FeedHandler;
import com.tradingdashboard.gateway.feed.TokenHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final FeedHandler feedHandler;
    private final TokenHandshakeInterceptor tokenInterceptor;

    public WebSocketConfig(FeedHandler feedHandler, TokenHandshakeInterceptor tokenInterceptor) {
        this.feedHandler = feedHandler;
        this.tokenInterceptor = tokenInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // "*" allows any origin during development. Restrict it before deployment.
        registry.addHandler(feedHandler, "/ws/feed")
                .addInterceptors(tokenInterceptor)
                .setAllowedOrigins("*");
    }
}
