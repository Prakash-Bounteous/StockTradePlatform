package com.trading.platform.bot.repository;

import com.trading.platform.bot.entity.TradingBot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradingBotRepository extends JpaRepository<TradingBot, Long> {

    List<TradingBot> findByActiveTrue();
}