package com.trading.platform.order.repository;

import com.trading.platform.order.entity.Order;
import com.trading.platform.order.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStockIdAndSideAndStatusOrderByPriceAscCreatedAtAsc(
            Long stockId,
            OrderSide side,
            OrderStatus status
    );
}