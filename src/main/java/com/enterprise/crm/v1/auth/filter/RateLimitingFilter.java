package com.enterprise.crm.v1.auth.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> authLimiters = new ConcurrentHashMap<>();
    private final Map<String, Bucket> apiLimiters = new ConcurrentHashMap<>();

    @Value("${app.rate-limiting.enabled:true}")
    private boolean rateLimitingEnabled;

    private Bucket getAuthBucket(String ip) {
        return authLimiters.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
                .build());
    }

    private Bucket getApiBucket(String ip) {
        return apiLimiters.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(60).refillGreedy(60, Duration.ofMinutes(1)).build())
                .build());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!rateLimitingEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();

        Bucket bucket;
        if (uri.contains("/auth/login") || uri.contains("/auth/forgot-password") || uri.contains("/auth/reset-password") || uri.contains("/auth/register")) {
            bucket = getAuthBucket(ip);
        } else {
            bucket = getApiBucket(ip);
        }

        if (!bucket.tryConsume(1)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"message\":\"Too Many Requests. Rate limit exceeded.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
