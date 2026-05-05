package com.courtbooking.bookingservice.service;

import com.courtbooking.bookingservice.config.AppConfig;
import com.courtbooking.bookingservice.exception.BadRequestException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class PaymentServiceClient {

    private final RestTemplate restTemplate;
    private final AppConfig appConfig;

    public PaymentServiceClient(RestTemplate restTemplate, AppConfig appConfig) {
        this.restTemplate = restTemplate;
        this.appConfig = appConfig;
    }

    public Long createPayment(Long bookingId, Long userId, BigDecimal amount) {
        try {
            String url = appConfig.getPaymentServiceUrl() + "/api/payments";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String requestBody = String.format(
                "{\"bookingId\":%d,\"userId\":%d,\"amount\":%s,\"paymentMethod\":\"ONLINE\"}",
                bookingId, userId, amount
            );
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Number id = (Number) response.getBody().get("id");
                return id.longValue();
            }
            return null;
        } catch (Exception e) {
            throw new BadRequestException("Failed to create payment: " + e.getMessage());
        }
    }

    public boolean processPayment(Long paymentId) {
        try {
            String url = appConfig.getPaymentServiceUrl() + "/api/payments/" + paymentId + "/process";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}