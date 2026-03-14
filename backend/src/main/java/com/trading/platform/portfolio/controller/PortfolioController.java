package com.trading.platform.portfolio.controller;

import com.trading.platform.portfolio.entity.Portfolio;
import com.trading.platform.portfolio.service.PortfolioService;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserRepository userRepository;

    @GetMapping
    public List<Portfolio> getPortfolio(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return portfolioService.getPortfolio(user.getId());
    }
}
