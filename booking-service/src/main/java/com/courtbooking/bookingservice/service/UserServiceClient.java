package com.courtbooking.bookingservice.service;

import com.courtbooking.bookingservice.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final WebClient webClient;

    public UserServiceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public boolean validateUser(Long userId) {
        try {
            Boolean result = webClient.get()
                    .uri("/users/validate/" + userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                    .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(
                            new BadRequestException("User service error")))
                    .bodyToMono(Boolean.class)
                    .block();

            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Error validating user {}: {}", userId, e.getMessage());
            throw new BadRequestException("Invalid user ID: " + userId);
        }
    }

    public Mono<Boolean> validateUserAsync(Long userId) {
        return webClient.get()
                .uri("/users/validate/" + userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false)
                .doOnNext(result -> log.debug("Async validation for user {}: {}", userId, result));
    }
}