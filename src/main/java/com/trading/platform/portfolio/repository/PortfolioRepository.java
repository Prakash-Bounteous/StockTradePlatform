package com.trading.platform.portfolio.repository;

import com.trading.platform.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByUserIdAndStockId(
            Long userId,
            Long stockId
    );

    List<Portfolio> findByUserId(Long userId);
}