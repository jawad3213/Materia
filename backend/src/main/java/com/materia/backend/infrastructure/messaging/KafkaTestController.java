package com.materia.backend.infrastructure.messaging;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kafka")
@Tag(name = "Kafka Test API", description = "Endpoints for testing Kafka Producer & Consumer")
public class KafkaTestController {

    @Autowired
    private KafkaProducerService producerService;

    @PostMapping("/publish")
    @Operation(summary = "Publish a message to Kafka topic")
    public String publishMessage(@RequestParam(defaultValue = "materia-topic") String topic,
                                 @RequestParam(defaultValue = "test-key") String key,
                                 @RequestBody String message) {
        producerService.sendMessage(topic, key, message);
        return "Message published successfully to topic: " + topic;
    }
}
