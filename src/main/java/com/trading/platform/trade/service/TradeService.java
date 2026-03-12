package com.trading.platform.trade.service;

import com.trading.platform.portfolio.service.PortfolioService;
import com.trading.platform.stock.entity.Stock;
import com.trading.platform.trade.entity.Trade;
import com.trading.platform.trade.repository.TradeRepository;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final PortfolioService portfolioService;
    private final UserRepository userRepository;

    public void executeTrade(User buyer,
                             User seller,
                             Stock stock,
                             Long quantity,
                             BigDecimal price) {

        Trade trade = Trade.builder()
                .buyer(buyer)
                .seller(seller)
                .stock(stock)
                .quantity(quantity)
                .price(price)
                .executedAt(LocalDateTime.now())
                .build();

        tradeRepository.save(trade);

        BigDecimal totalPrice =
                price.multiply(BigDecimal.valueOf(quantity));

        // update balances
        buyer.setBalance(
                buyer.getBalance().subtract(totalPrice)
        );

        seller.setBalance(
                seller.getBalance().add(totalPrice)
        );

        userRepository.save(buyer);
        userRepository.save(seller);

        // update portfolios
        portfolioService.addStock(buyer, stock, quantity);

        portfolioService.removeStock(seller, stock, quantity);
    }
}