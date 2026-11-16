package com.tradingdashboard.gateway.feed;

import com.tradingdashboard.gateway.security.TokenVerifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

// Checks the token before the WebSocket opens. The token comes from /ws/feed?token=...
// This class never logs the query string, so the token stays out of the logs.
@Component
public class TokenHandshakeInterceptor implements HandshakeInterceptor {

    static final String USER_ATTRIBUTE = "userId";

    private final TokenVerifier verifier;

    public TokenHandshakeInterceptor(TokenVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler handler, Map<String, Object> attributes) {
        String token = UriComponentsBuilder.fromUri(request.getURI()).build()
                .getQueryParams().getFirst("token");
        Optional<String> userId = verifier.userIdFrom(token);
        if (userId.isEmpty()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        attributes.put(USER_ATTRIBUTE, userId.get());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler handler, Exception exception) {
        // nothing to do after the handshake
    }
}
