package com.trading.platform.engine;

import com.trading.platform.order.entity.Order;
import com.trading.platform.order.model.OrderSide;

import java.util.Comparator;
import java.util.PriorityQueue;

public class OrderBook {

    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;

    public OrderBook() {

        buyOrders = new PriorityQueue<>(
                Comparator.comparing(Order::getPrice).reversed()
                        .thenComparing(Order::getCreatedAt)
        );

        sellOrders = new PriorityQueue<>(
                Comparator.comparing(Order::getPrice)
                        .thenComparing(Order::getCreatedAt)
        );
    }

    public synchronized void addOrder(Order order) {

        if (order.getSide() == OrderSide.BUY) {
            buyOrders.add(order);
        } else {
            sellOrders.add(order);
        }
    }

    public synchronized PriorityQueue<Order> getBuyOrders() {
        return buyOrders;
    }

    public synchronized PriorityQueue<Order> getSellOrders() {
        return sellOrders;
    }
}