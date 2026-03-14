package com.trading.platform.analytics.controller;

import com.trading.platform.analytics.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public Map<String, BigDecimal> getLeaderboard() {
        return leaderboardService.getLeaderboard();
    }
}
