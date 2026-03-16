package com.trading.platform.market.service;

import com.trading.platform.market.model.MarketStatus;
import org.springframework.stereotype.Service;
import java.time.LocalTime;

@Service
public class MarketService {
    private final LocalTime marketOpen = LocalTime.of(1, 15);
    private final LocalTime marketClose = LocalTime.of(23, 59);

    public boolean isMarketOpen() {
        LocalTime now = LocalTime.now();
        return now.isAfter(marketOpen) && now.isBefore(marketClose);
    }

    public MarketStatus getMarketStatus() {
        return isMarketOpen() ? MarketStatus.OPEN : MarketStatus.CLOSED;
    }
}
