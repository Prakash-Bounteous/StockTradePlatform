package com.trading.platform.margin.service;

import com.trading.platform.margin.entity.MarginAccount;
import com.trading.platform.margin.repository.MarginAccountRepository;
import com.trading.platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MarginService {

    private final MarginAccountRepository marginAccountRepository;

    public BigDecimal getTradingPower(User user) {
        MarginAccount account = marginAccountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Margin account not found"));
        BigDecimal available = account.getAvailableMargin() == null ? BigDecimal.ZERO : account.getAvailableMargin();
        Integer multiplier = account.getMarginMultiplier() == null ? 1 : account.getMarginMultiplier();
        return available.multiply(BigDecimal.valueOf(multiplier));
    }

    public boolean hasEnoughMargin(User user, BigDecimal requiredAmount) {
        return getTradingPower(user).compareTo(requiredAmount) >= 0;
    }
}
