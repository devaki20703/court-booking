package com.courtbooking.apigateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GatewayAuthorizationFilter implements GlobalFilter, Ordered {

    private static final String USER_ROLE = "USER";
    private static final String ADMIN_ROLE = "ADMIN";

    private static final Map<String, Set<String>> ROLE_PERMISSIONS = Map.of(
            "/users", Set.of(USER_ROLE, ADMIN_ROLE),
            "/bookings", Set.of(USER_ROLE, ADMIN_ROLE),
            "/payments", Set.of(USER_ROLE, ADMIN_ROLE)
    );

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/",
            "/eureka/",
            "/actuator/",
            "/swagger-ui/",
            "/api-docs/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String userRole = request.getHeaders().getFirst("X-User-Role");

        if (!hasPermission(path, userRole)) {
            return forbidden(exchange.getResponse(), "Access denied for this resource");
        }

        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean hasPermission(String path, String userRole) {
        if (userRole == null || userRole.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, Set<String>> entry : ROLE_PERMISSIONS.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue().contains(userRole);
            }
        }

        return true;
    }

    private Mono<Void> forbidden(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"%s\"}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -2000;
    }
}