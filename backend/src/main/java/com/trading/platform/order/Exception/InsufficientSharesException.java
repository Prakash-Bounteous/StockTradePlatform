package com.trading.platform.order.Exception;

public class InsufficientSharesException extends Throwable {
    public InsufficientSharesException(String symbol, Long held, Long quantity) {
    }
}
