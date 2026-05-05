package com.courtbooking.bookingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    private final String userServiceUrl;

    public AppConfig(@Value("${user-service.url}") String userServiceUrl) {
        this.userServiceUrl = userServiceUrl;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getUserServiceUrl() {
        return userServiceUrl;
    }
}