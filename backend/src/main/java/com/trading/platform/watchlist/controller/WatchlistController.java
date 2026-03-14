package com.trading.platform.watchlist.controller;

import com.trading.platform.watchlist.entity.Watchlist;
import com.trading.platform.watchlist.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping("/add/{symbol}")
    public String add(Authentication authentication, @PathVariable String symbol) {
        return watchlistService.addToWatchlist(authentication.getName(), symbol);
    }

    @DeleteMapping("/remove/{symbol}")
    public String remove(Authentication authentication, @PathVariable String symbol) {
        return watchlistService.removeFromWatchlist(authentication.getName(), symbol);
    }

    @GetMapping
    public List<Watchlist> get(Authentication authentication) {
        return watchlistService.getWatchlist(authentication.getName());
    }
}
