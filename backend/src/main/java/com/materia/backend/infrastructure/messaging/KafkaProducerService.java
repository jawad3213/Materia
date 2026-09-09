package com.materia.backend.infrastructure.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.messaging.type", havingValue = "kafka")
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String topic, String key, Object message) {
        kafkaTemplate.send(topic, key, message);
        System.out.println("📤 [Kafka Producer] Sent message to topic [" + topic + "]: " + message);
    }
}
