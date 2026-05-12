package com.courtbooking.apigateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Order(-2)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (ex instanceof ResponseStatusException) {
            ResponseStatusException statusException = (ResponseStatusException) ex;
            response.setStatusCode(statusException.getStatusCode());
        } else if (ex.getMessage() != null && ex.getMessage().contains("401")) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
        } else if (ex.getMessage() != null && ex.getMessage().contains("403")) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
            "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
            response.getStatusCode().value(),
            response.getStatusCode().toString().replace(" ", "_"),
            ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
            LocalDateTime.now()
        );

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}