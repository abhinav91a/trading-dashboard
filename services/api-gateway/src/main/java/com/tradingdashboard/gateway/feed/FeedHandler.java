package com.tradingdashboard.gateway.feed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class FeedHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(FeedHandler.class);

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("Feed client connected: {} (total {})", session.getId(), sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("Feed client disconnected: {} (total {})", session.getId(), sessions.size());
    }

    // Called from the Kafka listener thread. The value is already JSON, so it is
    // embedded as-is rather than parsed and re-serialized.
    public void broadcast(String topic, String json) {
        String message = "{\"topic\":\"" + topic + "\",\"data\":" + json + "}";
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                sessions.remove(session);
            }
        }
    }
}
