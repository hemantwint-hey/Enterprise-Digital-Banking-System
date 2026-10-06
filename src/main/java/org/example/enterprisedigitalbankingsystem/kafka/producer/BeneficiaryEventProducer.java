package org.example.enterprisedigitalbankingsystem.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterprisedigitalbankingsystem.kafka.event.BeneficiaryActivatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class BeneficiaryEventProducer {
    private static final String TOPIC = "beneficiary.activated";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(BeneficiaryActivatedEvent event){
        try{
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC,event.getCustomerId().toString(),payload);
        }
        catch (Exception e){
            log.error("Failed to publish BeneficiaryActivatedEvent for beneficiary {}",
                    event.getBeneficiaryId(),e);
        }
    }
}
