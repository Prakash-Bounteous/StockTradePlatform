//package com.trading.platform.engine.service;
//
//import com.trading.platform.engine.OrderBook;
//import com.trading.platform.order.entity.Order;
//import com.trading.platform.order.model.OrderType;
//import com.trading.platform.trade.service.TradeService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.concurrent.locks.ReentrantLock;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class MatchingEngine {
//
//    private final TradeService tradeService;
//
//    /**
//     * Called by OrderService WHILE already holding the OrderBook lock.
//     * The lock is acquired in OrderService.placeOrder() before addOrder() is called,
//     * so this method does NOT lock again — it just runs the matching loop.
//     *
//     * Flow per order placement:
//     *   1. OrderService acquires orderBook.getLock()
//     *   2. OrderService calls orderBook.addOrder()        <- inside lock
//     *   3. OrderService calls matchingEngine.match()      <- inside same lock
//     *   4. OrderService releases lock
//     *
//     * This guarantees: no two threads can be matching the same stock simultaneously.
//     * Different stocks (different OrderBook instances) match fully in parallel.
//     */
//    public void match(OrderBook orderBook) {
//        while (true) {
//            Order buyOrder  = orderBook.getBuyOrders().peek();
//            Order sellOrder = orderBook.getSellOrders().peek();
//
//            if (buyOrder == null || sellOrder == null) break;
//            if (!isMatchPossible(buyOrder, sellOrder)) break;
//
//            executeTrade(orderBook, buyOrder, sellOrder);
//        }
//    }
//
//    private boolean isMatchPossible(Order buy, Order sell) {
//        if (buy.getType() == OrderType.MARKET || sell.getType() == OrderType.MARKET) {
//            return true;
//        }
//        BigDecimal buyPrice  = buy.getPrice()  != null ? buy.getPrice()  : BigDecimal.ZERO;
//        BigDecimal sellPrice = sell.getPrice() != null ? sell.getPrice() : BigDecimal.ZERO;
//        return buyPrice.compareTo(sellPrice) >= 0;
//    }
//
//    private void executeTrade(OrderBook orderBook, Order buy, Order sell) {
//        long quantity = Math.min(buy.getRemainingQuantity(), sell.getRemainingQuantity());
//        BigDecimal price = sell.getPrice() != null ? sell.getPrice() : buy.getStock().getPrice();
//
//        log.info("[MatchingEngine] Executing trade: {} x {} @ {} (buyer={}, seller={})",
//                quantity, buy.getStock().getSymbol(), price,
//                buy.getUser().getUsername(), sell.getUser().getUsername());
//
//        tradeService.executeTrade(buy.getUser(), sell.getUser(), buy.getStock(), quantity, price);
//
//        buy.setRemainingQuantity(buy.getRemainingQuantity() - quantity);
//        sell.setRemainingQuantity(sell.getRemainingQuantity() - quantity);
//
//        if (buy.getRemainingQuantity() == 0)  orderBook.getBuyOrders().poll();
//        if (sell.getRemainingQuantity() == 0) orderBook.getSellOrders().poll();
//    }
//}



package com.trading.platform.engine.service;

