package com.tradingdashboard.alert.api;

import com.tradingdashboard.alert.model.AlertRule;
import com.tradingdashboard.alert.service.AlertRuleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
public class AlertRuleController {

    public record CreateAlertRequest(
            @NotBlank @Size(max = 10) String symbol,
            @NotNull AlertRule.Direction direction,
            @NotNull @Positive BigDecimal threshold,
            @Min(0) Integer cooldownSeconds) {
    }

    private final AlertRuleService service;

    public AlertRuleController(AlertRuleService service) {
        this.service = service;
    }

    @GetMapping
    public List<AlertRule> list(@RequestHeader("X-User-Id") String userId) {
        return service.listForUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertRule create(@RequestHeader("X-User-Id") String userId,
                            @Valid @RequestBody CreateAlertRequest request) {
        return service.create(userId, request.symbol(), request.direction(),
                request.threshold(), request.cooldownSeconds());
    }

    @GetMapping("/{id}")
    public AlertRule get(@RequestHeader("X-User-Id") String userId, @PathVariable UUID id) {
        return service.get(userId, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-User-Id") String userId, @PathVariable UUID id) {
        service.delete(userId, id);
    }
}
