package com.trading.platform.margin.entity;

import com.trading.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "margin_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarginAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    private Integer marginMultiplier;

    private BigDecimal usedMargin;

    private BigDecimal availableMargin;

    private boolean marginCallTriggered;
}