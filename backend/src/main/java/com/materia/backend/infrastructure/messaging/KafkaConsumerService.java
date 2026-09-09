package com.materia.backend.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.messaging.type", havingValue = "kafka")
public class KafkaConsumerService {

    @KafkaListener(topics = "materia-topic", groupId = "materia-group")
    public void consumeMessage(String message) {
        System.out.println("📥 [Kafka Consumer] Received message: " + message);
    }
}
