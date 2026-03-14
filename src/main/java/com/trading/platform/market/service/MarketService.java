package com.trading.platform.market.service;

import com.trading.platform.market.model.MarketStatus;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class MarketService {

    private MarketStatus marketStatus = MarketStatus.CLOSED;

    private final LocalTime marketOpen = LocalTime.of(1, 15);
    private final LocalTime marketClose = LocalTime.of(23, 30);

    public boolean isMarketOpen() {

        LocalTime now = LocalTime.now();

        return now.isAfter(marketOpen) && now.isBefore(marketClose);
    }

    public MarketStatus getMarketStatus() {

        if (isMarketOpen()) {
            marketStatus = MarketStatus.OPEN;
        } else {
            marketStatus = MarketStatus.CLOSED;
        }

        return marketStatus;
    }
}