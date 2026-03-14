package com.trading.platform.market.service;

import com.trading.platform.market.entity.PriceHistory;
import com.trading.platform.market.repository.PriceHistoryRepository;
import com.trading.platform.stock.entity.Stock;
import com.trading.platform.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

            BigDecimal currentPrice = stock.getPrice();

            // FIX: prevent null price crash
            if (currentPrice == null) {
                continue;
            }

            int movement = random.nextInt(3) - 1;

            BigDecimal change =
                    currentPrice.multiply(BigDecimal.valueOf(movement))
                            .divide(BigDecimal.valueOf(100));

            BigDecimal newPrice = currentPrice.add(change);

            if (newPrice.compareTo(BigDecimal.ZERO) > 0) {

                stock.setPrice(newPrice);
                stockRepository.save(stock);

                PriceHistory history = PriceHistory.builder()
                        .stock(stock)
                        .price(newPrice)
                        .timestamp(LocalDateTime.now())
                        .build();

                priceHistoryRepository.save(history);
            }
        }
    }
}