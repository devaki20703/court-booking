package com.courtbooking.bookingservice.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AppConfigTest {

    @Test
    void shouldCreateWebClientBean() {
        AppConfig appConfig = new AppConfig();
        WebClient webClient = appConfig.webClient();
        assertNotNull(webClient);
    }
}