package com.trading.platform.common.util;

import java.time.LocalTime;

public class MarketTimeUtil {

    private static final LocalTime OPEN = LocalTime.of(9, 15);
    private static final LocalTime CLOSE = LocalTime.of(15, 30);

    public static boolean isMarketOpen() {

        LocalTime now = LocalTime.now();

        return now.isAfter(OPEN) && now.isBefore(CLOSE);
    }
}