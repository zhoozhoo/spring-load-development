package ca.zhoozhoo.loaddev.api.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring Cache using JSR-107 (JCache) with EhCache 3 as the cache implementation.
 * 
 * <p>This configuration provides caching support for UMA permission tokens to reduce
 * unnecessary token exchange calls to Keycloak. The cache uses EhCache 3's high-performance
 * implementation with automatic expiration and size limits.</p>
 * 
 * <p><b>Cache Configuration:</b></p>
 * <ul>
 *   <li>Implementation: EhCache 3 with JSR-107 (JCache) API</li>
 *   <li>TTL: 5 minutes (90% of typical token expiry time)</li>
 *   <li>Max Heap Entries: 1000 entries</li>
 *   <li>Eviction: LRU (Least Recently Used)</li>
 *   <li>Statistics: Enabled for monitoring</li>
 * </ul>
 * 
 * <p><b>Configuration Sources:</b></p>
 * <ul>
 *   <li><b>application.yml:</b> Configures JCache provider and config file location
 *       <pre>
 *       spring:
 *         cache:
 *           jcache:
 *             provider: org.ehcache.jsr107.EhcacheCachingProvider
 *             config: classpath:ehcache.xml
 *       </pre>
 *   </li>
 *   <li><b>ehcache.xml:</b> Defines cache behavior (TTL, size, eviction policy)</li>
 * </ul>
 * 
 * <p>Spring Boot auto-configuration creates the {@code JCacheCacheManager} bean automatically
 * based on the application.yml settings. This class only needs to enable caching via
 * {@code @EnableCaching} annotation.</p>
 * 
 * @author Zhubin Salehi
 * @see javax.cache.CacheManager
 * @see org.ehcache.jsr107.EhcacheCachingProvider
 * @see org.springframework.cache.jcache.JCacheCacheManager
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * Cache name for UMA permission tokens.
     * This must match the cache name defined in ehcache.xml.
     */
    public static final String UMA_TOKEN_CACHE = "umaTokens";
}
