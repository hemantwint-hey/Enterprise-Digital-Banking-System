package org.example.enterprisedigitalbankingsystem.common.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankingServiceHealthIndicatorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RedisConnection redisConnection;

    @Mock
    private AdminClient adminClient;

    @Mock
    private DescribeClusterResult describeClusterResult;

    private BankingServiceHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new BankingServiceHealthIndicator(dataSource, redisConnectionFactory, adminClient);
    }

    private void mockKafkaHealthy() {
        Node broker = new Node(1, "localhost", 9092);
        KafkaFuture<Collection<Node>> nodesFuture = KafkaFuture.completedFuture(Collections.singletonList(broker));
        KafkaFuture<String> clusterIdFuture = KafkaFuture.completedFuture("test-cluster-id");

        when(adminClient.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.nodes()).thenReturn(nodesFuture);
        when(describeClusterResult.clusterId()).thenReturn(clusterIdFuture);
    }

    @Test
    @DisplayName("Should return UP status when Database, Redis, and Kafka are all healthy")
    void shouldReturnUpWhenAllDependenciesAreHealthy() throws Exception {
        // 1. Database healthy
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);

        // 2. Redis healthy
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        // 3. Kafka healthy
        mockKafkaHealthy();

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("database", "UP");
        assertThat(health.getDetails()).containsEntry("redis", "UP");
        assertThat((String) health.getDetails().get("kafka")).startsWith("UP (Nodes: 1");
        assertThat(health.getDetails()).containsKey("checkedAt");
    }

    @Test
    @DisplayName("Should return DOWN status when Database connection fails")
    void shouldReturnDownWhenDatabaseFails() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Communications link failure"));

        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");
        mockKafkaHealthy();

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat((String) health.getDetails().get("database")).contains("DOWN: Communications link failure");
        assertThat(health.getDetails()).containsEntry("redis", "UP");
        assertThat((String) health.getDetails().get("kafka")).startsWith("UP");
    }

    @Test
    @DisplayName("Should return DOWN status when Redis cache ping fails")
    void shouldReturnDownWhenRedisFails() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);

        when(redisConnectionFactory.getConnection()).thenThrow(new RuntimeException("Redis connection refused"));
        mockKafkaHealthy();

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("database", "UP");
        assertThat((String) health.getDetails().get("redis")).contains("DOWN: Redis connection refused");
        assertThat((String) health.getDetails().get("kafka")).startsWith("UP");
    }

    @Test
    @DisplayName("Should return DOWN status when Kafka cluster is unreachable")
    void shouldReturnDownWhenKafkaFails() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);

        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        when(adminClient.describeCluster()).thenThrow(new RuntimeException("Kafka broker timeout"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("database", "UP");
        assertThat(health.getDetails()).containsEntry("redis", "UP");
        assertThat((String) health.getDetails().get("kafka")).contains("DOWN: Kafka broker timeout");
    }
}

