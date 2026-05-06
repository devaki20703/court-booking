package com.courtbooking.userservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "mySecretKey1234567890mySecretKey1234567890mySecretKey1234567890";
    private static final long EXPIRATION = 3600000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken("testuser", 1L, "USER");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldValidateValidToken() {
        String token = jwtService.generateToken("testuser", 1L, "USER");

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.here"));
    }

    @Test
    void shouldReturnFalseForEmptyToken() {
        assertFalse(jwtService.validateToken(""));
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String token = jwtService.generateToken("testuser", 123L, "USER");

        Long userId = jwtService.getUserIdFromToken(token);

        assertEquals(123L, userId);
    }

    @Test
    void shouldExtractRoleFromToken() {
        String token = jwtService.generateToken("testuser", 1L, "ADMIN");

        String role = jwtService.getRoleFromToken(token);

        assertEquals("ADMIN", role);
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtService.generateToken("user1", 1L, "USER");
        String token2 = jwtService.generateToken("user2", 2L, "ADMIN");

        assertNotEquals(token1, token2);
    }
}