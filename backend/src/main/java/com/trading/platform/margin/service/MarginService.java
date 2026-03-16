package com.trading.platform.margin.service;

import com.trading.platform.margin.entity.MarginAccount;
import com.trading.platform.margin.repository.MarginAccountRepository;
import com.trading.platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarginService {

    private final MarginAccountRepository marginAccountRepository;

    /**
     * Returns or creates a margin account for the user.
     * Default: 2x multiplier, synced to user's actual cash balance.
     */
    public MarginAccount getOrCreateAccount(User user) {
        return marginAccountRepository.findByUser(user)
                .orElseGet(() -> {
                    MarginAccount account = MarginAccount.builder()
                            .user(user)
                            .marginMultiplier(2)
                            .availableMargin(user.getBalance())
                            .usedMargin(BigDecimal.ZERO)
                            .marginCallTriggered(false)
                            .build();
                    log.info("[MarginService] Created margin account for user: {}", user.getUsername());
                    return marginAccountRepository.save(account);
                });
    }

    /**
     * Trading power = user's cash balance × margin multiplier
     *
     * Example:
     *   balance = ₹1,00,000
     *   multiplier = 2
     *   trading power = ₹2,00,000
     */
    public BigDecimal getTradingPower(User user) {
        MarginAccount account = getOrCreateAccount(user);
        int multiplier = account.getMarginMultiplier() == null ? 1 : account.getMarginMultiplier();

        // Always based on current cash balance, not stale availableMargin field
        BigDecimal tradingPower = user.getBalance()
                .multiply(BigDecimal.valueOf(multiplier));

        log.info("[MarginService] {} trading power: ₹{} (balance ₹{} × {}x)",
                user.getUsername(), tradingPower, user.getBalance(), multiplier);

        return tradingPower;
    }

    /**
     * Check if user has enough margin to place a buy order
     */
    public boolean hasEnoughMargin(User user, BigDecimal requiredAmount) {
        BigDecimal tradingPower = getTradingPower(user);
        boolean enough = tradingPower.compareTo(requiredAmount) >= 0;
        if (!enough) {
            log.warn("[MarginService] {} insufficient margin: needs ₹{} has ₹{}",
                    user.getUsername(), requiredAmount, tradingPower);
        }
        return enough;
    }

    /**
     * Update usedMargin after a trade executes.
     * usedMargin tracks how much borrowed money is currently in use.
     *
     * Example:
     *   balance = ₹1,00,000, multiplier = 2x
     *   user buys ₹1,50,000 of stock
     *   own money used   = ₹1,00,000
     *   borrowed money   = ₹50,000   ← this is usedMargin
     */
    public void updateUsedMargin(User user, BigDecimal tradeValue, boolean isBuy) {
        MarginAccount account = getOrCreateAccount(user);
        BigDecimal ownMoney = user.getBalance();

        if (isBuy) {
            // How much of this trade exceeded their own balance (borrowed portion)
            BigDecimal borrowed = tradeValue.subtract(ownMoney);
            BigDecimal newUsed = account.getUsedMargin().add(
                    borrowed.compareTo(BigDecimal.ZERO) > 0 ? borrowed : BigDecimal.ZERO
            );
            account.setUsedMargin(newUsed);
        } else {
            // Selling reduces used margin
            BigDecimal newUsed = account.getUsedMargin().subtract(tradeValue);
            account.setUsedMargin(
                    newUsed.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newUsed
            );
        }

        account.setAvailableMargin(user.getBalance());
        marginAccountRepository.save(account);
    }
}