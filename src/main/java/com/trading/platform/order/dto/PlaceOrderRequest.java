package com.trading.platform.order.dto;

import com.trading.platform.order.model.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlaceOrderRequest {

    private String stockSymbol;

    private Long quantity;

    private OrderSide side;

    private OrderType type;

    private BigDecimal price; // for LIMIT order
}