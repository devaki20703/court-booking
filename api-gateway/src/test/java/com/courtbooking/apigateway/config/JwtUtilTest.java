package com.courtbooking.apigateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String JWT_SECRET = "very-long-secret-key-that-is-at-least-32-bytes-long-for-hs256";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 86400000L);
    }

    @Test
    void shouldGenerateValidToken() {
        String token = jwtUtil.generateToken(1L, "testuser", "USER");

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void shouldGenerateTokenForTest() {
        String token = jwtUtil.generateTokenForTest("testuser", 1L, "ADMIN");

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void shouldExtractUsername() {
        String token = jwtUtil.generateToken(1L, "testuser", "USER");

        String username = jwtUtil.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    void shouldExtractUserId() {
        String token = jwtUtil.generateToken(123L, "testuser", "USER");

        Long userId = jwtUtil.extractUserId(token);

        assertEquals(123L, userId);
    }

    @Test
    void shouldExtractRole() {
        String token = jwtUtil.generateToken(1L, "testuser", "ADMIN");

        String role = jwtUtil.extractRole(token);

        assertEquals("ADMIN", role);
    }

    @Test
    void shouldExtractExpiration() {
        String token = jwtUtil.generateToken(1L, "testuser", "USER");

        Date expiration = jwtUtil.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void shouldValidateValidToken() {
        String token = jwtUtil.generateToken(1L, "testuser", "USER");

        boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        boolean isValid = jwtUtil.validateToken("invalid.token.here");

        assertFalse(isValid);
    }

    @Test
    void shouldReturnFalseForTamperedToken() {
        String token = jwtUtil.generateToken(1L, "testuser", "USER");
        String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

        boolean isValid = jwtUtil.validateToken(tamperedToken);

        assertFalse(isValid);
    }

    @Test
    void shouldReturnFalseForNullToken() {
        boolean isValid = jwtUtil.validateToken(null);

        assertFalse(isValid);
    }

    @Test
    void shouldReturnFalseForEmptyToken() {
        boolean isValid = jwtUtil.validateToken("");

        assertFalse(isValid);
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtUtil.generateToken(1L, "user1", "USER");
        String token2 = jwtUtil.generateToken(2L, "user2", "ADMIN");

        assertNotEquals(token1, token2);
    }

    @Test
    void shouldExtractClaim() {
        String token = jwtUtil.generateToken(1L, "testuser", "ADMIN");

        String role = jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));

        assertEquals("ADMIN", role);
    }
}