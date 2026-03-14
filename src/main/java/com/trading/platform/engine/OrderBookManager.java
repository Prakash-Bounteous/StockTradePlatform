package com.trading.platform.engine;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderBookManager {

    private final ConcurrentHashMap<String, OrderBook> orderBooks
            = new ConcurrentHashMap<>();

    public OrderBook getOrderBook(String symbol) {

        return orderBooks.computeIfAbsent(
                symbol,
                s -> new OrderBook()
        );
    }
}