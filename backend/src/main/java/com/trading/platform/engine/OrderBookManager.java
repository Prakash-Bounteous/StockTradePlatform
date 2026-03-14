package com.trading.platform.engine;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderBookManager {

    // ConcurrentHashMap ensures thread-safe book creation per symbol
    // computeIfAbsent is atomic — two threads requesting the same symbol
    // will never create two separate OrderBook instances
    private final ConcurrentHashMap<String, OrderBook> orderBooks = new ConcurrentHashMap<>();

    public OrderBook getOrderBook(String symbol) {
        return orderBooks.computeIfAbsent(symbol, s -> new OrderBook());
    }
}
