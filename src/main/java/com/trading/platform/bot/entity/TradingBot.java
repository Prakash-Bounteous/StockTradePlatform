package com.trading.platform.bot.entity;

import com.trading.platform.stock.entity.Stock;
import com.trading.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "trading_bots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradingBot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Stock stock;

    private BigDecimal triggerPrice;

    private Long quantity;

    private boolean active;
}