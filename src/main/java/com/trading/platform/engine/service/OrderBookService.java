package com.trading.platform.engine.service;

import com.trading.platform.order.entity.Order;
import com.trading.platform.order.model.OrderSide;
import com.trading.platform.order.model.OrderStatus;
import com.trading.platform.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderBookService {

    private final OrderRepository orderRepository;
    private final MatchingEngine matchingEngine;

    public void processOrder(Order newOrder) {

        if (newOrder.getSide() == OrderSide.BUY) {

            List<Order> sellOrders =
                    orderRepository
                            .findByStockIdAndSideAndStatusOrderByPriceAscCreatedAtAsc(
                                    newOrder.getStock().getId(),
                                    OrderSide.SELL,
                                    OrderStatus.PENDING
                            );

            for (Order sellOrder : sellOrders) {

                if (newOrder.getRemainingQuantity() == 0) {
                    break;
                }

                if (newOrder.getPrice()
                        .compareTo(sellOrder.getPrice()) >= 0) {

                    matchingEngine.matchOrders(newOrder, sellOrder);
                }
            }
        }
    }
}