package com.trading.platform.order.Exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends Throwable {
    public InsufficientBalanceException(BigDecimal totalCost, BigDecimal tradingPower) {
    }
}
