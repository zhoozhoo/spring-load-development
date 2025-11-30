package ca.zhoozhoo.loaddev.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import javax.cache.Caching;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.jcache.JCacheCacheManager;

/**
 * Unit tests for {@link CacheConfiguration}.
 * Tests JSR-107 JCache with EhCache 3 configuration.
 * 
 * <p>These tests verify that the cache manager can be created and configured correctly
 * using the JCache API with EhCache 3 as the provider.</p>
 * 
 * <p>Note: These are simplified unit tests that directly test cache functionality without
 * requiring full Spring Boot context initialization. This avoids the need to configure
 * all application dependencies (OAuth2, Eureka, WebFlux, etc.) for cache testing.</p>
 *
 * @author Zhubin Salehi
 */
class CacheConfigurationTest {

    private JCacheCacheManager cacheManager;

    @BeforeEach
    void setUp() throws Exception {
        // Create JCache cache manager programmatically with EhCache 3
        var cachingProvider = Caching.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");
        var jCacheManager = cachingProvider.getCacheManager(
            getClass().getResource("/ehcache.xml").toURI(),
            getClass().getClassLoader()
        );
        cacheManager = new JCacheCacheManager(jCacheManager);
    }

    @AfterEach
    void tearDown() {
        if (cacheManager != null) {
            cacheManager.getCacheManager().close();
        }
    }

    @Test
    @DisplayName("Should create JCache cache manager with EhCache 3")
    void shouldCreateJCacheCacheManager() {
        // Then
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager).isInstanceOf(JCacheCacheManager.class);
        
        var cache = cacheManager.getCache(CacheConfiguration.UMA_TOKEN_CACHE);
        assertThat(cache).isNotNull();
        assertThat(cache.getName()).isEqualTo(CacheConfiguration.UMA_TOKEN_CACHE);
    }

    @Test
    @DisplayName("Should configure EhCache with JSR-107 JCache API")
    void shouldConfigureEhCacheFromXml() {
        // Then - Verify JCache manager is properly initialized
        assertThat(cacheManager).isInstanceOf(JCacheCacheManager.class);
        
        var underlyingCacheManager = cacheManager.getCacheManager();
        assertThat(underlyingCacheManager).isNotNull();
        
        // Verify the UMA token cache is available
        var cacheNames = underlyingCacheManager.getCacheNames();
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
