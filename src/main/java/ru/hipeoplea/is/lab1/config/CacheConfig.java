package ru.hipeoplea.is.lab1.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import javax.cache.CacheManager;
import javax.cache.Caching;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager jCacheManager() throws URISyntaxException {
        var provider = (EhcacheCachingProvider) Caching.getCachingProvider(
                EhcacheCachingProvider.class.getName());
        URI configUri = Objects.requireNonNull(provider.getDefaultClassLoader()
                        .getResource("ehcache.xml"))
                .toURI();
        CacheManager manager = provider.getCacheManager(configUri,
                provider.getDefaultClassLoader());
        manager.getCacheNames().forEach(name -> {
            try {
                manager.enableStatistics(name, true);
                manager.enableManagement(name, true);
            } catch (Exception ignored) {
            }
        });
        return manager;
    }

    @Bean
    public org.springframework.cache.CacheManager cacheManager(
            CacheManager jCacheManager) {
        return new JCacheCacheManager(jCacheManager);
    }
}
