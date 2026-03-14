package com.trading.platform.admin.controller;

import com.trading.platform.admin.dto.CreateStockRequest;
import com.trading.platform.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/stocks")
@RequiredArgsConstructor
public class AdminStockController {

    private final AdminService adminService;

    @PostMapping("/create")
    public String addStock(@RequestBody CreateStockRequest request) {
        adminService.addStock(request);
        return "Stock added successfully";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteStock(@PathVariable Long id) {
        adminService.deleteStock(id);
        return "Stock removed";
    }

    @PostMapping("/{id}/enable")
    public String enableTrading(@PathVariable Long id) {
        adminService.enableTrading(id);
        return "Trading enabled";
    }

    @PostMapping("/{id}/disable")
    public String disableTrading(@PathVariable Long id) {
        adminService.disableTrading(id);
        return "Trading disabled";
    }
}
