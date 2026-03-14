package com.trading.platform.watchlist.entity;

import com.trading.platform.stock.entity.Stock;
import com.trading.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "watchlist",
        uniqueConstraints =
        @UniqueConstraint(
                columnNames = {"user_id", "stock_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Stock stock;
}