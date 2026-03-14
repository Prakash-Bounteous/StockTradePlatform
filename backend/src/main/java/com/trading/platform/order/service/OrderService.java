package com.trading.platform.order.service;

import com.trading.platform.engine.OrderBook;
import com.trading.platform.engine.OrderBookManager;
import com.trading.platform.engine.service.MatchingEngine;
import com.trading.platform.order.dto.OrderRequest;
import com.trading.platform.order.entity.Order;
import com.trading.platform.order.model.*;
import com.trading.platform.order.repository.OrderRepository;
import com.trading.platform.stock.entity.Stock;
import com.trading.platform.stock.repository.StockRepository;
import com.trading.platform.trade.service.TradeService;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository  orderRepository;
    private final OrderBookManager orderBookManager;
    private final MatchingEngine   matchingEngine;
    private final StockRepository  stockRepository;
    private final UserRepository   userRepository;
    private final TradeService     tradeService;

    @Transactional
    public Order placeOrder(String username, OrderRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Stock stock = stockRepository.findBySymbol(request.getSymbol())
                .orElseThrow(() -> new RuntimeException("Stock not found: " + request.getSymbol()));

        if (!stock.isTradable())
            throw new RuntimeException("Trading is disabled for " + request.getSymbol());

        OrderSide side = OrderSide.valueOf(request.getSide().toUpperCase());
        OrderType type = OrderType.valueOf(request.getType().toUpperCase());
        BigDecimal price = (type == OrderType.MARKET) ? stock.getPrice() : request.getPrice();
        Long quantity = request.getQuantity();

        if (side == OrderSide.BUY) {
            BigDecimal totalCost = price.multiply(BigDecimal.valueOf(quantity));
            if (user.getBalance().compareTo(totalCost) < 0)
                throw new RuntimeException("Insufficient balance. Need ₹"
                        + totalCost + " but have ₹" + user.getBalance());
        }

        if (side == OrderSide.SELL) {
            // Check user actually holds enough shares
            boolean hasShares = false;
            var portfolio = user.getId() != null
                    ? tradeService.getPortfolioQuantity(user, stock)
                    : 0L;
            if (portfolio < quantity)
                throw new RuntimeException("Insufficient shares. You hold "
                        + portfolio + " shares of " + stock.getSymbol());
        }

        // ---------------------------------------------------------------
        // INSTANT EXECUTION: For a simulation platform, every order
        // executes immediately against the market (system as counterparty).
        // This ensures balance, portfolio, notifications all update instantly.
        // ---------------------------------------------------------------
        if (side == OrderSide.BUY) {
            // System sells to user
            User systemSeller = getSystemAccount();
            tradeService.executeTrade(user, systemSeller, stock, quantity, price);
        } else {
            // System buys from user
            User systemBuyer = getSystemAccount();
            tradeService.executeTrade(systemBuyer, user, stock, quantity, price);
        }

        // Also save the order record for history
        Order order = Order.builder()
                .user(user).stock(stock).side(side).type(type)
                .price(price).quantity(quantity)
                .remainingQuantity(0L)
                .status(OrderStatus.FILLED)
                .build();
        orderRepository.save(order);

        // Still add to order book for limit order matching between real users
        if (type == OrderType.LIMIT) {
            OrderBook orderBook = orderBookManager.getOrderBook(stock.getSymbol());
            ReentrantLock lock = orderBook.getLock();
            lock.lock();
            try {
                orderBook.addOrder(order);
                matchingEngine.match(orderBook);
            } finally {
                lock.unlock();
            }
        }

        log.info("[OrderService] {} {} x {} @ ₹{} executed for {}",
                side, quantity, stock.getSymbol(), price, username);

        return order;
    }

    private User getSystemAccount() {
        return userRepository.findByUsername("system")
                .orElseGet(() -> {
                    User system = User.builder()
                            .username("system")
                            .email("system@tradepro.internal")
                            .password("$2a$10$disabled")
                            .balance(BigDecimal.valueOf(999999999))
                            .build();
                    return userRepository.save(system);
                });
    }

    public List<Order> getOrdersForUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }
}