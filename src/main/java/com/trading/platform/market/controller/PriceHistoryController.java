package com.trading.platform.market.controller;

import com.trading.platform.market.entity.PriceHistory;
import com.trading.platform.market.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prices")
@RequiredArgsConstructor
public class PriceHistoryController {

    private final PriceHistoryRepository priceHistoryRepository;

    @GetMapping("/{symbol}")
    public List<PriceHistory> getPriceHistory(@PathVariable String symbol) {

        return priceHistoryRepository.findByStockSymbolOrderByTimestampAsc(symbol);
    }
}