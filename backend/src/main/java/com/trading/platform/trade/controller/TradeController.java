package com.trading.platform.trade.controller;

import com.trading.platform.trade.entity.Trade;
import com.trading.platform.trade.service.TradeService;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final UserRepository userRepository;

    @GetMapping("/my")
    public List<Trade> getMyTrades(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return tradeService.getTradesForUser(user.getId());
    }
}
