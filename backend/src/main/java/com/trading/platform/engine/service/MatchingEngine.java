package com.trading.platform.engine.service;

import com.trading.platform.engine.OrderBook;
import com.trading.platform.order.entity.Order;
import com.trading.platform.order.model.OrderType;
import com.trading.platform.trade.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngine {

    private final TradeService tradeService;

    /**
     * Called by OrderService WHILE already holding the OrderBook lock.
     * The lock is acquired in OrderService.placeOrder() before addOrder() is called,
     * so this method does NOT lock again — it just runs the matching loop.
     *
     * Flow per order placement:
     *   1. OrderService acquires orderBook.getLock()
     *   2. OrderService calls orderBook.addOrder()        <- inside lock
     *   3. OrderService calls matchingEngine.match()      <- inside same lock
     *   4. OrderService releases lock
     *
     * This guarantees: no two threads can be matching the same stock simultaneously.
     * Different stocks (different OrderBook instances) match fully in parallel.
     */
    public void match(OrderBook orderBook) {
        while (true) {
            Order buyOrder  = orderBook.getBuyOrders().peek();
            Order sellOrder = orderBook.getSellOrders().peek();

            if (buyOrder == null || sellOrder == null) break;
            if (!isMatchPossible(buyOrder, sellOrder)) break;

            executeTrade(orderBook, buyOrder, sellOrder);
        }
    }

    private boolean isMatchPossible(Order buy, Order sell) {
        if (buy.getType() == OrderType.MARKET || sell.getType() == OrderType.MARKET) {
            return true;
        }
        BigDecimal buyPrice  = buy.getPrice()  != null ? buy.getPrice()  : BigDecimal.ZERO;
        BigDecimal sellPrice = sell.getPrice() != null ? sell.getPrice() : BigDecimal.ZERO;
        return buyPrice.compareTo(sellPrice) >= 0;
    }

    private void executeTrade(OrderBook orderBook, Order buy, Order sell) {
        long quantity = Math.min(buy.getRemainingQuantity(), sell.getRemainingQuantity());
        BigDecimal price = sell.getPrice() != null ? sell.getPrice() : buy.getStock().getPrice();

        log.info("[MatchingEngine] Executing trade: {} x {} @ {} (buyer={}, seller={})",
                quantity, buy.getStock().getSymbol(), price,
                buy.getUser().getUsername(), sell.getUser().getUsername());

        tradeService.executeTrade(buy.getUser(), sell.getUser(), buy.getStock(), quantity, price);

        buy.setRemainingQuantity(buy.getRemainingQuantity() - quantity);
        sell.setRemainingQuantity(sell.getRemainingQuantity() - quantity);

        if (buy.getRemainingQuantity() == 0)  orderBook.getBuyOrders().poll();
        if (sell.getRemainingQuantity() == 0) orderBook.getSellOrders().poll();
    }
}
