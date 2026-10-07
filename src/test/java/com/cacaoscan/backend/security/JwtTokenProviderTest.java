package com.cacaoscan.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {

    @Test
    void rejectsMissingSecret() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", " ");

        assertThrows(IllegalStateException.class, provider::validateSecret);
    }

    @Test
    void rejectsSecretShorterThan256Bits() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "short-secret");

        assertThrows(IllegalStateException.class, provider::validateSecret);
    }

    @Test
    void rejectsPreviouslyExposedExampleSecrets() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret",
                "cacaoscan_super_secret_jwt_key_that_is_at_least_256_bits_long_for_hmac_sha256");

        assertThrows(IllegalStateException.class, provider::validateSecret);

        ReflectionTestUtils.setField(provider, "jwtSecret",
                "9a67471b79e1b212356c9a3d4f828a2a89047fd20c9aa1b3c9f28d8b8e01de11");
        assertThrows(IllegalStateException.class, provider::validateSecret);
    }

    @Test
    void acceptsConfiguredSecretOfAtLeast256Bits() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "test-only-secret-with-at-least-256-bits-0123456789abcdef");

        assertDoesNotThrow(provider::validateSecret);
    }
}
