package com.cacaoscan.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class SecurityConfigTest {

    @Test
    void corsAllowsOnlyConfiguredOriginsAndDoesNotEnableCredentials() {
        SecurityConfig config = new SecurityConfig(null);
        ReflectionTestUtils.setField(config, "allowedOrigins", List.of("https://app.example.com"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/perfil");

        CorsConfiguration cors = config.corsConfigurationSource().getCorsConfiguration(request);

        assertEquals(List.of("https://app.example.com"), cors.getAllowedOrigins());
        assertFalse(cors.getAllowCredentials());
        assertEquals("https://app.example.com", cors.checkOrigin("https://app.example.com"));
        assertNull(cors.checkOrigin("https://malicious.example"));
    }
}
