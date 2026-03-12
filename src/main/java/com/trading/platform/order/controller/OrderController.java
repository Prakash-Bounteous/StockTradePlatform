package com.trading.platform.order.controller;

import com.trading.platform.order.dto.PlaceOrderRequest;
import com.trading.platform.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public String placeOrder(
            Authentication authentication,
            @RequestBody PlaceOrderRequest request) {

        String username = authentication.getName();

        return orderService.placeOrder(username, request);
    }
}