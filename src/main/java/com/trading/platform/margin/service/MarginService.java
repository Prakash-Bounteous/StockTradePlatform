package com.trading.platform.margin.service;

import com.trading.platform.user.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MarginService {

    public BigDecimal getTradingPower(User user) {

        return user.getBalance()
                .multiply(user.getMarginMultiplier());
    }

    public boolean hasEnoughMargin(User user, BigDecimal orderValue) {

        BigDecimal tradingPower = getTradingPower(user);

        BigDecimal used = user.getUsedMargin() == null
                ? BigDecimal.ZERO
                : user.getUsedMargin();

        BigDecimal remainingPower = tradingPower.subtract(used);

        return remainingPower.compareTo(orderValue) >= 0;
    }
}