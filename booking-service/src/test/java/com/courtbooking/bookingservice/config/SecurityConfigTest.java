package com.courtbooking.bookingservice.config;

import com.courtbooking.bookingservice.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldAllowSwaggerEndpoints() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowSwaggerUiEndpoints() throws Exception {
        mockMvc.perform(get("/swagger-ui/**"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyCourtAccessForNonAdminUser() throws Exception {
        mockMvc.perform(get("/courts"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowCourtAccessForAdminUser() throws Exception {
        mockMvc.perform(get("/courts"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAuthenticatedEndpointsForUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/bookings/user/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void shouldAllowAuthenticatedEndpoints() throws Exception {
        mockMvc.perform(get("/bookings/user/1"))
                .andExpect(status().isOk());
    }
}