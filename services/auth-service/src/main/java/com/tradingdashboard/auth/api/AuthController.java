package com.tradingdashboard.auth.api;

import com.tradingdashboard.auth.model.AppUser;
import com.tradingdashboard.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // BCrypt only uses the first 72 bytes of a password, so longer passwords are rejected here
    public record Credentials(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record RegisteredUser(UUID id, String username) {
    }

    public record TokenResponse(String token) {
    }

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisteredUser register(@Valid @RequestBody Credentials request) {
        AppUser user = service.register(request.username(), request.password());
        return new RegisteredUser(user.getId(), user.getUsername());
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody Credentials request) {
        return new TokenResponse(service.login(request.username(), request.password()));
    }
}
