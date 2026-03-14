package com.trading.platform.watchlist.service;

import com.trading.platform.stock.entity.Stock;
import com.trading.platform.stock.repository.StockRepository;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import com.trading.platform.watchlist.entity.Watchlist;
import com.trading.platform.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    public String addToWatchlist(String username, String symbol) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        Stock stock = stockRepository
                .findBySymbol(symbol)
                .orElseThrow();

        watchlistRepository
                .findByUserIdAndStockId(user.getId(), stock.getId())
                .ifPresent(w -> {
                    throw new RuntimeException("Already in watchlist");
                });

        Watchlist watchlist = Watchlist.builder()
                .user(user)
                .stock(stock)
                .build();

        watchlistRepository.save(watchlist);

        return "Stock added to watchlist";
    }

    public List<Watchlist> getWatchlist(String username) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        return watchlistRepository.findByUserId(user.getId());
    }

    public String removeFromWatchlist(String username, String symbol) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();

        Stock stock = stockRepository
                .findBySymbol(symbol)
                .orElseThrow();

        Watchlist watchlist =
                watchlistRepository
                        .findByUserIdAndStockId(user.getId(), stock.getId())
                        .orElseThrow();

        watchlistRepository.delete(watchlist);

        return "Stock removed from watchlist";
    }
}