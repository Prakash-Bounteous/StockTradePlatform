package com.trading.platform.order.controller;

import com.trading.platform.engine.service.OrderBookService;
import com.trading.platform.order.dto.*;
import com.trading.platform.order.entity.Order;
import com.trading.platform.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderBookService orderBookService;

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(Authentication authentication,
                                        @RequestBody OrderRequest request) {
        try {
            Order order = orderService.placeOrder(authentication.getName(), request);
            return ResponseEntity.ok(Map.of("message", "Order placed successfully", "orderId", order.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public List<Order> getMyOrders(Authentication authentication) {
        return orderService.getOrdersForUser(authentication.getName());
    }

    @GetMapping("/book/{symbol}/buy")
    public List<OrderBookResponse> getBuyOrders(@PathVariable String symbol) {
        return orderBookService.getBuyOrders(symbol);
    }

    @GetMapping("/book/{symbol}/sell")
    public List<OrderBookResponse> getSellOrders(@PathVariable String symbol) {
        return orderBookService.getSellOrders(symbol);
    }
}
