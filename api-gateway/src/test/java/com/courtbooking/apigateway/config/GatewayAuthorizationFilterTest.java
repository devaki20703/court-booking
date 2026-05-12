package com.courtbooking.apigateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GatewayAuthorizationFilterTest {

    private GatewayAuthorizationFilter filter;
    private GatewayFilterChain mockChain;

    @BeforeEach
    void setUp() {
        filter = new GatewayAuthorizationFilter();
        mockChain = mock(GatewayFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowPublicPathWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(exchange);
    }

    @Test
    void shouldAllowPublicPathEureka() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/eureka/apps").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(exchange);
    }

    @Test
    void shouldAllowProtectedPathWithUserRole() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/bookings")
                .header("X-User-Role", "USER")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(exchange);
    }

    @Test
    void shouldAllowProtectedPathWithAdminRole() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/users")
                .header("X-User-Role", "ADMIN")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(exchange);
    }

    @Test
    void shouldDenyAccessWithoutRole() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/bookings").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, mockChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(mockChain, never()).filter(exchange);
    }

    @Test
    void shouldDenyAccessWithEmptyRole() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/users")
                .header("X-User-Role", "")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, mockChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldDenyAccessWithInvalidRole() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/bookings")
                .header("X-User-Role", "GUEST")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, mockChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldAllowNestedPaths() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/users/123/profile")
                .header("X-User-Role", "USER")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(exchange);
    }

    @Test
    void shouldDenyAccessToPaymentsWithUserRole() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/payments")
                .header("X-User-Role", "USER")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, mockChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldReturnCorrectErrorBody() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/bookings").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, mockChain);

        assertEquals("application/json", exchange.getResponse().getHeaders().getFirst("Content-Type"));
    }

    @Test
    void shouldAllowSwaggerPaths() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/swagger-ui/index.html").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, mockChain);

        verify(mockChain).filter(exchange);
    }
}
