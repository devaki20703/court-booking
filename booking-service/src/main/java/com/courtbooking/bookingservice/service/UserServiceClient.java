package com.courtbooking.bookingservice.service;

import com.courtbooking.bookingservice.config.AppConfig;
import com.courtbooking.bookingservice.exception.BadRequestException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final AppConfig appConfig;

    public UserServiceClient(RestTemplate restTemplate, AppConfig appConfig) {
        this.restTemplate = restTemplate;
        this.appConfig = appConfig;
    }

    public boolean validateUser(Long userId) {
        try {
            String url = appConfig.getUserServiceUrl() + "/users/validate/" + userId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            var response = restTemplate.exchange(url, HttpMethod.GET, entity, Boolean.class);
            
            return response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody());
        } catch (Exception e) {
            throw new BadRequestException("Invalid user ID: " + userId);
        }
    }
}