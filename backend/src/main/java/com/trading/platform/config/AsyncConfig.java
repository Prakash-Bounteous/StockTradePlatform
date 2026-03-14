package com.trading.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Dedicated thread pool for order execution.
     *
     * - corePoolSize  = 10  : always 10 threads alive, ready to handle bursts
     * - maxPoolSize   = 50  : can grow up to 50 under heavy load
     * - queueCapacity = 500 : up to 500 orders queued before rejection
     * - threadNamePrefix    : makes logs readable ("orderExecutor-1", etc.)
     *
     * Why a separate pool?
     *   Order matching involves DB writes + in-memory locking.
     *   Keeping it off the default Tomcat HTTP thread pool means the web
     *   server stays responsive even when 50 orders are matching in parallel.
     */
    @Bean(name = "orderExecutor")
    public Executor orderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("orderExecutor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
