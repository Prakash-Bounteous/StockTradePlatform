package com.trading.platform.admin.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateStockRequest {
    private String symbol;
    private String companyName;
    private BigDecimal price;
    private Long totalShares;
}
