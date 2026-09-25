package com.materia.backend.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;

/**
 * 🔹 API GATEWAY APPLICATION
 * 
 * Point d'entrée de la passerelle d'API (API Gateway).
 * Orchestre les requêtes entrantes, la sécurité, le rate-limiting,
 * le logging et le routage vers les différents modules contextuels.
 */
@Configuration
public class GatewayApplication {

    private static final Logger log = LoggerFactory.getLogger(GatewayApplication.class);

    public static void main(String[] args) {
        log.info("Starting Materia API Gateway...");
        SpringApplication.run(GatewayApplication.class, args);
        log.info("Materia API Gateway started successfully.");
    }
}
