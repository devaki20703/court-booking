package com.courtbooking.apigateway.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private GatewayFilterChain mockChain;

    private JwtAuthenticationFilter filter;
    private SecretKey secretKey;
    private static final String JWT_SECRET = "very-long-secret-key-that-is-at-least-32-bytes-long-for-hs256";

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(JWT_SECRET);
        secretKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String createValidToken(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(secretKey)
                .compact();
    }

    private String createExpiredToken(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis() - 172800000))
                .expiration(new Date(System.currentTimeMillis() - 86400000))
                .signWith(secretKey)
                .compact();
    }

    @Test
    void shouldSkipAuthForPublicPath() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(mockChain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(exchange);
    }

    @Test
    void shouldSkipAuthForRegisterPath() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/auth/register").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(mockChain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(exchange);
    }

    @Test
    void shouldSkipAuthForEurekaPath() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/eureka/apps").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(mockChain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(exchange);
    }

    @Test
    void shouldSkipAuthForActuatorPath() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(mockChain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(exchange);
    }

    @Test
    void shouldDenyRequestWithNoAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/bookings").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, mockChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(mockChain, never()).filter(any());
    }

    @Test
    void shouldDenyRequestWithInvalidBearerFormat() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/bookings")
                .header("Authorization", "Basic abc123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, mockChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldDenyRequestWithInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/bookings")
                .header("Authorization", "Bearer invalid.token.here")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, mockChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldDenyRequestWithExpiredToken() {
        String expiredToken = createExpiredToken(1L, "testuser", "USER");
        MockServerHttpRequest request = MockServerHttpRequest.get("/bookings")
                .header("Authorization", "Bearer " + expiredToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, mockChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldAllowRequestWithValidToken() {
        String validToken = createValidToken(1L, "testuser", "USER");
        MockServerHttpRequest request = MockServerHttpRequest.get("/bookings")
                .header("Authorization", "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(mockChain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(any());
    }

    @Test
    void shouldAddUserIdHeader() {
        String validToken = createValidToken(123L, "testuser", "ADMIN");
        MockServerHttpRequest request = MockServerHttpRequest.get("/users")
                .header("Authorization", "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(mockChain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(argThat(ex -> {
            String userId = ex.getRequest().getHeaders().getFirst("X-User-Id");
            return "123".equals(userId);
        }));
    }

    @Test
    void shouldAddRoleHeader() {
        String validToken = createValidToken(1L, "testuser", "ADMIN");
        MockServerHttpRequest request = MockServerHttpRequest.get("/bookings")
                .header("Authorization", "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(mockChain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(argThat(ex -> {
            String role = ex.getRequest().getHeaders().getFirst("X-User-Role");
            return "ADMIN".equals(role);
        }));
    }

    @Test
    void shouldAddUsernameHeader() {
        String validToken = createValidToken(1L, "johndoe", "USER");
        MockServerHttpRequest request = MockServerHttpRequest.get("/bookings")
                .header("Authorization", "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(mockChain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(argThat(ex -> {
            String username = ex.getRequest().getHeaders().getFirst("X-Username");
            return "johndoe".equals(username);
        }));
    }

    @Test
    void shouldReturnCorrectFilterOrder() {
        assertEquals(-100, filter.getOrder());
    }
}