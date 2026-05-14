package com.courtbooking.apigateway.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private GatewayAuthorizationFilter gatewayAuthorizationFilter;

    @Mock
    private GatewayFilterChain gatewayFilterChain;

    @Test
    void shouldLoadSecurityConfig() {
        assertNotNull(jwtAuthenticationFilter);
        assertNotNull(gatewayAuthorizationFilter);
    }
}