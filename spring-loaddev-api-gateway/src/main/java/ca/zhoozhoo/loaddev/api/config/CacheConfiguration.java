package ca.zhoozhoo.loaddev.api.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Configuration for Spring Cache using Caffeine as the cache implementation.
 * 
 * <p>This configuration provides caching support for UMA permission tokens to reduce
 * unnecessary token exchange calls to Keycloak. The cache uses Caffeine's high-performance
 * implementation with automatic expiration and size limits.</p>
 * 
 * <p><b>Cache Configuration:</b></p>
 * <ul>
 *   <li>Implementation: Caffeine (supports CompletableFuture for reactive caching)</li>
 *   <li>TTL: 5 minutes (90% of typical token expiry time)</li>
 *   <li>Max Size: 1000 entries</li>
 *   <li>Eviction: Size-based with W-TinyLFU (Window Tiny Least Frequently Used)</li>
 *   <li>Statistics: Enabled for monitoring</li>
 * </ul>
 * 
 * <p><b>Spring Boot 4.0 Compatibility:</b></p>
 * <p>Caffeine is required for reactive caching in Spring Boot 4.0 (Spring Framework 7.0)
 * because it supports {@code CompletableFuture}-based cache retrieval, which is necessary
 * for {@code @Cacheable} methods returning {@code Mono<>} or {@code Flux<>}.</p>
 * 
 * <p>JCache/EhCache does not support CompletableFuture and will throw
 * {@code UnsupportedOperationException} when used with reactive methods.</p>
 * 
 * @author Zhubin Salehi
 * @see org.springframework.cache.caffeine.CaffeineCacheManager
 * @see com.github.benmanes.caffeine.cache.Caffeine
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * Cache name for UMA permission tokens.
     */
    public static final String UMA_TOKEN_CACHE = "umaTokens";

    /**
     * Creates a Caffeine-based cache manager for reactive caching support.
     * 
     * <p>Caffeine supports {@code CompletableFuture}-based retrieval which is required
     * for Spring Boot 4.0's reactive caching infrastructure.</p>
     * 
     * <p><b>Important:</b> Async cache mode must be enabled for reactive methods
     * returning {@code Mono<>} or {@code Flux<>}.</p>
     * 
     * @return configured CacheManager with Caffeine implementation
     */
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
