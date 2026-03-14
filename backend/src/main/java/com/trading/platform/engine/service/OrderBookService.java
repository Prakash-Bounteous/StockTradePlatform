package com.trading.platform.engine.service;

import com.trading.platform.engine.OrderBook;
import com.trading.platform.engine.OrderBookManager;
import com.trading.platform.order.dto.OrderBookResponse;
import com.trading.platform.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderBookService {

    private final OrderBookManager orderBookManager;

    public List<OrderBookResponse> getBuyOrders(String symbol) {
        OrderBook orderBook = orderBookManager.getOrderBook(symbol);
        return orderBook.getBuyOrders().stream().map(this::convert).collect(Collectors.toList());
    }

    public List<OrderBookResponse> getSellOrders(String symbol) {
        OrderBook orderBook = orderBookManager.getOrderBook(symbol);
        return orderBook.getSellOrders().stream().map(this::convert).collect(Collectors.toList());
    }

    private OrderBookResponse convert(Order order) {
        return new OrderBookResponse(order.getPrice(), order.getRemainingQuantity());
    }
}
