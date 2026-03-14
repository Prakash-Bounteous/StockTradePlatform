package com.trading.platform.trade.service;

import com.trading.platform.notification.service.NotificationService;
import com.trading.platform.portfolio.service.PortfolioService;
import com.trading.platform.stock.entity.Stock;
import com.trading.platform.trade.entity.Trade;
import com.trading.platform.trade.repository.TradeRepository;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final PortfolioService portfolioService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void executeTrade(User buyer, User seller, Stock stock, Long quantity, BigDecimal price) {
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(quantity));

        Trade trade = Trade.builder()
                .buyer(buyer).seller(seller).stock(stock)
                .quantity(quantity).price(price)
                .executedAt(LocalDateTime.now()).build();
        tradeRepository.save(trade);

        buyer.setBalance(buyer.getBalance().subtract(totalPrice));
        seller.setBalance(seller.getBalance().add(totalPrice));
        userRepository.save(buyer);
        userRepository.save(seller);

        portfolioService.addStock(buyer, stock, quantity);
        portfolioService.removeStock(seller, stock, quantity);

        notificationService.createNotification(buyer, "Bought " + quantity + " shares of " + stock.getSymbol() + " @ ₹" + price);
        notificationService.createNotification(seller, "Sold " + quantity + " shares of " + stock.getSymbol() + " @ ₹" + price);
    }

    public List<Trade> getTradesForUser(Long userId) {
        return tradeRepository.findByBuyerIdOrSellerIdOrderByExecutedAtDesc(userId, userId);
    }
}
