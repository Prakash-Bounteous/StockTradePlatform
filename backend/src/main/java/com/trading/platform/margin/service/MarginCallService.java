package com.trading.platform.margin.service;

import com.trading.platform.margin.entity.MarginAccount;
import com.trading.platform.margin.repository.MarginAccountRepository;
import com.trading.platform.notification.service.NotificationService;
import com.trading.platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarginCallService {

    private final MarginAccountRepository marginAccountRepository;
    private final NotificationService notificationService;

    /**
     * Called after every trade execution.
     *
     * Margin call triggers when user's balance goes below 0
     * meaning they've lost more than their own capital —
     * they owe money on their borrowed margin.
     *
     * In real exchanges this would force-liquidate positions.
     * Here we notify the user and flag the account.
     */
    public boolean checkAndTrigger(User user) {
        if (user.getBalance().compareTo(BigDecimal.ZERO) < 0) {

            log.warn("[MarginCall] TRIGGERED for user: {} | balance: ₹{}",
                    user.getUsername(), user.getBalance());

            // Flag the margin account
            marginAccountRepository.findByUser(user).ifPresent(account -> {
                if (!account.isMarginCallTriggered()) {
                    account.setMarginCallTriggered(true);
                    marginAccountRepository.save(account);
                }
            });

            // Notify the user immediately
            notificationService.createNotification(user,
                    "⚠️ MARGIN CALL: Your balance is ₹" + user.getBalance()
                            + ". Your positions may be at risk. Please add funds or close positions.");

            return true;
        }

        // Balance recovered — clear the margin call flag
        marginAccountRepository.findByUser(user).ifPresent(account -> {
            if (account.isMarginCallTriggered()) {
                account.setMarginCallTriggered(false);
                marginAccountRepository.save(account);
                notificationService.createNotification(user,
                        "✅ Margin call resolved. Balance restored to ₹" + user.getBalance());
            }
        });

        return false;
    }
}