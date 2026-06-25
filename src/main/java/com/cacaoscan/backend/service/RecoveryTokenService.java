package com.cacaoscan.backend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RecoveryTokenService {

    private final StringRedisTemplate redisTemplate;
    private static final String RECOVERY_PREFIX = "reset:token:";
    private static final long RECOVERY_TTL_MINUTES = 30;

    public RecoveryTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Crea un token de recuperación temporal en Redis para el usuario especificado.
     */
    public String createRecoveryToken(UUID userId) {
        String token = UUID.randomUUID().toString();
        String key = RECOVERY_PREFIX + token;
        
        redisTemplate.opsForValue().set(key, userId.toString(), RECOVERY_TTL_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    /**
     * Obtiene el ID del usuario asociado a un token de recuperación, o null si expiró/no existe.
     */
    public UUID getUserIdByToken(String token) {
        String key = RECOVERY_PREFIX + token;
        String userIdStr = redisTemplate.opsForValue().get(key);
        
        if (userIdStr == null) {
            return null;
        }
        
        try {
            return UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Invalida (elimina) un token de recuperación para que no pueda usarse de nuevo.
     */
    public void invalidateToken(String token) {
        String key = RECOVERY_PREFIX + token;
        redisTemplate.delete(key);
    }
}
