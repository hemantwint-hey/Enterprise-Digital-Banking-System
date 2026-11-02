package org.example.enterprisedigitalbankingsystem.common.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Day 63: Production Health Indicator for Enterprise Banking.
 * Aggregates health checks for core infrastructure dependencies:
 * 1. Database (MySQL via HikariCP)
 * 2. Redis Distributed Cache
 * 3. Apache Kafka Broker Cluster
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BankingServiceHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final AdminClient adminClient;

    @Override
    public Health health() {
        boolean isHealthy = true;

        // 1. Check Database (MySQL)
        String dbStatus;
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                dbStatus = "UP";
            } else {
                dbStatus = "DOWN (Connection validation timed out)";
                isHealthy = false;
            }
        } catch (Exception ex) {
            log.error("Health check failure - Database: {}", ex.getMessage());
            dbStatus = "DOWN: " + ex.getMessage();
            isHealthy = false;
        }

        // 2. Check Redis Cache
        String redisStatus;
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pingResult = connection.ping();
            if ("PONG".equalsIgnoreCase(pingResult)) {
                redisStatus = "UP";
            } else {
                redisStatus = "DOWN (Unexpected response: " + pingResult + ")";
                isHealthy = false;
            }
        } catch (Exception ex) {
            log.error("Health check failure - Redis: {}", ex.getMessage());
            redisStatus = "DOWN: " + ex.getMessage();
            isHealthy = false;
        }

        // 3. Check Kafka Broker Cluster
        String kafkaStatus;
        try {
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            int nodeCount = clusterResult.nodes().get(3, TimeUnit.SECONDS).size();
            String clusterId = clusterResult.clusterId().get(3, TimeUnit.SECONDS);
            kafkaStatus = String.format("UP (Nodes: %d, ClusterId: %s)", nodeCount, clusterId);
        } catch (Exception ex) {
            log.error("Health check failure - Kafka: {}", ex.getMessage());
            kafkaStatus = "DOWN: " + ex.getMessage();
            isHealthy = false;
        }

        // Build composite health status
        Health.Builder builder = isHealthy ? Health.up() : Health.down();
        return builder
                .withDetail("database", dbStatus)
                .withDetail("redis", redisStatus)
                .withDetail("kafka", kafkaStatus)
                .withDetail("checkedAt", LocalDateTime.now().toString())
                .build();
    }
}
