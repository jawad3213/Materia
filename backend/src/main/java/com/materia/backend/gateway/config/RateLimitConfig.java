package com.materia.backend.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 🔹 RATE LIMIT CONFIGURATION
 * 
 * Définit la politique et les compteurs de requêtes par IP / client
 * pour protéger les endpoints de l'API Gateway contre les abus ou DoS.
 */
@Configuration
public class RateLimitConfig {

    @Value("${gateway.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${gateway.rate-limit.max-requests-per-minute:120}")
    private int maxRequestsPerMinute;

    // Credential endpoints get a much tighter quota than ordinary API traffic:
    // they are unauthenticated and are the target of password guessing.
    @Value("${gateway.rate-limit.max-auth-requests-per-minute:10}")
    private int maxAuthRequestsPerMinute;

    @Value("${gateway.rate-limit.window-seconds:60}")
    private long windowSeconds;

    // Cache mémoire des compteurs : clientIp -> TokenBucket
    private final ConcurrentMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxRequestsPerMinute() {
        return maxRequestsPerMinute;
    }

    public int getMaxAuthRequestsPerMinute() {
        return maxAuthRequestsPerMinute;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    /**
     * Vérifie si le client a dépassé son quota de requêtes.
     * 
     * @param clientKey IP du client ou identifiant utilisateur
     * @return true si la requête est autorisée, false si la limite est dépassée
     */
    public boolean tryConsume(String clientKey) {
        return tryConsume(clientKey, maxRequestsPerMinute);
    }

    /**
     * Même compteur glissant, mais avec un quota explicite : permet d'appliquer
     * une limite distincte (plus stricte) aux endpoints d'authentification.
     *
     * @param clientKey clé du compteur (l'appelant la préfixe par famille de routes)
     * @param limit     nombre maximum de requêtes autorisées dans la fenêtre
     */
    public boolean tryConsume(String clientKey, int limit) {
        if (!enabled) {
            return true;
        }

        long now = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000L;

        TokenBucket bucket = buckets.compute(clientKey, (key, existing) -> {
            if (existing == null || now - existing.windowStartMillis >= windowMillis) {
                return new TokenBucket(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        return bucket.count.get() <= limit;
    }

    public int getRemainingRequests(String clientKey) {
        return getRemainingRequests(clientKey, maxRequestsPerMinute);
    }

    public int getRemainingRequests(String clientKey, int limit) {
        TokenBucket bucket = buckets.get(clientKey);
        if (bucket == null) {
            return limit;
        }
        return Math.max(0, limit - bucket.count.get());
    }

    private static class TokenBucket {
        final long windowStartMillis;
        final AtomicInteger count;

        TokenBucket(long windowStartMillis, AtomicInteger count) {
            this.windowStartMillis = windowStartMillis;
            this.count = count;
        }
    }
}
