package com.trading.platform.market.service;

import com.trading.platform.stock.entity.Stock;
import com.trading.platform.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CircuitBreakerService {

    private final StockRepository stockRepository;

    @Scheduled(fixedRate = 10000)
    public void checkCircuitBreakers() {
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            BigDecimal price = stock.getPrice();
            if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
                stock.setTradable(false);
                stockRepository.save(stock);
            }
        }
    }
}
