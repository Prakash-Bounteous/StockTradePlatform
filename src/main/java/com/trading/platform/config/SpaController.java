package com.trading.platform.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({
            "/",
            "/dashboard",
            "/trade",
            "/portfolio",
            "/watchlist",
            "/notifications",
            "/leaderboard",
            "/admin"
    })
    public String forward() {

        return "forward:/index.html";

    }
}