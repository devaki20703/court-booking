package com.courtbooking.bookingservice.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AppConfigTest {

    @Test
    void shouldCreateRestTemplateBean() {
        AppConfig appConfig = new AppConfig("http://user-service", "http://payment-service");

        RestTemplate restTemplate = appConfig.restTemplate();

        assertNotNull(restTemplate);
    }

    @Test
    void shouldReturnUserServiceUrl() {
        AppConfig appConfig = new AppConfig("http://user-service", "http://payment-service");

        String url = appConfig.getUserServiceUrl();

        assertEquals("http://user-service", url);
    }

    @Test
    void shouldReturnPaymentServiceUrl() {
        AppConfig appConfig = new AppConfig("http://user-service", "http://payment-service");

        String url = appConfig.getPaymentServiceUrl();

        assertEquals("http://payment-service", url);
    }
}