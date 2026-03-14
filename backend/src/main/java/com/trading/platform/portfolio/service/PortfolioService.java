package com.trading.platform.portfolio.service;

import com.trading.platform.portfolio.entity.Portfolio;
import com.trading.platform.portfolio.repository.PortfolioRepository;
import com.trading.platform.stock.entity.Stock;
import com.trading.platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public void addStock(User user, Stock stock, Long quantity) {
        Portfolio portfolio = portfolioRepository
                .findByUserIdAndStockId(user.getId(), stock.getId())
                .orElse(null);

        if (portfolio == null) {
            portfolio = Portfolio.builder()
                    .user(user).stock(stock).quantity(quantity)
                    .averagePrice(stock.getPrice()).build();
        } else {
            long totalQuantity = portfolio.getQuantity() + quantity;
            BigDecimal totalCost = portfolio.getAveragePrice()
                    .multiply(BigDecimal.valueOf(portfolio.getQuantity()))
                    .add(stock.getPrice().multiply(BigDecimal.valueOf(quantity)));
            portfolio.setQuantity(totalQuantity);
            portfolio.setAveragePrice(totalCost.divide(BigDecimal.valueOf(totalQuantity), 4, java.math.RoundingMode.HALF_UP));
        }
        portfolioRepository.save(portfolio);
    }

    public void removeStock(User user, Stock stock, Long quantity) {
        Portfolio portfolio = portfolioRepository
                .findByUserIdAndStockId(user.getId(), stock.getId())
                .orElseThrow(() -> new RuntimeException("Portfolio entry not found"));

        if (portfolio.getQuantity() <= quantity) {
            portfolioRepository.delete(portfolio);
        } else {
            portfolio.setQuantity(portfolio.getQuantity() - quantity);
            portfolioRepository.save(portfolio);
        }
    }

    public List<Portfolio> getPortfolio(Long userId) {
        return portfolioRepository.findByUserId(userId);
    }
}
