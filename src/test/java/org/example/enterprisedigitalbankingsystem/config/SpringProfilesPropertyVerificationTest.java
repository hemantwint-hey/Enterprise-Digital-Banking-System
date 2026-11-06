package org.example.enterprisedigitalbankingsystem.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Day 68: Spring Profiles Property Verification Test.
 * Validates that base, dev, and prod configuration properties adhere to
 * banking security standards, performance tuning, and profile segregation.
 */
class SpringProfilesPropertyVerificationTest {

    @Test
    @DisplayName("Should verify base application.properties contains universal defaults and dev fallback")
    void testBaseApplicationProperties() throws IOException {
        Properties baseProps = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));

        assertThat(baseProps.getProperty("spring.application.name")).isEqualTo("Enterprise-Digital-Banking-System");
        assertThat(baseProps.getProperty("spring.profiles.active")).isEqualTo("dev");
        assertThat(baseProps.getProperty("spring.threads.virtual.enabled")).isEqualTo("true");
        assertThat(baseProps.getProperty("spring.kafka.consumer.group-id")).isEqualTo("banking-consumer-group");
        assertThat(baseProps.getProperty("spring.kafka.producer.acks")).isEqualTo("all");
        assertThat(baseProps.getProperty("spring.kafka.producer.properties.enable.idempotence")).isEqualTo("true");
    }

    @Test
    @DisplayName("Should verify application-dev.properties is optimized for local debugging and rapid iteration")
    void testDevApplicationProperties() throws IOException {
        Properties devProps = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-dev.properties"));

        // DDL Auto & SQL Logging enabled for dev
        assertThat(devProps.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("update");
        assertThat(devProps.getProperty("spring.jpa.show-sql")).isEqualTo("true");
        assertThat(devProps.getProperty("spring.jpa.properties.hibernate.format_sql")).isEqualTo("true");

        // Small local connection pool
        assertThat(devProps.getProperty("spring.datasource.hikari.maximum-pool-size")).isEqualTo("10");
        assertThat(devProps.getProperty("spring.datasource.hikari.minimum-idle")).isEqualTo("5");

        // Full trace sampling & visible health details
        assertThat(devProps.getProperty("management.tracing.sampling.probability")).isEqualTo("1.0");
        assertThat(devProps.getProperty("management.endpoint.health.show-details")).isEqualTo("always");

        // Safe local fallbacks present
        assertThat(devProps.getProperty("spring.datasource.username")).isEqualTo("${DB_USERNAME:root}");
        assertThat(devProps.getProperty("spring.datasource.password")).isEqualTo("${DB_PASSWORD:hemant851311420}");
    }

    @Test
    @DisplayName("Should verify application-prod.properties is hardened for high-throughput and container security")
    void testProdApplicationProperties() throws IOException {
        Properties prodProps = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-prod.properties"));

        // DDL Auto validate (no auto-mutation) & SQL Logging disabled
        assertThat(prodProps.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        assertThat(prodProps.getProperty("spring.jpa.show-sql")).isEqualTo("false");
        assertThat(prodProps.getProperty("spring.jpa.properties.hibernate.format_sql")).isEqualTo("false");

        // Strict externalized credentials (no hardcoded passwords)
        assertThat(prodProps.getProperty("spring.datasource.username")).isEqualTo("${DB_USERNAME}");
        assertThat(prodProps.getProperty("spring.datasource.password")).isEqualTo("${DB_PASSWORD}");
        assertThat(prodProps.getProperty("jwt.secret")).isEqualTo("${JWT_SECRET}");

        // Tuned HikariCP production connection pool
        assertThat(prodProps.getProperty("spring.datasource.hikari.maximum-pool-size")).isEqualTo("${DB_POOL_MAX_SIZE:30}");
        assertThat(prodProps.getProperty("spring.datasource.hikari.minimum-idle")).isEqualTo("${DB_POOL_MIN_IDLE:10}");
        assertThat(prodProps.getProperty("spring.datasource.hikari.leak-detection-threshold")).isEqualTo("15000");

        // Restricted Actuator exposure for production security
        assertThat(prodProps.getProperty("management.endpoints.web.exposure.include")).isEqualTo("health,info,prometheus");
        assertThat(prodProps.getProperty("management.endpoint.health.show-details")).isEqualTo("never");

        // Container-optimized Kafka broker & batching
        assertThat(prodProps.getProperty("spring.kafka.bootstrap-servers")).isEqualTo("${KAFKA_BOOTSTRAP_SERVERS:kafka:29092}");
        assertThat(prodProps.getProperty("spring.kafka.producer.properties.batch.size")).isEqualTo("32768");
    }
}

