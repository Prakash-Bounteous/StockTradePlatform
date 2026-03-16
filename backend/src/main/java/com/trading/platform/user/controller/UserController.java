package com.trading.platform.user.controller;

import com.trading.platform.notification.service.NotificationService;
import com.trading.platform.user.dto.UserProfileResponse;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final BigDecimal MIN_DEPOSIT  = BigDecimal.valueOf(100);
    private static final BigDecimal MAX_DEPOSIT  = BigDecimal.valueOf(10_00_000);
    private static final BigDecimal MAX_BALANCE  = BigDecimal.valueOf(1_00_00_000);
    private static final BigDecimal MIN_WITHDRAW = BigDecimal.valueOf(100);

    @GetMapping("/me")
    public UserProfileResponse getMe(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setBalance(user.getBalance());
        response.setRole(user.getRole() != null ? user.getRole().name() : "USER");
        return response;
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(Authentication authentication,
                                     @RequestBody Map<String, Object> body) {
        try {
            BigDecimal amount;
            try {
                amount = new BigDecimal(body.get("amount").toString());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid amount"));
            }

            if (amount.compareTo(MIN_DEPOSIT) < 0)
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Minimum deposit is ₹" + MIN_DEPOSIT));

            if (amount.compareTo(MAX_DEPOSIT) > 0)
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Maximum deposit per transaction is ₹" + MAX_DEPOSIT));

            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();

            BigDecimal newBalance = user.getBalance().add(amount);
            if (newBalance.compareTo(MAX_BALANCE) > 0)
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Balance cannot exceed ₹1,00,00,000. Current: ₹" + user.getBalance()));

            user.setBalance(newBalance);
            userRepository.save(user);

            notificationService.createNotification(user,
                    "💰 ₹" + amount.toPlainString() + " deposited. New balance: ₹" + newBalance.toPlainString());

            log.info("[Deposit] {} deposited ₹{} | balance: ₹{}", user.getUsername(), amount, newBalance);

            return ResponseEntity.ok(Map.of(
                    "message", "₹" + amount.toPlainString() + " deposited successfully",
                    "newBalance", newBalance
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Deposit failed"));
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(Authentication authentication,
                                      @RequestBody Map<String, Object> body) {
        try {
            BigDecimal amount;
            try {
                amount = new BigDecimal(body.get("amount").toString());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid amount"));
            }

            if (amount.compareTo(MIN_WITHDRAW) < 0)
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Minimum withdrawal is ₹" + MIN_WITHDRAW));

            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();

            if (user.getBalance().compareTo(amount) < 0)
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Insufficient balance. Available: ₹" + user.getBalance()));

            BigDecimal newBalance = user.getBalance().subtract(amount);
            user.setBalance(newBalance);
            userRepository.save(user);

            notificationService.createNotification(user,
                    "🏧 ₹" + amount.toPlainString() + " withdrawn. Remaining balance: ₹" + newBalance.toPlainString());

            log.info("[Withdraw] {} withdrew ₹{} | balance: ₹{}", user.getUsername(), amount, newBalance);

            return ResponseEntity.ok(Map.of(
                    "message", "₹" + amount.toPlainString() + " withdrawn successfully",
                    "newBalance", newBalance
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Withdrawal failed"));
        }
    }
}