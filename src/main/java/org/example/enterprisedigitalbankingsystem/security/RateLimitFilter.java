package org.example.enterprisedigitalbankingsystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Enforces Redis-backed fixed-window rate limits using remoteAddr (proxy-normalized via Tomcat)
 * for anonymous traffic and authenticated principal names for API traffic.
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisRateLimiter rateLimiter;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        Optional<RateLimitPolicy> policyOpt = RateLimitPolicy.resolve(request.getMethod(), path);
        if (policyOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitPolicy policy = policyOpt.get();
        String identity = resolveIdentity(request, policy);
        RedisRateLimiter.RateLimitResult result = rateLimiter.checkLimit(policy, identity);

        if (result.enforced()) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(result.resetSeconds()));
        }

        if (!result.allowed()) {
            long retryAfter = Math.max(1L, result.resetSeconds());
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {"timestamp":"%s","status":429,"error":"Too Many Requests","message":"Rate limit exceeded. Retry after %d seconds."}"""
                    .formatted(LocalDateTime.now(), retryAfter));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveIdentity(HttpServletRequest request, RateLimitPolicy policy) {
        if (policy.isPreferAuthenticatedUser()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                return "user:" + auth.getName();
            }
        }
        String remoteAddr = request.getRemoteAddr();
        return "ip:" + (remoteAddr != null && !remoteAddr.isBlank() ? remoteAddr : "unknown");
    }
}

