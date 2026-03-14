package com.trading.platform.analytics.service;

import com.trading.platform.portfolio.entity.Portfolio;
import com.trading.platform.portfolio.repository.PortfolioRepository;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    public Map<String, BigDecimal> getLeaderboard() {

        List<User> users = userRepository.findAll();

        Map<String, BigDecimal> leaderboard = new HashMap<>();

        for (User user : users) {

            List<Portfolio> portfolios =
                    portfolioRepository.findByUserId(user.getId());

            BigDecimal totalValue = portfolios.stream()
                    .map(p ->
                            p.getStock().getPrice()
                                    .multiply(BigDecimal.valueOf(p.getQuantity()))
                    )
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            leaderboard.put(user.getUsername(), totalValue);
        }

        return leaderboard.entrySet()
                .stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}