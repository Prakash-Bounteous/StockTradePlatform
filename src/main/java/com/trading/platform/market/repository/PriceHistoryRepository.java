package com.trading.platform.market.repository;

import com.trading.platform.market.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findByStockSymbolOrderByTimestampAsc(String symbol);
}