package com.tradingdashboard.auth.service;

import com.tradingdashboard.auth.model.AppUser;
import com.tradingdashboard.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, PasswordEncoder encoder, TokenService tokens) {
        this.users = users;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    @Transactional
    public AppUser register(String username, String password) {
        String name = normalize(username);
        if (users.existsByUsername(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        return users.save(new AppUser(name, encoder.encode(password)));
    }

    public String login(String username, String password) {
        // Same message for unknown user and wrong password, so attackers can't tell which usernames exist
        AppUser user = users.findByUsername(normalize(username))
                .filter(u -> encoder.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Invalid username or password"));
        return tokens.issue(user.getId(), user.getUsername());
    }

    private String normalize(String username) {
        return username.trim().toLowerCase();
    }
}
