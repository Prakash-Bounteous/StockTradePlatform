package com.trading.platform.margin.entity;

import com.trading.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "margin_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarginAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    private Integer marginMultiplier = 2;

    @Builder.Default
    private BigDecimal usedMargin = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal availableMargin = BigDecimal.valueOf(100000);

    private boolean marginCallTriggered = false;
}
