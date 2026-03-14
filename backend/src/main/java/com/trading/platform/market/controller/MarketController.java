package com.trading.platform.market.controller;

import com.trading.platform.market.model.MarketStatus;
import com.trading.platform.market.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    @GetMapping("/status")
    public MarketStatus getMarketStatus() {
        return marketService.getMarketStatus();
    }
}
