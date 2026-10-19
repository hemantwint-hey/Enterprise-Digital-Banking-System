package org.example.enterprisedigitalbankingsystem.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterprisedigitalbankingsystem.kafka.event.TransactionCompletedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {
    private static final String TOPIC = "transaction.completed";
    private final KafkaTemplate<String , String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(TransactionCompletedEvent event){
        try{
          String payload = objectMapper.writeValueAsString(event);
          String key = event.getAccountId() != null ? event.getAccountId().toString() : null;
          kafkaTemplate.send(TOPIC, key, payload);
        }
        catch(Exception e){
            log.error("Failed to publish TransactionCompletedEvent for transaction {}",event.getTransactionId(),e);
        }
    }

}
