package org.example.enterprisedigitalbankingsystem.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Atomic Redis Lua fixed-window rate limiter with fail-open degradation
 * when rate limiting is disabled or Redis is unreachable.
 */
@Slf4j
@Component
public class RedisRateLimiter {

    private static final DefaultRedisScript<List> FIXED_WINDOW_SCRIPT = createScript();

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final boolean enabled;

    public RedisRateLimiter(
            ObjectProvider<StringRedisTemplate> redisTemplateProvider,
            @Value("${banking.rate-limit.enabled:true}") boolean enabled) {
        this.redisTemplateProvider = redisTemplateProvider;
        this.enabled = enabled;
    }

    @SuppressWarnings("unchecked")
    public RateLimitResult checkLimit(RateLimitPolicy policy, String identity) {
        if (!enabled) {
            return RateLimitResult.unbounded(policy.getLimit());
        }

        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            log.debug("RedisTemplate unavailable; failing open for rate-limit policy={}", policy.getBucket());
            return RateLimitResult.unbounded(policy.getLimit());
        }

        try {
            String key = policy.redisKey(identity);
            List<Long> scriptResult = (List<Long>) redisTemplate.execute(
                    FIXED_WINDOW_SCRIPT,
                    Collections.singletonList(key),
                    String.valueOf(policy.getWindowSeconds())
            );
            if (scriptResult == null || scriptResult.isEmpty()) {
                return RateLimitResult.unbounded(policy.getLimit());
            }

            long currentCount = scriptResult.get(0);
            long ttlSeconds = scriptResult.size() > 1 && scriptResult.get(1) > 0
                    ? scriptResult.get(1)
                    : policy.getWindowSeconds();

            boolean allowed = currentCount <= policy.getLimit();
            long remaining = Math.max(0L, policy.getLimit() - currentCount);
            return new RateLimitResult(allowed, policy.getLimit(), remaining, ttlSeconds, true);
        } catch (Exception ex) {
            log.warn("Rate limiter failed open for policy={} due to Redis error: {}",
                    policy.getBucket(), ex.getMessage());
            return RateLimitResult.unbounded(policy.getLimit());
        }
    }

    private static DefaultRedisScript<List> createScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setResultType(List.class);
        script.setScriptText("""
                local current = redis.call('INCR', KEYS[1])
                if current == 1 then
                    redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))
                end
                local ttl = redis.call('TTL', KEYS[1])
                return {current, ttl}
                """);
        return script;
    }

    public record RateLimitResult(
            boolean allowed,
            long limit,
            long remaining,
            long resetSeconds,
            boolean enforced
    ) {
        public static RateLimitResult unbounded(long limit) {
            return new RateLimitResult(true, limit, limit, 0L, false);
        }
    }
}

