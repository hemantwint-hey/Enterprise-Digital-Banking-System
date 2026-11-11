package org.example.enterprisedigitalbankingsystem.security;

import jakarta.servlet.ServletException;
import org.example.enterprisedigitalbankingsystem.audit.mapper.AuditMapper;
import org.example.enterprisedigitalbankingsystem.audit.repository.AuditRepository;
import org.example.enterprisedigitalbankingsystem.audit.service.impl.AuditServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityHardeningAndRateLimitTest {

    @AfterEach
    void cleanContexts() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("RateLimitPolicy resolves login, register, sensitive transactions, general APIs, and exclusions")
    void rateLimitPolicy_shouldClassifyRoutes() {
        assertEquals(Optional.of(RateLimitPolicy.LOGIN), RateLimitPolicy.resolve("POST", "/auth/login"));
        assertEquals(Optional.of(RateLimitPolicy.REGISTER), RateLimitPolicy.resolve("POST", "/auth/register"));
        assertEquals(Optional.of(RateLimitPolicy.SENSITIVE_TRANSACTION),
                RateLimitPolicy.resolve("POST", "/api/v1/transactions/transfer"));
        assertEquals(Optional.of(RateLimitPolicy.SENSITIVE_TRANSACTION),
                RateLimitPolicy.resolve("POST", "/api/v1/transactions/withdraw"));
        assertEquals(Optional.of(RateLimitPolicy.API_GENERAL),
                RateLimitPolicy.resolve("GET", "/api/v1/accounts/search"));

        assertTrue(RateLimitPolicy.resolve("GET", "/actuator/prometheus").isEmpty());
        assertTrue(RateLimitPolicy.resolve("GET", "/swagger-ui/index.html").isEmpty());
        assertTrue(RateLimitPolicy.resolve("GET", "/v3/api-docs").isEmpty());
        assertTrue(RateLimitPolicy.resolve("OPTIONS", "/api/v1/accounts/search").isEmpty());
    }

    @Test
    @DisplayName("RedisRateLimiter fails open when disabled, Redis absent, or Redis throws")
    void redisRateLimiter_shouldFailOpen() {
        @SuppressWarnings("unchecked")
        ObjectProvider<StringRedisTemplate> emptyProvider = mock(ObjectProvider.class);
        when(emptyProvider.getIfAvailable()).thenReturn(null);

        RedisRateLimiter disabledLimiter = new RedisRateLimiter(emptyProvider, false);
        assertTrue(disabledLimiter.checkLimit(RateLimitPolicy.LOGIN, "ip:1.2.3.4").allowed());

        RedisRateLimiter noRedisLimiter = new RedisRateLimiter(emptyProvider, true);
        RedisRateLimiter.RateLimitResult absentResult = noRedisLimiter.checkLimit(RateLimitPolicy.LOGIN, "ip:1.2.3.4");
        assertTrue(absentResult.allowed());
        assertFalse(absentResult.enforced());

        StringRedisTemplate failingTemplate = mock(StringRedisTemplate.class);
        when(failingTemplate.execute(any(RedisScript.class), anyList(), eq("60")))
                .thenThrow(new RuntimeException("Redis connection refused"));
        @SuppressWarnings("unchecked")
        ObjectProvider<StringRedisTemplate> failingProvider = mock(ObjectProvider.class);
        when(failingProvider.getIfAvailable()).thenReturn(failingTemplate);

        RedisRateLimiter failingLimiter = new RedisRateLimiter(failingProvider, true);
        RedisRateLimiter.RateLimitResult errResult = failingLimiter.checkLimit(RateLimitPolicy.LOGIN, "ip:1.2.3.4");
        assertTrue(errResult.allowed());
        assertFalse(errResult.enforced());
    }

    @Test
    @DisplayName("RateLimitFilter returns 429 with Retry-After and does not trust spoofed X-Forwarded-For")
    void rateLimitFilter_shouldEnforceLimitAndUseRemoteAddr() throws ServletException, IOException {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of("rate_limit:auth_login:ip:198.51.100.10")), eq("60")))
                .thenReturn(List.of(10L, 45L))
                .thenReturn(List.of(11L, 45L));

        @SuppressWarnings("unchecked")
        ObjectProvider<StringRedisTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(redisTemplate);

        RateLimitFilter filter = new RateLimitFilter(new RedisRateLimiter(provider, true));

        MockHttpServletRequest req1 = new MockHttpServletRequest("POST", "/auth/login");
        req1.setRemoteAddr("198.51.100.10");
        req1.addHeader("X-Forwarded-For", "203.0.113.1");
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilter(req1, res1, new MockFilterChain());
        assertEquals(HttpStatus.OK.value(), res1.getStatus());
        assertEquals("0", res1.getHeader("X-RateLimit-Remaining"));

        MockHttpServletRequest req2 = new MockHttpServletRequest("POST", "/auth/login");
        req2.setRemoteAddr("198.51.100.10");
        req2.addHeader("X-Forwarded-For", "203.0.113.99");
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        MockFilterChain blockedChain = mock(MockFilterChain.class);
        filter.doFilter(req2, res2, blockedChain);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), res2.getStatus());
        assertEquals("45", res2.getHeader("Retry-After"));
        assertTrue(res2.getContentAsString().contains("\"status\":429"));
    }

    @Test
    @DisplayName("RateLimitFilter keys general API traffic by authenticated user when present")
    void rateLimitFilter_shouldKeyApiTrafficByAuthenticatedUser() throws ServletException, IOException {
        RedisRateLimiter limiter = mock(RedisRateLimiter.class);
        when(limiter.checkLimit(RateLimitPolicy.API_GENERAL, "user:alice@bank.com"))
                .thenReturn(new RedisRateLimiter.RateLimitResult(true, 120, 119, 60, true));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice@bank.com", null, List.of())
        );

        RateLimitFilter filter = new RateLimitFilter(limiter);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/accounts/search");
        request.setRemoteAddr("10.0.0.5");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        verify(limiter).checkLimit(RateLimitPolicy.API_GENERAL, "user:alice@bank.com");
        assertEquals("119", response.getHeader("X-RateLimit-Remaining"));
    }

    @Test
    @DisplayName("AuditServiceImpl resolves client IP from remoteAddr rather than untrusted X-Forwarded-For")
    void auditService_shouldResolveRemoteAddrSafely() {
        AuditServiceImpl auditService = new AuditServiceImpl(mock(AuditRepository.class), mock(AuditMapper.class));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.25");
        request.addHeader("X-Forwarded-For", "10.99.99.99");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String resolvedIp = ReflectionTestUtils.invokeMethod(auditService, "resolveClientIp");
        assertEquals("192.0.2.25", resolvedIp);
    }
}

