package ca.zhoozhoo.loaddev.api.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/// Configuration for Spring Cache using Caffeine as the cache implementation.
///
/// This configuration provides caching support for UMA permission tokens to reduce
/// unnecessary token exchange calls to Keycloak. The cache uses Caffeine's high-performance
/// implementation with automatic expiration and size limits.
///
/// **Cache Configuration:**
///
/// - Implementation: Caffeine (supports CompletableFuture for reactive caching)
/// - TTL: 5 minutes (90% of typical token expiry time)
/// - Max Size: 1000 entries
/// - Eviction: Size-based with W-TinyLFU (Window Tiny Least Frequently Used)
/// - Statistics: Enabled for monitoring
///
/// **Spring Boot 4.0 Compatibility:**
///
/// Caffeine is required for reactive caching in Spring Boot 4.0 (Spring Framework 7.0)
/// because it supports `CompletableFuture`-based cache retrieval, which is necessary
/// for `@Cacheable` methods returning `Mono<>` or `Flux<>`.
///
/// JCache/EhCache does not support CompletableFuture and will throw
/// `UnsupportedOperationException` when used with reactive methods.
///
/// @author Zhubin Salehi
/// @see org.springframework.cache.caffeine.CaffeineCacheManager
/// @see com.github.benmanes.caffeine.cache.Caffeine
@Configuration
@EnableCaching
public class CacheConfiguration {

    /// Cache name for UMA permission tokens.
    public static final String UMA_TOKEN_CACHE = "umaTokens";

    /// Creates a Caffeine-based cache manager for reactive caching support.
    ///
    /// Caffeine supports `CompletableFuture`-based retrieval which is required
    /// for Spring Boot 4.0's reactive caching infrastructure.
    ///
    /// **Important:** Async cache mode must be enabled for reactive methods
    /// returning `Mono<>` or `Flux<>`.
    ///
    /// @return configured CacheManager with Caffeine implementation
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(UMA_TOKEN_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats());
        cacheManager.setAsyncCacheMode(true);  // Required for reactive caching
        return cacheManager;
    }
}
