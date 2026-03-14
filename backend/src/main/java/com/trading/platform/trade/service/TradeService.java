package com.trading.platform.trade.service;

import com.trading.platform.notification.service.NotificationService;
import com.trading.platform.portfolio.entity.Portfolio;
import com.trading.platform.portfolio.repository.PortfolioRepository;
import com.trading.platform.portfolio.service.PortfolioService;
import com.trading.platform.stock.entity.Stock;
import com.trading.platform.trade.entity.Trade;
import com.trading.platform.trade.repository.TradeRepository;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository       tradeRepository;
    private final PortfolioService      portfolioService;
    private final PortfolioRepository   portfolioRepository;
    private final UserRepository        userRepository;
    private final NotificationService   notificationService;

    @Transactional
    public void executeTrade(User buyer, User seller, Stock stock,
                             Long quantity, BigDecimal price) {

        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(quantity));

        // Save trade record
        Trade trade = Trade.builder()
                .buyer(buyer).seller(seller).stock(stock)
                .quantity(quantity).price(price)
                .executedAt(LocalDateTime.now())
                .build();
        tradeRepository.save(trade);

        // Update balances — re-fetch to get latest DB state
        User freshBuyer  = userRepository.findById(buyer.getId()).orElse(buyer);
        User freshSeller = userRepository.findById(seller.getId()).orElse(seller);

        freshBuyer.setBalance(freshBuyer.getBalance().subtract(totalPrice));
        freshSeller.setBalance(freshSeller.getBalance().add(totalPrice));

        userRepository.save(freshBuyer);
        userRepository.save(freshSeller);

        // Update buyer's portfolio (add shares)
        portfolioService.addStock(freshBuyer, stock, quantity);

        // Update seller's portfolio (remove shares) — skip for system account
        boolean sellerIsSystem = "system".equals(freshSeller.getUsername());
        if (!sellerIsSystem) {
            portfolioService.removeStock(freshSeller, stock, quantity);
        }

        // Notifications — skip for system account
        boolean buyerIsSystem = "system".equals(freshBuyer.getUsername());
        if (!buyerIsSystem) {
            notificationService.createNotification(freshBuyer,
                    "✅ Bought " + quantity + " shares of "
                            + stock.getSymbol() + " @ ₹" + price);
        }
        if (!sellerIsSystem) {
            notificationService.createNotification(freshSeller,
                    "✅ Sold " + quantity + " shares of "
                            + stock.getSymbol() + " @ ₹" + price);
        }

        log.info("[TradeService] Trade executed: {} x {} @ ₹{} | buyer={} seller={}",
                quantity, stock.getSymbol(), price,
                freshBuyer.getUsername(), freshSeller.getUsername());
    }

    public List<Trade> getTradesForUser(Long userId) {
        return tradeRepository.findByBuyerIdOrSellerIdOrderByExecutedAtDesc(userId, userId);
    }

    // Used by OrderService to validate sell orders
    public Long getPortfolioQuantity(User user, Stock stock) {
        return portfolioRepository
                .findByUserIdAndStockId(user.getId(), stock.getId())
                .map(Portfolio::getQuantity)
                .orElse(0L);
    }
}