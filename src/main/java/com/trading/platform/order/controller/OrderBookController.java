package com.trading.platform.engine.controller;

import com.trading.platform.engine.service.OrderBookService;
import com.trading.platform.order.dto.OrderBookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orderbook")
@RequiredArgsConstructor
public class OrderBookController {

    private final OrderBookService orderBookService;

    @GetMapping("/{symbol}/buy")
    public List<OrderBookResponse> getBuyOrders(
            @PathVariable String symbol) {

        return orderBookService.getBuyOrders(symbol);
    }

    @GetMapping("/{symbol}/sell")
    public List<OrderBookResponse> getSellOrders(
            @PathVariable String symbol) {

        return orderBookService.getSellOrders(symbol);
    }
}