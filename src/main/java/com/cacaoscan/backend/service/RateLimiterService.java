package com.cacaoscan.backend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_ATTEMPTS = 5;
    private static final long ATTEMPTS_TTL_MINUTES = 15;
    private static final String ATTEMPTS_PREFIX = "login:attempts:";
    private static final String BLOCKED_PREFIX = "login:blocked:";

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Verifica si una combinación de email e IP está bloqueada.
     */
    public boolean isBlocked(String email, String ip) {
        String blockedKey = getBlockedKey(email, ip);
        Boolean isBlocked = redisTemplate.hasKey(blockedKey);
        return isBlocked != null && isBlocked;
    }

    /**
     * Obtiene los segundos restantes del bloqueo actual.
     */
    public long getBlockedTimeRemaining(String email, String ip) {
        String blockedKey = getBlockedKey(email, ip);
        Long ttl = redisTemplate.getExpire(blockedKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * Registra un intento de inicio de sesión fallido.
     */
    public void recordFailedAttempt(String email, String ip) {
        String attemptsKey = getAttemptsKey(email, ip);
        
        // Incrementar el número de intentos
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        
        if (attempts == null) {
            attempts = 1L;
        }

        // Establecer o renovar el TTL del contador de intentos
        redisTemplate.expire(attemptsKey, ATTEMPTS_TTL_MINUTES, TimeUnit.MINUTES);

        // Si se supera el número máximo de intentos, bloquear
        if (attempts >= MAX_ATTEMPTS) {
            String blockedKey = getBlockedKey(email, ip);
            
            // Backoff exponencial: 5 intentos = 5min (300s), 6 intentos = 10min, 7 intentos = 20min... max 1 hora (3600s)
            long attemptsOverLimit = attempts - MAX_ATTEMPTS;
            long blockTimeSeconds = 300 * (long) Math.pow(2, attemptsOverLimit);
            if (blockTimeSeconds > 3600) {
                blockTimeSeconds = 3600; // Cap a 1 hora
            }

            redisTemplate.opsForValue().set(blockedKey, "true", blockTimeSeconds, TimeUnit.SECONDS);
            
            // Sincronizar el TTL de los intentos para que no expire durante el bloqueo, añadiendo la ventana estándar
            redisTemplate.expire(attemptsKey, blockTimeSeconds + (ATTEMPTS_TTL_MINUTES * 60), TimeUnit.SECONDS);
        }
    }

    /**
     * Limpia los registros de intentos y bloques al realizar un inicio de sesión exitoso.
     */
    public void resetAttempts(String email, String ip) {
        redisTemplate.delete(getAttemptsKey(email, ip));
        redisTemplate.delete(getBlockedKey(email, ip));
    }

    private String getAttemptsKey(String email, String ip) {
        return ATTEMPTS_PREFIX + email.toLowerCase().trim() + ":" + ip.trim();
    }

    private String getBlockedKey(String email, String ip) {
        return BLOCKED_PREFIX + email.toLowerCase().trim() + ":" + ip.trim();
    }
}
