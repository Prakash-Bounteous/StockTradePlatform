package com.trading.platform.watchlist.repository;

import com.trading.platform.watchlist.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    List<Watchlist> findByUserId(Long userId);
    Optional<Watchlist> findByUserIdAndStockId(Long userId, Long stockId);
}
