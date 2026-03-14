package com.trading.platform.admin.controller;

import com.trading.platform.admin.dto.CreateStockRequest;
import com.trading.platform.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/stocks")
@RequiredArgsConstructor
public class AdminStockController {

    private final AdminService adminService;

    @PostMapping("/create")
    public ResponseEntity<String> addStock(@RequestBody CreateStockRequest request) {
        adminService.addStock(request);
        return ResponseEntity.ok("Stock added successfully");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteStock(@PathVariable Long id) {
        adminService.deleteStock(id);
        return ResponseEntity.ok("Stock removed");
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<String> enableTrading(@PathVariable Long id) {
        adminService.enableTrading(id);
        return ResponseEntity.ok("Trading enabled");
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<String> disableTrading(@PathVariable Long id) {
        adminService.disableTrading(id);
        return ResponseEntity.ok("Trading disabled");
    }
}