package com.courtbooking.apigateway.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

class GatewayExceptionHandlerTest {

    private GatewayExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GatewayExceptionHandler();
    }

    @Test
    void shouldHandleResponseStatusException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldHandleUnauthorizedException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Exception ex = new Exception("401 Unauthorized");

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldHandleForbiddenException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Exception ex = new Exception("403 Forbidden");

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldHandleGenericException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Exception ex = new Exception("Something went wrong");

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldHandleNullMessageException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Exception ex = new Exception();

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldSetJsonContentType() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Exception ex = new Exception("Error");

        handler.handle(exchange, ex).block();

        assertEquals("application/json", exchange.getResponse().getHeaders().getFirst("Content-Type"));
    }
}