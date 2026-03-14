package com.trading.platform.order.service;

import com.trading.platform.engine.OrderBook;
import com.trading.platform.engine.OrderBookManager;
import com.trading.platform.engine.service.MatchingEngine;
import com.trading.platform.margin.service.MarginService;
import com.trading.platform.order.dto.PlaceOrderRequest;
import com.trading.platform.order.entity.Order;
import com.trading.platform.order.model.OrderStatus;
import com.trading.platform.order.model.OrderType;
import com.trading.platform.stock.entity.Stock;
import com.trading.platform.stock.repository.StockRepository;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import com.trading.platform.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final MarginService marginService;
    private final OrderRepository orderRepository;

    private final OrderBookManager orderBookManager;
    private final MatchingEngine matchingEngine;

    public String placeOrder(String username, PlaceOrderRequest request) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        Stock stock = stockRepository
                .findBySymbol(request.getStockSymbol())
                .orElseThrow();

        if (!stock.isTradable()) {
            throw new RuntimeException("Trading halted for this stock");
        }

        BigDecimal orderValue =
                stock.getPrice()
                        .multiply(BigDecimal.valueOf(request.getQuantity()));

        if (!marginService.hasEnoughMargin(user, orderValue)) {

            throw new RuntimeException(
                    "Not enough margin to place trade"
            );
        }

        BigDecimal orderPrice;

        if (request.getType() == OrderType.MARKET) {
            orderPrice = stock.getPrice();
        } else {
            orderPrice = request.getPrice();
        }

        Order order = Order.builder()
                .user(user)
                .stock(stock)
                .side(request.getSide())
                .type(request.getType())
                .quantity(request.getQuantity())
                .remainingQuantity(request.getQuantity())
                .price(orderPrice)
                .stopLossPrice(request.getPrice())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);

        OrderBook orderBook =
                orderBookManager.getOrderBook(stock.getSymbol());

        orderBook.addOrder(order);

        matchingEngine.match(orderBook);

        return "Order placed successfully";
    }
}