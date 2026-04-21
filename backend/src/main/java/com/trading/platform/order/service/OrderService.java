//package com.trading.platform.order.service;
//
//import com.trading.platform.engine.OrderBook;
//import com.trading.platform.engine.OrderBookManager;
//import com.trading.platform.engine.service.MatchingEngine;
//import com.trading.platform.margin.service.MarginCallService;
//import com.trading.platform.margin.service.MarginService;
//import com.trading.platform.order.dto.OrderRequest;
//import com.trading.platform.order.entity.Order;
//import com.trading.platform.order.model.*;
//import com.trading.platform.order.repository.OrderRepository;
//import com.trading.platform.stock.entity.Stock;
//import com.trading.platform.stock.repository.StockRepository;
//import com.trading.platform.trade.service.TradeService;
//import com.trading.platform.user.entity.User;
//import com.trading.platform.user.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.concurrent.locks.ReentrantLock;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class OrderService {
//
//    private final OrderRepository  orderRepository;
//    private final OrderBookManager orderBookManager;
//    private final MatchingEngine   matchingEngine;
//    private final StockRepository  stockRepository;
//    private final UserRepository   userRepository;
//    private final TradeService     tradeService;
//    private final MarginService    marginService;
//    private final MarginCallService marginCallService;
//
//    @Transactional
//    public Order placeOrder(String username, OrderRequest request) {
//
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Stock stock = stockRepository.findBySymbol(request.getSymbol())
//                .orElseThrow(() -> new RuntimeException("Stock not found: " + request.getSymbol()));
//
//        if (!stock.isTradable())
//            throw new RuntimeException("Trading is disabled for " + request.getSymbol());
//
//        OrderSide side = OrderSide.valueOf(request.getSide().toUpperCase());
//        OrderType type = OrderType.valueOf(request.getType().toUpperCase());
//        BigDecimal price = (type == OrderType.MARKET) ? stock.getPrice() : request.getPrice();
//        Long quantity = request.getQuantity();
//        BigDecimal totalCost = price.multiply(BigDecimal.valueOf(quantity));
//
//        // ---------------------------------------------------------------
//        // VALIDATION
//        // ---------------------------------------------------------------
//        if (side == OrderSide.BUY) {
//            // Use margin trading power instead of raw balance
//            // Example: balance=₹1,00,000 × 2x multiplier = ₹2,00,000 buying power
//            BigDecimal tradingPower = marginService.getTradingPower(user);
//            if (tradingPower.compareTo(totalCost) < 0) {
//                throw new RuntimeException(
//                        "Insufficient margin. Order value ₹" + totalCost
//                                + " exceeds your trading power ₹" + tradingPower
//                                + " (Balance ₹" + user.getBalance()
//                                + " × " + marginService.getOrCreateAccount(user).getMarginMultiplier() + "x margin)"
//                );
//            }
//        }
//
//        if (side == OrderSide.SELL) {
//            Long held = tradeService.getPortfolioQuantity(user, stock);
//            if (held < quantity) {
//                throw new RuntimeException(
//                        "Insufficient shares. You hold " + held
//                                + " shares of " + stock.getSymbol()
//                                + " but tried to sell " + quantity
//                );
//            }
//        }
//
//        // ---------------------------------------------------------------
//        // OPTION C — HYBRID EXECUTION
//        // MARKET → instant via system account
//        // LIMIT  → real order book matching between users
//        // ---------------------------------------------------------------
//        if (type == OrderType.MARKET) {
//            return executeMarketOrder(user, stock, side, price, quantity, totalCost);
//        } else {
//            return executeLimitOrder(user, stock, side, price, quantity);
//        }
//    }
//
//    // ---------------------------------------------------------------
//    // MARKET ORDER — instant execution via system account
//    // ---------------------------------------------------------------
//    private Order executeMarketOrder(User user, Stock stock, OrderSide side,
//                                     BigDecimal price, Long quantity, BigDecimal totalCost) {
//        User system = getSystemAccount();
//
//        if (side == OrderSide.BUY) {
//            tradeService.executeTrade(user, system, stock, quantity, price);
//            // Track how much margin was used for this buy
//            marginService.updateUsedMargin(user, totalCost, true);
//            log.info("[MARKET BUY] {} bought {} x {} @ ₹{}",
//                    user.getUsername(), quantity, stock.getSymbol(), price);
//        } else {
//            tradeService.executeTrade(system, user, stock, quantity, price);
//            // Selling reduces used margin
//            marginService.updateUsedMargin(user, totalCost, false);
//            log.info("[MARKET SELL] {} sold {} x {} @ ₹{}",
//                    user.getUsername(), quantity, stock.getSymbol(), price);
//        }
//
//        // Re-fetch user to get updated balance after trade
//        User updatedUser = userRepository.findById(user.getId()).orElse(user);
//
//        // Check if margin call triggered after this trade
//        marginCallService.checkAndTrigger(updatedUser);
//
//        Order order = Order.builder()
//                .user(user).stock(stock)
//                .side(side).type(OrderType.MARKET)
//                .price(price).quantity(quantity)
//                .remainingQuantity(0L)
//                .status(OrderStatus.FILLED)
//                .build();
//
//        return orderRepository.save(order);
//    }
//
//    // ---------------------------------------------------------------
//    // LIMIT ORDER — real order book matching between users
//    // ---------------------------------------------------------------
//    private Order executeLimitOrder(User user, Stock stock,
//                                    OrderSide side, BigDecimal price, Long quantity) {
//        Order order = Order.builder()
//                .user(user).stock(stock)
//                .side(side).type(OrderType.LIMIT)
//                .price(price).quantity(quantity)
//                .remainingQuantity(quantity)
//                .status(OrderStatus.PENDING)
//                .build();
//
//        orderRepository.save(order);
//
//        OrderBook orderBook = orderBookManager.getOrderBook(stock.getSymbol());
//        ReentrantLock lock = orderBook.getLock();
//        lock.lock();
//        try {
//            orderBook.addOrder(order);
//            matchingEngine.match(orderBook);
//
//            // After matching, check margin call for any users whose balance changed
//            User updatedUser = userRepository.findById(user.getId()).orElse(user);
//            marginCallService.checkAndTrigger(updatedUser);
//
//            log.info("[LIMIT {}] {} placed {} x {} @ ₹{} — status: {}",
//                    side, user.getUsername(), quantity, stock.getSymbol(), price, order.getStatus());
//        } finally {
//            lock.unlock();
//        }
//
//        return order;
//    }
//
//    // ---------------------------------------------------------------
//    // System account — silent market maker for MARKET orders only
//    // ---------------------------------------------------------------
//    private User getSystemAccount() {
//        return userRepository.findByUsername("system")
//                .orElseGet(() -> {
//                    User system = User.builder()
//                            .username("system")
//                            .email("system@tradepro.internal")
//                            .password("$2a$10$disabled")
//                            .balance(BigDecimal.valueOf(999_999_999))
//                            .build();
//                    log.info("[System] Created system market maker account");
//                    return userRepository.save(system);
//                });
//    }
//
//    public List<Order> getOrdersForUser(String username) {
//        User user = userRepository.findByUsername(username).orElseThrow();
//        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
//    }
//}

