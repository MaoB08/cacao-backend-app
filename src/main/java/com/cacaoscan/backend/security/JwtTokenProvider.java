package com.cacaoscan.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.accessExpirationMs}")
    private long jwtAccessExpirationMs;

    @jakarta.annotation.PostConstruct
    public void validateSecret() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("ERROR DE SEGURIDAD: La variable app.jwt.secret (JWT_SECRET) debe estar configurada en el entorno.");
        }
        if (jwtSecret.contains("9a67471b79e1b212356c9a3d4f828a2a89047fd20c9aa1b3c9f28d8b8e01de11")) {
            logger.warn("⚠️ ALERTA DE SEGURIDAD: Estás usando la firma JWT por defecto. En producción DEBES generar un JWT_SECRET único.");
        }
        byte[] bytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("ERROR DE SEGURIDAD: La clave JWT_SECRET debe tener al menos 32 caracteres (256 bits) de longitud.");
        }
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateAccessTokenFromUsername(userPrincipal.getUsername());
    }

    public String generateAccessTokenFromUsername(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtAccessExpirationMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Devuelve los milisegundos restantes antes de que expire el token.
     */
    public long getExpirationRemainingMs(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            long expirationMs = claims.getExpiration().getTime();
            long nowMs = System.currentTimeMillis();
            return Math.max(0, expirationMs - nowMs);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Token JWT inválido: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Token JWT expirado: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Token JWT no soportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("La cadena de claims JWT está vacía: {}", ex.getMessage());
        }
        return false;
    }
}