import com.trading.platform.engine.OrderBook;
import com.trading.platform.order.entity.Order;
import com.trading.platform.order.model.OrderStatus;
import com.trading.platform.order.model.OrderType;
import com.trading.platform.order.repository.OrderRepository;
import com.trading.platform.trade.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.PriorityQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngine {

    private final TradeService    tradeService;
    private final OrderRepository orderRepository;

    /**
     * MARKET ORDER BEHAVIOUR:
     *   - Walks the opposite side of the book (best price first)
     *   - Consumes limit orders one by one until quantity is filled
     *   - If not enough sellers/buyers → fills what is available
     *   - Remaining unfilled quantity → CANCELLED (not stored in book)
     *   - Market orders NEVER stay in the order book
     *
     * LIMIT ORDER BEHAVIOUR:
     *   - Matches only if buyPrice >= sellPrice
     *   - If no match → stays in book and waits
     *   - Can be partially filled and stay in book for the rest
     */
    public void match(OrderBook orderBook) {

        while (true) {
            Order buyOrder  = orderBook.getBuyOrders().peek();
            Order sellOrder = orderBook.getSellOrders().peek();

            if (buyOrder == null || sellOrder == null) break;
            if (!isMatchPossible(buyOrder, sellOrder))  break;

            executeTrade(orderBook, buyOrder, sellOrder);
        }

        // After matching — remove any leftover MARKET orders from book
        // Market orders must never wait — cancel unfilled remainder
        cancelUnfilledMarketOrders(orderBook.getBuyOrders());
        cancelUnfilledMarketOrders(orderBook.getSellOrders());
    }

    // ─────────────────────────────────────────────────────────────
    // Can these two orders match?
    // ─────────────────────────────────────────────────────────────
    private boolean isMatchPossible(Order buy, Order sell) {

        // MARKET vs anything → always match (market taker accepts any price)
        if (buy.getType()  == OrderType.MARKET) return true;
        if (sell.getType() == OrderType.MARKET) return true;

        // LIMIT vs LIMIT → match only if buyPrice >= sellPrice
        return buy.getPrice().compareTo(sell.getPrice()) >= 0;
    }

    // ─────────────────────────────────────────────────────────────
    // Execute one trade between top buy and top sell
    // ─────────────────────────────────────────────────────────────
    private void executeTrade(OrderBook orderBook, Order buy, Order sell) {

        long quantity = Math.min(
                buy.getRemainingQuantity(),
                sell.getRemainingQuantity()
        );

        BigDecimal tradePrice = determineTradePrice(buy, sell);

        log.info("[MatchingEngine] {} x {} @ ₹{} | buyer={} seller={}",
                quantity, buy.getStock().getSymbol(), tradePrice,
                buy.getUser().getUsername(), sell.getUser().getUsername());

        // Execute — updates balances, portfolio, notifications
        tradeService.executeTrade(
                buy.getUser(), sell.getUser(),
                buy.getStock(), quantity, tradePrice
        );

        // Update remaining quantities
        buy.setRemainingQuantity(buy.getRemainingQuantity()   - quantity);
        sell.setRemainingQuantity(sell.getRemainingQuantity() - quantity);

        // Remove fully filled orders from book and mark FILLED
        if (buy.getRemainingQuantity() == 0) {
            orderBook.getBuyOrders().poll();
            buy.setStatus(OrderStatus.FILLED);
            orderRepository.save(buy);
        }
        if (sell.getRemainingQuantity() == 0) {
            orderBook.getSellOrders().poll();
            sell.setStatus(OrderStatus.FILLED);
            orderRepository.save(sell);
        }

        // Partially filled LIMIT orders — update status, stay in book
        if (buy.getRemainingQuantity() > 0 && buy.getType() == OrderType.LIMIT) {
            buy.setStatus(OrderStatus.PARTIALLY_FILLED);
            orderRepository.save(buy);
        }
        if (sell.getRemainingQuantity() > 0 && sell.getType() == OrderType.LIMIT) {
            sell.setStatus(OrderStatus.PARTIALLY_FILLED);
            orderRepository.save(sell);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Price rules:
    //   MARKET BUY  + LIMIT SELL  → sell's limit price
    //   LIMIT BUY   + MARKET SELL → buy's limit price
    //   MARKET BUY  + MARKET SELL → current stock price
    //   LIMIT BUY   + LIMIT SELL  → sell's limit price (standard rule)
    // ─────────────────────────────────────────────────────────────
    private BigDecimal determineTradePrice(Order buy, Order sell) {
        boolean buyMarket  = buy.getType()  == OrderType.MARKET;
        boolean sellMarket = sell.getType() == OrderType.MARKET;

        if (buyMarket && !sellMarket)  return sell.getPrice();
        if (!buyMarket && sellMarket)  return buy.getPrice();
        if (!buyMarket)                return sell.getPrice(); // both limit
        return buy.getStock().getPrice();                      // both market
    }

    // ─────────────────────────────────────────────────────────────
    // Market orders must NEVER stay in the book after matching.
    // Cancel any remaining unfilled market order quantity.
    //
    // Example:
    //   MARKET BUY 10 shares
    //   Only 7 sell orders available → 7 filled
    //   Remaining 3 → CANCELLED (not stored)
    // ─────────────────────────────────────────────────────────────
    private void cancelUnfilledMarketOrders(PriorityQueue<Order> queue) {
        queue.removeIf(order -> {
            if (order.getType() != OrderType.MARKET) return false;

            long filled = order.getQuantity() - order.getRemainingQuantity();

            if (filled > 0) {
                order.setStatus(OrderStatus.PARTIALLY_FILLED);
                log.info("[MatchingEngine] MARKET order {} — {}/{} filled, remainder CANCELLED",
                        order.getId(), filled, order.getQuantity());
            } else {
                order.setStatus(OrderStatus.CANCELLED);
                log.warn("[MatchingEngine] MARKET order {} — no sellers available, CANCELLED",
                        order.getId());
            }

            order.setRemainingQuantity(0L);
            orderRepository.save(order);
            return true; // remove from queue
        });
    }
}
