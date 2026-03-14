package com.trading.platform.bot.service;

import com.trading.platform.bot.entity.TradingBot;
import com.trading.platform.bot.repository.TradingBotRepository;
import com.trading.platform.order.dto.PlaceOrderRequest;
import com.trading.platform.order.model.OrderSide;
import com.trading.platform.order.model.OrderType;
import com.trading.platform.order.service.OrderService;
import com.trading.platform.stock.entity.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TradingBotService {

    private final TradingBotRepository tradingBotRepository;
    private final OrderService orderService;

    @Scheduled(fixedRate = 5000)
    public void runBots() {

        List<TradingBot> bots = tradingBotRepository.findByActiveTrue();

        for (TradingBot bot : bots) {

            Stock stock = bot.getStock();

            if (stock.getPrice().compareTo(bot.getTriggerPrice()) <= 0) {

                PlaceOrderRequest request = new PlaceOrderRequest();

                request.setStockSymbol(stock.getSymbol());
                request.setQuantity(bot.getQuantity());
                request.setSide(OrderSide.BUY);
                request.setType(OrderType.MARKET);

                orderService.placeOrder(
                        bot.getUser().getUsername(),
                        request
                );

                bot.setActive(false);

                tradingBotRepository.save(bot);
            }
        }
    }
}