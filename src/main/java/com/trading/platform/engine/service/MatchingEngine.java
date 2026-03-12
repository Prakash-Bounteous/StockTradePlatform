package com.trading.platform.engine.service;

import com.trading.platform.order.entity.Order;
import com.trading.platform.order.model.OrderStatus;
import com.trading.platform.order.repository.OrderRepository;
import com.trading.platform.trade.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MatchingEngine {

    private final OrderRepository orderRepository;
    private final TradeService tradeService;

    public void matchOrders(Order buyOrder, Order sellOrder) {

        Long tradeQuantity = Math.min(
                buyOrder.getRemainingQuantity(),
                sellOrder.getRemainingQuantity()
        );

        BigDecimal price = sellOrder.getPrice();

        tradeService.executeTrade(
                buyOrder.getUser(),
                sellOrder.getUser(),
                buyOrder.getStock(),
                tradeQuantity,
                price
        );

        buyOrder.setRemainingQuantity(
                buyOrder.getRemainingQuantity() - tradeQuantity
        );

        sellOrder.setRemainingQuantity(
                sellOrder.getRemainingQuantity() - tradeQuantity
        );

        updateOrderStatus(buyOrder);
        updateOrderStatus(sellOrder);

        orderRepository.save(buyOrder);
        orderRepository.save(sellOrder);
    }

    private void updateOrderStatus(Order order) {

        if (order.getRemainingQuantity() == 0) {
            order.setStatus(OrderStatus.EXECUTED);
        } else {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
        }
    }
}