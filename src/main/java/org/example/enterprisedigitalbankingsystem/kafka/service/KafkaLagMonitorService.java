package org.example.enterprisedigitalbankingsystem.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaLagMonitorService {

    private final AdminClient adminClient;

    @Value("${spring.kafka.consumer.group-id:banking-consumer-group}")
    private String consumerGroup;

    private static final long LAG_WARNING_THRESHOLD = 50L;

    // Day 60: Periodically checks consumer lag every 30 seconds
    @Scheduled(fixedRate = 30000)
    public void monitorConsumerLag() {
        try {
            Map<TopicPartition, Long> lagMap = calculateLag(consumerGroup);
            long totalLag = lagMap.values().stream().mapToLong(Long::longValue).sum();

            if (totalLag > LAG_WARNING_THRESHOLD) {
                log.warn("⚠️ HIGH CONSUMER LAG DETECTED! Group: '{}', Total Lag: {} messages. Details: {}",
                        consumerGroup, totalLag, lagMap);
            } else {
                log.info("✅ Kafka Consumer Lag healthy. Group: '{}', Total Lag: {}", consumerGroup, totalLag);
            }
        } catch (Exception ex) {
            log.error("Failed to retrieve Kafka consumer lag metrics", ex);
        }
    }

    public Map<TopicPartition, Long> calculateLag(String groupId) throws Exception {
        // 1. Retrieve current committed offsets for the consumer group
        ListConsumerGroupOffsetsResult groupOffsetsResult = adminClient.listConsumerGroupOffsets(groupId);
        Map<TopicPartition, OffsetAndMetadata> consumerOffsets =
                groupOffsetsResult.partitionsToOffsetAndMetadata().get(5, TimeUnit.SECONDS);

        // 2. Query log end offsets (LEO) for the same partitions
        Map<TopicPartition, OffsetSpec> requestEndOffsets = new HashMap<>();
        for (TopicPartition tp : consumerOffsets.keySet()) {
            requestEndOffsets.put(tp, OffsetSpec.latest());
        }

        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> endOffsets =
                adminClient.listOffsets(requestEndOffsets).all().get(5, TimeUnit.SECONDS);

        // 3. Compute: Lag = EndOffset - CurrentOffset
        Map<TopicPartition, Long> lagPerPartition = new HashMap<>();
        for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : consumerOffsets.entrySet()) {
            TopicPartition tp = entry.getKey();
            long currentOffset = entry.getValue() != null ? entry.getValue().offset() : 0L;
            long logEndOffset = endOffsets.containsKey(tp) ? endOffsets.get(tp).offset() : 0L;
            long lag = Math.max(0L, logEndOffset - currentOffset);
            lagPerPartition.put(tp, lag);
        }

        return lagPerPartition;
    }
}
