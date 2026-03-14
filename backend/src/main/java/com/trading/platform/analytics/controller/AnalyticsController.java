package com.trading.platform.analytics.controller;

import com.trading.platform.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/pnl")
    public Map<String, BigDecimal> getPnL(Authentication authentication) {
        return analyticsService.calculatePnL(authentication.getName());
    }
}
