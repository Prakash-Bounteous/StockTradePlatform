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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderBookManager orderBookManager;
    private final MatchingEngine matchingEngine;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;

    @Transactional
    public Order placeOrder(String username, OrderRequest request) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Stock stock = stockRepository.findBySymbol(request.getSymbol())
                .orElseThrow(() -> new RuntimeException("Stock not found: " + request.getSymbol()));

        if (!stock.isTradable()) throw new RuntimeException("Trading is disabled for " + request.getSymbol());

        OrderSide side = OrderSide.valueOf(request.getSide().toUpperCase());
        OrderType type = OrderType.valueOf(request.getType().toUpperCase());

        BigDecimal price = request.getPrice();
        if (type == OrderType.MARKET) {
            price = stock.getPrice();
        }

        // Balance check for BUY orders
        if (side == OrderSide.BUY) {
            BigDecimal totalCost = price.multiply(BigDecimal.valueOf(request.getQuantity()));
            if (user.getBalance().compareTo(totalCost) < 0) {
                throw new RuntimeException("Insufficient balance");
            }
        }

        Order order = Order.builder()
                .user(user).stock(stock).side(side).type(type)
                .price(price).quantity(request.getQuantity())
                .remainingQuantity(request.getQuantity())
                .status(OrderStatus.PENDING).build();

        orderRepository.save(order);

        OrderBook orderBook = orderBookManager.getOrderBook(stock.getSymbol());
        orderBook.addOrder(order);
        matchingEngine.match(orderBook);

        return order;
    }

    public List<Order> getOrdersForUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }
}
