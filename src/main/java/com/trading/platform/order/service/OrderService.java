package com.trading.platform.order.service;

import com.trading.platform.margin.service.MarginService;
import com.trading.platform.order.dto.PlaceOrderRequest;
import com.trading.platform.order.entity.Order;
import com.trading.platform.order.model.OrderStatus;
import com.trading.platform.stock.entity.Stock;
import com.trading.platform.stock.repository.StockRepository;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
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
    private final com.trading.platform.order.repository.OrderRepository orderRepository;

    public String placeOrder(String username, PlaceOrderRequest request) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        Stock stock = stockRepository
                .findBySymbol(request.getStockSymbol())
                .orElseThrow();

        BigDecimal orderValue =
                stock.getPrice()
                        .multiply(BigDecimal.valueOf(request.getQuantity()));

        if (!marginService.hasEnoughMargin(user, orderValue)) {

            throw new RuntimeException(
                    "Not enough margin to place trade"
            );
        }

        Order order = Order.builder()
                .user(user)
                .stock(stock)
                .side(request.getSide())
                .type(request.getType())
                .quantity(request.getQuantity())
                .remainingQuantity(request.getQuantity())
                .price(stock.getPrice())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);

        return "Order placed successfully";
    }
}