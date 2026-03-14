package com.trading.platform.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderBookResponse {
    private BigDecimal price;
    private Long quantity;
}
