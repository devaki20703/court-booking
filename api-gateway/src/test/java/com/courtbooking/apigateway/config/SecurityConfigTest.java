package com.courtbooking.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "app.jwt.secret=very-long-secret-key-that-is-at-least-32-bytes-long-for-hs256",
    "app.jwt.expiration=86400000"
})
class SecurityConfigTest {

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private GatewayAuthorizationFilter gatewayAuthorizationFilter;

    @Test
    void shouldLoadSecurityConfig() {
        assertNotNull(jwtAuthenticationFilter);
        assertNotNull(gatewayAuthorizationFilter);
    }
}