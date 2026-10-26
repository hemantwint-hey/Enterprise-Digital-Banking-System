package org.example.enterprisedigitalbankingsystem.kafka.consumer.repository;

import org.example.enterprisedigitalbankingsystem.kafka.consumer.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {

    // Day 59 Idempotency check:
    boolean existsByEventId(String eventId);
}
