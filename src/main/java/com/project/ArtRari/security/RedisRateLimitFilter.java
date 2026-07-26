package com.project.ArtRari.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class RedisRateLimitFilter extends OncePerRequestFilter implements Ordered {
    private final ObjectProvider<ProxyManager<byte[]>> proxyManagerProvider;

    @Value("${app.rate-limit.capacity}")
    private long capacity;
    @Value("${app.rate-limit.refill-minutes}")
    private long refillMinutes;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ProxyManager<byte[]> proxyManager = proxyManagerProvider.getIfAvailable();
        if (proxyManager == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI();
        if (path.startsWith("/websocket") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }
        String ip = request.getRemoteAddr();
        byte[] key = ip.getBytes();
        Supplier<BucketConfiguration> bucketConfig = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, Duration.ofMinutes(refillMinutes))
                        .build())
                .build();
        BucketProxy bucket = proxyManager.builder().build(key, bucketConfig);
        try {
            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().println("Ліміт запитів вичерпано. Спробуйте пізніше");
                log.warn("Ліміт запитів вичерпано для IP: {}", ip);
            }
        } catch (Exception e) {
            log.error("Помилка Rate Limiter: {}", e.getMessage(), e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().println("Технічні негаразди на сервері");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
