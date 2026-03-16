package com.trading.platform.margin.controller;

import com.trading.platform.margin.entity.MarginAccount;
import com.trading.platform.margin.service.MarginService;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/margin")
@RequiredArgsConstructor
public class MarginController {

    private final MarginService marginService;
    private final UserRepository userRepository;

    @GetMapping("/info")
    public Map<String, Object> getMarginInfo(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        MarginAccount account = marginService.getOrCreateAccount(user);
        BigDecimal tradingPower = marginService.getTradingPower(user);

        return Map.of(
                "balance",             user.getBalance(),
                "marginMultiplier",    account.getMarginMultiplier(),
                "tradingPower",        tradingPower,
                "usedMargin",          account.getUsedMargin(),
                "marginCallTriggered", account.isMarginCallTriggered()
        );
    }
}