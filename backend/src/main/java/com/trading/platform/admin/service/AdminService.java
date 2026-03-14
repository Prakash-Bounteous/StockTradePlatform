package com.trading.platform.admin.service;

import com.trading.platform.admin.dto.CreateStockRequest;
import com.trading.platform.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final StockService stockService;

    public void addStock(CreateStockRequest request) { stockService.createStock(request); }
    public void deleteStock(Long stockId) { stockService.deleteStock(stockId); }
    public void enableTrading(Long stockId) { stockService.enableTrading(stockId); }
    public void disableTrading(Long stockId) { stockService.disableTrading(stockId); }
}
