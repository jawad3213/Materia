package com.materia.backend.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "materia-topic", groupId = "materia-group")
    public void consumeMessage(String message) {
        System.out.println("📥 [Kafka Consumer] Received message: " + message);
    }
}
