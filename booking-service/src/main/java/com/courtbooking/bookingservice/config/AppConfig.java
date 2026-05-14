package com.courtbooking.bookingservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public WebClient webClient() {
        log.info("Initializing WebClient with base URL: http://localhost:8080");
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(10))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://localhost:8080")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}