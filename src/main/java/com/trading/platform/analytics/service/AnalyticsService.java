package com.trading.platform.analytics.service;

import com.trading.platform.portfolio.entity.Portfolio;
import com.trading.platform.portfolio.repository.PortfolioRepository;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    public Map<String, BigDecimal> calculatePnL(String username) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        List<Portfolio> portfolios =
                portfolioRepository.findByUserId(user.getId());

        Map<String, BigDecimal> pnl = new HashMap<>();

        BigDecimal total = BigDecimal.ZERO;

        for (Portfolio portfolio : portfolios) {

            BigDecimal currentPrice =
                    portfolio.getStock().getPrice();

            BigDecimal averagePrice =
                    portfolio.getAveragePrice() == null
                            ? currentPrice
                            : portfolio.getAveragePrice();

            BigDecimal profitLoss =
                    currentPrice.subtract(averagePrice)
                            .multiply(
                                    BigDecimal.valueOf(
                                            portfolio.getQuantity()
                                    )
                            );

            pnl.put(
                    portfolio.getStock().getSymbol(),
                    profitLoss
            );

            total = total.add(profitLoss);
        }

        pnl.put("TOTAL", total);

        return pnl;
    }
}