package com.project.ArtRari.config;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

@Slf4j
@Configuration
public class CacheConfigurator implements CachingConfigurer {
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(
                    @Nullable RuntimeException exception,
                    @Nullable Cache cache,
                    @Nullable Object key
            ) {
                log.warn("Redis does not respond! Could not get cache for the key: {}. Addressing DB directly...", key);
            }

            @Override
            public void handleCachePutError(
                    @Nullable RuntimeException exception,
                    @Nullable Cache cache,
                    @Nullable Object key,
                    @Nullable Object value
            ) {
                log.warn("Redis does not respond! Could not save data in cache for the key: {}", key);
            }

            @Override
            public void handleCacheEvictError(
                    @Nullable RuntimeException exception,
                    @Nullable Cache cache,
                    @Nullable Object key) {
                log.warn("Redis does not respond! Could not reset cache for the key: {}", key);
            }

            @Override
            public void handleCacheClearError(
                    @Nullable RuntimeException exception,
                    @Nullable Cache cache) {
                log.warn("Redis does not respond! Could not clean cache: {}", cache.getName());
            }
        };
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues();
    }
}
