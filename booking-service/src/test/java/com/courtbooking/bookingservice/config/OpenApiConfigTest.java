package com.courtbooking.bookingservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void shouldCreateCustomOpenAPI() {
        OpenApiConfig openApiConfig = new OpenApiConfig();

        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("Booking Service API", openAPI.getInfo().getTitle());
        assertEquals("1.0", openAPI.getInfo().getVersion());
        assertEquals("Court Booking Management Service API", openAPI.getInfo().getDescription());
    }
}