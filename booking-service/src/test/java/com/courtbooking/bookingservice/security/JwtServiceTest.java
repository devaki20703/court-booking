package com.courtbooking.bookingservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "mySecretKey12345678901234567890123456789012345678901234567890";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        String token = createValidToken();

        boolean isValid = jwtService.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.string";

        boolean isValid = jwtService.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject("1")
                .claim("role", "USER")
                .claim("username", "testuser")
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        boolean isValid = jwtService.validateToken(expiredToken);

        assertFalse(isValid);
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String token = createValidToken();

        Long userId = jwtService.getUserIdFromToken(token);

        assertEquals(1L, userId);
    }

    @Test
    void shouldExtractRoleFromToken() {
        String token = createValidToken();

        String role = jwtService.getRoleFromToken(token);

        assertEquals("ADMIN", role);
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = createValidToken();

        String username = jwtService.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }

    @Test
    void shouldThrowExceptionForInvalidTokenUserId() {
        String invalidToken = "invalid.token";

        assertThrows(Exception.class, () -> jwtService.getUserIdFromToken(invalidToken));
    }

    @Test
    void shouldThrowExceptionForInvalidTokenRole() {
        String invalidToken = "invalid.token";

        assertThrows(Exception.class, () -> jwtService.getRoleFromToken(invalidToken));
    }

    @Test
    void shouldThrowExceptionForInvalidTokenUsername() {
        String invalidToken = "invalid.token";

        assertThrows(Exception.class, () -> jwtService.getUsernameFromToken(invalidToken));
    }

    private String createValidToken() {
        return Jwts.builder()
                .subject("1")
                .claim("role", "ADMIN")
                .claim("username", "testuser")
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}