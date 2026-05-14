package com.courtbooking.bookingservice.service;

import com.courtbooking.bookingservice.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class PaymentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceClient.class);

    private final WebClient webClient;

    public PaymentServiceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Long createPayment(Long bookingId, Long userId, BigDecimal amount) {
        try {
            Map<String, Object> response = webClient.post()
                    .uri("/api/payments")
                    .bodyValue(Map.of(
                            "bookingId", bookingId,
                            "userId", userId,
                            "amount", amount,
                            "paymentMethod", "ONLINE"
                    ))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(
                            new BadRequestException("Payment creation failed")))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(
                            new BadRequestException("Payment service error")))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null && response.containsKey("id")) {
                return ((Number) response.get("id")).longValue();
            }
            return null;
        } catch (Exception e) {
            log.error("Error creating payment for booking {}: {}", bookingId, e.getMessage());
            throw new BadRequestException("Failed to create payment: " + e.getMessage());
        }
    }

    public Mono<Long> createPaymentAsync(Long bookingId, Long userId, BigDecimal amount) {
        return webClient.post()
                .uri("/api/payments")
                .bodyValue(Map.of(
                        "bookingId", bookingId,
                        "userId", userId,
                        "amount", amount,
                        "paymentMethod", "ONLINE"
                ))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    if (response != null && response.containsKey("id")) {
                        return ((Number) response.get("id")).longValue();
                    }
                    return -1L;
                })
                .doOnNext(id -> log.info("Async payment created with ID: {}", id))
                .onErrorResume(e -> {
                    log.error("Async payment creation failed: {}", e.getMessage());
                    return Mono.just(-1L);
                });
    }

    public boolean processPayment(Long paymentId) {
        try {
            webClient.post()
                    .uri("/api/payments/" + paymentId + "/process")
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.error("Error processing payment {}: {}", paymentId, e.getMessage());
            return false;
        }
    }
}