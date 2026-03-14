package com.trading.platform.engine.service;

import com.trading.platform.engine.OrderBook;
import com.trading.platform.order.entity.Order;
import com.trading.platform.order.model.OrderType;
import com.trading.platform.trade.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MatchingEngine {

    private final TradeService tradeService;

    public void match(OrderBook orderBook) {

        while (true) {

            Order buyOrder = orderBook.getBuyOrders().peek();
            Order sellOrder = orderBook.getSellOrders().peek();

            if (buyOrder == null || sellOrder == null) {
                break;
            }

            if (!isMatchPossible(buyOrder, sellOrder)) {
                break;
            }

            executeTrade(orderBook, buyOrder, sellOrder);
        }
    }

    private boolean isMatchPossible(Order buy, Order sell) {

        if (buy.getType() == OrderType.MARKET
                || sell.getType() == OrderType.MARKET) {
            return true;
        }

        BigDecimal buyPrice = buy.getPrice();
        BigDecimal sellPrice = sell.getPrice();

        return buyPrice.compareTo(sellPrice) >= 0;
    }

    private void executeTrade(
            OrderBook orderBook,
            Order buy,
            Order sell
    ) {

        long quantity =
                Math.min(
                        buy.getRemainingQuantity(),
                        sell.getRemainingQuantity()
                );

        BigDecimal price = sell.getPrice();

        tradeService.executeTrade(
                buy.getUser(),
                sell.getUser(),
                buy.getStock(),
                quantity,
                price
        );

        buy.setRemainingQuantity(
                buy.getRemainingQuantity() - quantity
        );

        sell.setRemainingQuantity(
                sell.getRemainingQuantity() - quantity
        );

        if (buy.getRemainingQuantity() == 0) {
            orderBook.getBuyOrders().poll();
        }

        if (sell.getRemainingQuantity() == 0) {
            orderBook.getSellOrders().poll();
        }
    }
}