package com.starfleet.gamifier.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Configuration for Spring Cache abstraction.
 * Provides both request-scoped and application-level caching for performance optimization.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Request-scoped cache manager that creates a new cache for each HTTP request.
     * The cache is automatically cleared when the request completes.
     *
     * @return CacheManager scoped to the current request
     */
    @Bean
    @RequestScope
    public CacheManager requestScopedCacheManager() {
        return new ConcurrentMapCacheManager("organizationById");
    }

    /**
     * Application-level cache manager for long-lived data like leaderboards.
     * These caches persist across requests and provide better performance for expensive operations.
     *
     * @return Primary CacheManager for application-level caching
     */
    @Bean
    @Primary
    public CacheManager applicationCacheManager() {
        return new ConcurrentMapCacheManager(
            "monthlyLeaderboard",
            "allTimeLeaderboard",
            "leaderboardStats",
            "userRankings",
            "eventStatistics"
        );
    }
}