package com.project.ArtRari.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
@Slf4j
public class RateLimiterConfig {
    @Value("${app.rate-limit.expiration-seconds}")
    private long expirationSeconds;

    @Bean
    public ProxyManager<byte[]> proxyManager(RedisConnectionFactory connectionFactory) {
        try {
            LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) connectionFactory;
            RedisClient redisClient = (RedisClient) lettuceFactory.getNativeClient();
            ClientSideConfig clientSideConfig = ClientSideConfig.getDefault()
                    .withExpirationAfterWriteStrategy(
                            ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(expirationSeconds))
                    );
            return LettuceBasedProxyManager.builderFor(redisClient)
                    .withClientSideConfig(clientSideConfig)
                    .build();
        } catch (Exception e) {
            log.warn("Rate Limiter не доступний: {}", e.getMessage(), e);
            return null;
        }
    }
}
