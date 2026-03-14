package com.trading.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Thread pool not needed for order placement
    // Concurrency is handled by per-stock ReentrantLock in OrderService
    // @Async removed to avoid SecurityContext propagation issues
}