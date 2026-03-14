package com.trading.platform.engine;

import com.trading.platform.order.entity.Order;
import com.trading.platform.order.model.OrderSide;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

public class OrderBook {

    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;

    // Fair ReentrantLock — one lock per stock symbol
    // RELIANCE orders never block TCS orders
    private final ReentrantLock lock = new ReentrantLock(true);

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

    public void addOrder(Order order) {
        lock.lock();
        try {
            if (order.getSide() == OrderSide.BUY) buyOrders.add(order);
            else sellOrders.add(order);
        } finally {
            lock.unlock();
        }
    }

    public void removeOrder(Order order) {
        lock.lock();
        try {
            if (order.getSide() == OrderSide.BUY) buyOrders.remove(order);
            else sellOrders.remove(order);
        } finally {
            lock.unlock();
        }
    }

    // Expose lock so MatchingEngine holds it across add + match atomically
    public ReentrantLock getLock() {
        return lock;
    }

    // Only call these while holding the lock
    public PriorityQueue<Order> getBuyOrders() {
        return buyOrders;
    }

    public PriorityQueue<Order> getSellOrders() {
        return sellOrders;
    }
}
