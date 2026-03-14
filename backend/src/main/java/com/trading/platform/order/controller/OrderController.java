package com.trading.platform.order.controller;

import com.trading.platform.engine.service.OrderBookService;
import com.trading.platform.order.dto.*;
import com.trading.platform.order.entity.Order;
import com.trading.platform.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService     orderService;
    private final OrderBookService orderBookService;

    /**
     * Async order placement.
     * The HTTP request returns only after the order is saved + matched,
     * but the matching runs on the orderExecutor thread pool — not Tomcat's.
     * Multiple simultaneous requests are handled in parallel.
     */
    @PostMapping("/place")
    public CompletableFuture<ResponseEntity<Object>> placeOrder(
            Authentication authentication,
            @RequestBody OrderRequest request) {

        return orderService
                .placeOrderAsync(authentication.getName(), request)
                .thenApply(order ->
                        ResponseEntity.ok((Object) Map.of(
                                "message", "Order placed successfully",
                                "orderId", order.getId()
                        ))
                )
                .exceptionally(ex -> {
                    String msg = ex.getCause() != null
                            ? ex.getCause().getMessage()
                            : ex.getMessage();
                    log.warn("[OrderController] Order failed: {}", msg);
                    return ResponseEntity
                            .badRequest()
                            .body(Map.of("error", msg));
                });
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
