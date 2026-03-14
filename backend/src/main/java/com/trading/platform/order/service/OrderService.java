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
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository    orderRepository;
    private final OrderBookManager   orderBookManager;
    private final MatchingEngine     matchingEngine;
    private final StockRepository    stockRepository;
    private final UserRepository     userRepository;

    /**
     * CONCURRENCY DESIGN:
     *
     * 1. @Transactional(isolation = SERIALIZABLE) on the DB side:
     *    Prevents two threads from double-spending the same user balance
     *    at the database level (last line of defence).
     *
     * 2. OrderBook ReentrantLock (per stock symbol) on the in-memory side:
     *    Only ONE thread can add + match orders for a given stock at a time.
     *    Different stocks run fully in parallel (RELIANCE lock != TCS lock).
     *
     * 3. @Async("orderExecutor"):
     *    Each order placement runs on a dedicated thread pool (not the HTTP thread),
     *    so the web server is never blocked by matching logic.
     *    Returns CompletableFuture so the caller can await the result.
     */
    @Async("orderExecutor")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CompletableFuture<Order> placeOrderAsync(String username, OrderRequest request) {
        try {
            Order order = doPlaceOrder(username, request);
            return CompletableFuture.completedFuture(order);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // Kept for internal/synchronous use (e.g. tests)
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Order placeOrder(String username, OrderRequest request) {
        return doPlaceOrder(username, request);
    }

    private Order doPlaceOrder(String username, OrderRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Stock stock = stockRepository.findBySymbol(request.getSymbol())
                .orElseThrow(() -> new RuntimeException("Stock not found: " + request.getSymbol()));

        if (!stock.isTradable())
            throw new RuntimeException("Trading is disabled for " + request.getSymbol());

        OrderSide side = OrderSide.valueOf(request.getSide().toUpperCase());
        OrderType type = OrderType.valueOf(request.getType().toUpperCase());

        BigDecimal price = (type == OrderType.MARKET) ? stock.getPrice() : request.getPrice();

        // Balance check for BUY orders
        if (side == OrderSide.BUY) {
            BigDecimal totalCost = price.multiply(BigDecimal.valueOf(request.getQuantity()));
            if (user.getBalance().compareTo(totalCost) < 0)
                throw new RuntimeException("Insufficient balance");
        }

        Order order = Order.builder()
                .user(user).stock(stock).side(side).type(type)
                .price(price).quantity(request.getQuantity())
                .remainingQuantity(request.getQuantity())
                .status(OrderStatus.PENDING)
                .build();

        orderRepository.save(order);

        // ---------------------------------------------------------------
        // Acquire the per-stock lock BEFORE touching the order book.
        // This is the critical section: add order + run matching engine
        // must be atomic for a given stock.
        // ---------------------------------------------------------------
        OrderBook orderBook = orderBookManager.getOrderBook(stock.getSymbol());
        ReentrantLock lock  = orderBook.getLock();
        lock.lock();
        try {
            log.info("[OrderService] Thread '{}' acquired lock for {} — placing {} {} order",
                    Thread.currentThread().getName(), stock.getSymbol(), side, type);

            orderBook.addOrder(order);
            matchingEngine.match(orderBook);

        } finally {
            lock.unlock();
            log.info("[OrderService] Thread '{}' released lock for {}",
                    Thread.currentThread().getName(), stock.getSymbol());
        }

        return order;
    }

    public List<Order> getOrdersForUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }
}
