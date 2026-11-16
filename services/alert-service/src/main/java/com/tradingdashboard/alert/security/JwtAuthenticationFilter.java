package com.tradingdashboard.alert.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Reads "Authorization: Bearer <token>", verifies the signature and expiry, and
// stores the token's subject (the user ID) as the authenticated principal.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey key;

    public JwtAuthenticationFilter(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(header.substring(7))
                        .getPayload();
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(claims.getSubject(), null, List.of()));
            } catch (JwtException | IllegalArgumentException e) {
                // Bad or expired token: leave the context empty so the request is rejected
            }
        }
        chain.doFilter(request, response);
    }
}
