package com.trading.platform.margin.service;

import com.trading.platform.user.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MarginCallService {

    public boolean isMarginCall(User user) {

        BigDecimal equity = user.getBalance();

        if (equity.compareTo(BigDecimal.ZERO) < 0) {

            System.out.println(
                    "Margin Call Triggered for user: "
                            + user.getId()
            );

            return true;
        }

        return false;
    }
}