package com.trading.platform.order.service;

import com.trading.platform.engine.OrderBook;
import com.trading.platform.engine.OrderBookManager;
import com.trading.platform.engine.service.MatchingEngine;
import com.trading.platform.margin.service.MarginCallService;
import com.trading.platform.margin.service.MarginService;
import com.trading.platform.order.Exception.*;
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
    private final OrderRepository   orderRepository;
    private final OrderBookManager  orderBookManager;
    private final MatchingEngine    matchingEngine;
    private final StockRepository   stockRepository;
    private final UserRepository    userRepository;
    private final TradeService      tradeService;
    private final MarginService     marginService;
    private final MarginCallService marginCallService;

    @Transactional
    public Order placeOrder(String username, OrderRequest request) throws ResourceNotFoundException, TradingHaltedException, InvalidRequestException, InsufficientBalanceException, InsufficientSharesException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        Stock stock = stockRepository.findBySymbol(request.getSymbol())
                .orElseThrow(() -> new ResourceNotFoundException("Stock", request.getSymbol()));

        if (!stock.isTradable())
            throw new TradingHaltedException(stock.getSymbol());

        OrderSide side;
        OrderType type;
        try {
            side = OrderSide.valueOf(request.getSide().toUpperCase());
            type = OrderType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid order side or type. Use BUY/SELL and MARKET/LIMIT");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0)
            throw new InvalidRequestException("Quantity must be greater than zero");

        // Market orders have no price — price discovered at match time
        // Limit orders require a price
        BigDecimal price = null;
        if (type == OrderType.LIMIT) {
            if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0)
                throw new InvalidRequestException("Limit price must be greater than zero");
            price = request.getPrice();
        }

        Long quantity = request.getQuantity();

        // Margin check for BUY — use current price as estimate
        if (side == OrderSide.BUY) {
            BigDecimal estimatedPrice = (price != null) ? price : stock.getPrice();
            BigDecimal totalCost      = estimatedPrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal tradingPower   = marginService.getTradingPower(user);
            if (tradingPower.compareTo(totalCost) < 0)
                throw new InsufficientBalanceException(totalCost, tradingPower);
        }

        // Share check for SELL
        if (side == OrderSide.SELL) {
            Long held = tradeService.getPortfolioQuantity(user, stock);
            if (held < quantity)
                throw new InsufficientSharesException(stock.getSymbol(), held, quantity);
        }

        // Build order — price is null for market orders
        Order order = Order.builder()
                .user(user).stock(stock)
                .side(side).type(type)
                .price(price)
                .quantity(quantity)
                .remainingQuantity(quantity)
                .status(OrderStatus.PENDING)
                .build();

        orderRepository.save(order);

        // Add to order book and run matching engine
        // Per-stock ReentrantLock ensures thread safety
        OrderBook orderBook = orderBookManager.getOrderBook(stock.getSymbol());
        ReentrantLock lock  = orderBook.getLock();
        lock.lock();
        try {
            orderBook.addOrder(order);
            matchingEngine.match(orderBook);
        } finally {
            lock.unlock();
        }

        // Check margin call after trade
        User updatedUser = userRepository.findById(user.getId()).orElse(user);
        marginCallService.checkAndTrigger(updatedUser);

        log.info("[OrderService] {} {} {} x {} — status: {}",
                username, side, type, quantity, order.getStatus());

        return order;
    }

    public List<Order> getOrdersForUser(String username) throws ResourceNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }
}