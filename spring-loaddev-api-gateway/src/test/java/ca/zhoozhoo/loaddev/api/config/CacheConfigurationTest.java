package ca.zhoozhoo.loaddev.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

/**
 * Unit tests for {@link CacheConfiguration}.
 * Tests Caffeine cache configuration for reactive caching support.
 * 
 * <p>These tests verify that the cache manager can be created and configured correctly
 * using Caffeine, which supports CompletableFuture-based retrieval required for
 * Spring Boot 4.0's reactive caching infrastructure.</p>
 * 
 * <p>Note: These are simplified unit tests that directly test cache functionality without
 * requiring full Spring Boot context initialization. This avoids the need to configure
 * all application dependencies (OAuth2, Eureka, WebFlux, etc.) for cache testing.</p>
 *
 * @author Zhubin Salehi
 */
class CacheConfigurationTest {

    private CacheManager cacheManager;
    private CacheConfiguration cacheConfiguration;

    @BeforeEach
    void setUp() {
        // Create Caffeine cache manager programmatically
        cacheConfiguration = new CacheConfiguration();
        cacheManager = cacheConfiguration.cacheManager();
    }

    @Test
    @DisplayName("Should create Caffeine cache manager")
    void shouldCreateCaffeineCacheManager() {
        // Then
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
        
        var cache = cacheManager.getCache(CacheConfiguration.UMA_TOKEN_CACHE);
        assertThat(cache).isNotNull();
        assertThat(cache.getName()).isEqualTo(CacheConfiguration.UMA_TOKEN_CACHE);
    }

    @Test
    @DisplayName("Should configure Caffeine cache with proper settings")
    void shouldConfigureCaffeineCache() {
        // Then - Verify cache manager is properly initialized
        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
        
        var caffeineCacheManager = (CaffeineCacheManager) cacheManager;
        
        // Verify the UMA token cache is available
        var cacheNames = caffeineCacheManager.getCacheNames();
        assertThat(cacheNames).contains(CacheConfiguration.UMA_TOKEN_CACHE);
        
        // Verify we can retrieve the cache
        var cache = cacheManager.getCache(CacheConfiguration.UMA_TOKEN_CACHE);
        assertThat(cache).isNotNull();
        assertThat(cache.getName()).isEqualTo(CacheConfiguration.UMA_TOKEN_CACHE);
    }

    @Test
    @DisplayName("Should cache UMA permission tokens with proper type safety")
    void shouldCacheUmaPermissionTokensWithTypeSafety() {
        // Given
        var cache = cacheManager.getCache(CacheConfiguration.UMA_TOKEN_CACHE);
        assertThat(cache).isNotNull();

        // When - Cache UmaPermissionToken objects (the intended type)
        var token1 = new ca.zhoozhoo.loaddev.api.security.UmaPermissionToken("token1", "Bearer", 300, "openid");
        var token2 = new ca.zhoozhoo.loaddev.api.security.UmaPermissionToken("token2", "Bearer", 300, "openid");
        var token3 = new ca.zhoozhoo.loaddev.api.security.UmaPermissionToken("token3", "Bearer", 300, "openid");
        
        cache.put("key1", token1);
        cache.put("key2", token2);
        cache.put("key3", token3);

        // Then
        assertThat(cache.get("key1", ca.zhoozhoo.loaddev.api.security.UmaPermissionToken.class)).isEqualTo(token1);
        assertThat(cache.get("key2", ca.zhoozhoo.loaddev.api.security.UmaPermissionToken.class)).isEqualTo(token2);
        assertThat(cache.get("key3", ca.zhoozhoo.loaddev.api.security.UmaPermissionToken.class)).isEqualTo(token3);
        
        // When - Evict one entry
        cache.evict("key2");
        
        // Then - Other entries remain
        assertThat(cache.get("key1", ca.zhoozhoo.loaddev.api.security.UmaPermissionToken.class)).isEqualTo(token1);
        assertThat(cache.get("key2", ca.zhoozhoo.loaddev.api.security.UmaPermissionToken.class)).isNull();
        assertThat(cache.get("key3", ca.zhoozhoo.loaddev.api.security.UmaPermissionToken.class)).isEqualTo(token3);
    }

    @Test
    @DisplayName("Should use correct cache name constant")
    void shouldUseCorrectCacheName() {
        // Then
        assertThat(CacheConfiguration.UMA_TOKEN_CACHE).isEqualTo("umaTokens");
    }
}
