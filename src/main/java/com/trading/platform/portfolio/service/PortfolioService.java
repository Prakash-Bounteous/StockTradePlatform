package com.trading.platform.portfolio.service;

import com.trading.platform.portfolio.entity.Portfolio;
import com.trading.platform.portfolio.repository.PortfolioRepository;
import com.trading.platform.stock.entity.Stock;
import com.trading.platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public void addStock(User user, Stock stock, Long quantity) {

        Portfolio portfolio =
                portfolioRepository
                        .findByUserIdAndStockId(
                                user.getId(),
                                stock.getId()
                        )
                        .orElse(null);

        if (portfolio == null) {

            portfolio = Portfolio.builder()
                    .user(user)
                    .stock(stock)
                    .quantity(quantity)
                    .averagePrice(stock.getPrice())
                    .build();

        } else {

            long totalQuantity =
                    portfolio.getQuantity() + quantity;

            BigDecimal totalCost =
                    portfolio.getAveragePrice()
                            .multiply(BigDecimal.valueOf(portfolio.getQuantity()))
                            .add(
                                    stock.getPrice()
                                            .multiply(BigDecimal.valueOf(quantity))
                            );

            BigDecimal newAverage =
                    totalCost.divide(BigDecimal.valueOf(totalQuantity));

            portfolio.setQuantity(totalQuantity);
            portfolio.setAveragePrice(newAverage);
        }

        portfolioRepository.save(portfolio);
    }

    public void removeStock(User user, Stock stock, Long quantity) {

        Portfolio portfolio =
                portfolioRepository
                        .findByUserIdAndStockId(
                                user.getId(),
                                stock.getId()
                        )
                        .orElseThrow();

        if (portfolio.getQuantity() <= quantity) {

            portfolioRepository.delete(portfolio);

        } else {

            portfolio.setQuantity(
                    portfolio.getQuantity() - quantity
            );

            portfolioRepository.save(portfolio);
        }
    }
}