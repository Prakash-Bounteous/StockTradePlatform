package com.trading.platform.market.service;

import com.trading.platform.market.entity.PriceHistory;
import com.trading.platform.market.repository.PriceHistoryRepository;
import com.trading.platform.stock.entity.Stock;
import com.trading.platform.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PriceSimulationService {

    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final Random random = new Random();

    @Scheduled(fixedRate = 5000)
    public void simulatePrices() {
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            if (!stock.isTradable()) continue;
            BigDecimal currentPrice = stock.getPrice();
            if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) continue;

            // Random movement: -1%, 0, or +1%
            double movement = (random.nextDouble() * 2 - 1) * 0.015; // ±1.5%
            BigDecimal change = currentPrice.multiply(BigDecimal.valueOf(movement)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal newPrice = currentPrice.add(change).setScale(2, RoundingMode.HALF_UP);

            if (newPrice.compareTo(BigDecimal.ONE) > 0) {
                stock.setPrice(newPrice);
                stockRepository.save(stock);

                priceHistoryRepository.save(PriceHistory.builder()
                        .stock(stock).price(newPrice).timestamp(LocalDateTime.now()).build());
            }
        }
    }
}
