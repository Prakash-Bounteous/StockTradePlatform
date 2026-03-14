package com.trading.platform.order.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderRequest {
    private String symbol;
    private String side;   // BUY or SELL
    private String type;   // MARKET or LIMIT
    private Long quantity;
    private BigDecimal price; // null for MARKET orders
}
