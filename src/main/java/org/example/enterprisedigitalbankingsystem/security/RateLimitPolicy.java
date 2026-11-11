package org.example.enterprisedigitalbankingsystem.security;

import java.util.Optional;

/**
 * Defines fixed-window rate-limiting tiers for authentication, sensitive money movement,
 * and general banking API endpoints.
 */
public enum RateLimitPolicy {

    LOGIN("auth_login", 10, 60, false),
    REGISTER("auth_register", 5, 60, false),
    SENSITIVE_TRANSACTION("tx_sensitive", 20, 60, true),
    API_GENERAL("api_general", 120, 60, true);

    private final String bucket;
    private final long limit;
    private final long windowSeconds;
    private final boolean preferAuthenticatedUser;

    RateLimitPolicy(String bucket, long limit, long windowSeconds, boolean preferAuthenticatedUser) {
        this.bucket = bucket;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
        this.preferAuthenticatedUser = preferAuthenticatedUser;
    }

    public String getBucket() {
        return bucket;
    }

    public long getLimit() {
        return limit;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public boolean isPreferAuthenticatedUser() {
        return preferAuthenticatedUser;
    }

    public String redisKey(String identity) {
        return "rate_limit:" + bucket + ":" + identity;
    }

    public static Optional<RateLimitPolicy> resolve(String method, String path) {
        if (path == null || "OPTIONS".equalsIgnoreCase(method)) {
            return Optional.empty();
        }
        if (path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            return Optional.empty();
        }
        if ("POST".equalsIgnoreCase(method) && "/auth/login".equals(path)) {
            return Optional.of(LOGIN);
        }
        if ("POST".equalsIgnoreCase(method) && "/auth/register".equals(path)) {
            return Optional.of(REGISTER);
        }
        if ("POST".equalsIgnoreCase(method)
                && ("/api/v1/transactions/transfer".equals(path) || "/api/v1/transactions/withdraw".equals(path))) {
            return Optional.of(SENSITIVE_TRANSACTION);
        }
        if (path.startsWith("/api/") || path.startsWith("/auth/")) {
            return Optional.of(API_GENERAL);
        }
        return Optional.empty();
    }
}

