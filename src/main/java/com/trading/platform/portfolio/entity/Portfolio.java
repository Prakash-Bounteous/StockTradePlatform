package com.trading.platform.portfolio.entity;

import com.trading.platform.stock.entity.Stock;
import com.trading.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "portfolio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Stock stock;

    private Long quantity;

    private BigDecimal averagePrice;
}