package com.trading.platform.portfolio.repository;

import com.trading.platform.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByUserIdAndStockId(Long userId, Long stockId);
    List<Portfolio> findByUserId(Long userId);
}
