package com.trading.platform.trade.repository;

import com.trading.platform.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByBuyerIdOrSellerIdOrderByExecutedAtDesc(Long buyerId, Long sellerId);
}
