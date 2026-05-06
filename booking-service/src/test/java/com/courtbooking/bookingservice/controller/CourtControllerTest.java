package com.courtbooking.bookingservice.controller;

import com.courtbooking.bookingservice.dto.CourtDTO;
import com.courtbooking.bookingservice.dto.CourtRequest;
import com.courtbooking.bookingservice.exception.ResourceNotFoundException;
import com.courtbooking.bookingservice.security.JwtAuthenticationFilter;
import com.courtbooking.bookingservice.security.JwtService;
import com.courtbooking.bookingservice.service.CourtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourtController.class)
class CourtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourtService courtService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private CourtRequest validRequest;
    private CourtDTO validResponse;

    @BeforeEach
    void setUp() {
        validRequest = new CourtRequest();
        validRequest.setName("Court Alpha");
        validRequest.setSportType("Tennis");
        validRequest.setLocation("Location 1");
        validRequest.setDescription("Test court");

        validResponse = new CourtDTO();
        validResponse.setId(1L);
        validResponse.setName("Court Alpha");
        validResponse.setSportType("Tennis");
        validResponse.setLocation("Location 1");
        validResponse.setAvailable(true);
        validResponse.setDescription("Test court");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addCourt_ShouldReturnCreated_WhenValidRequest() throws Exception {
        when(courtService.addCourt(any(CourtRequest.class))).thenReturn(validResponse);

        mockMvc.perform(post("/courts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Court Alpha"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addCourt_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        CourtRequest invalidRequest = new CourtRequest();
        invalidRequest.setName(null);

        mockMvc.perform(post("/courts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCourt_ShouldReturnOk_WhenValidRequest() throws Exception {
        when(courtService.updateCourt(anyLong(), any(CourtRequest.class))).thenReturn(validResponse);

        mockMvc.perform(put("/courts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCourt_ShouldReturnNotFound_WhenCourtNotExists() throws Exception {
        when(courtService.updateCourt(anyLong(), any(CourtRequest.class)))
                .thenThrow(new ResourceNotFoundException("Court not found with id: 999"));

        mockMvc.perform(put("/courts/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCourt_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        doNothing().when(courtService).deleteCourt(1L);

        mockMvc.perform(delete("/courts/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCourt_ShouldReturnNotFound_WhenCourtNotExists() throws Exception {
        doThrow(new ResourceNotFoundException("Court not found with id: 999"))
                .when(courtService).deleteCourt(999L);

        mockMvc.perform(delete("/courts/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCourtById_ShouldReturnOk_WhenCourtExists() throws Exception {
        when(courtService.getCourtById(1L)).thenReturn(validResponse);

        mockMvc.perform(get("/courts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Court Alpha"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCourtById_ShouldReturnNotFound_WhenCourtNotExists() throws Exception {
        when(courtService.getCourtById(999L))
                .thenThrow(new ResourceNotFoundException("Court not found with id: 999"));

        mockMvc.perform(get("/courts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCourts_ShouldReturnOk_WhenCourtsExist() throws Exception {
        when(courtService.getAllCourts()).thenReturn(List.of(validResponse));

        mockMvc.perform(get("/courts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Court Alpha"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCourts_ShouldReturnEmptyList_WhenNoCourts() throws Exception {
        when(courtService.getAllCourts()).thenReturn(List.of());

        mockMvc.perform(get("/courts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAvailableCourts_ShouldReturnOk_WhenCourtsAvailable() throws Exception {
        when(courtService.getAvailableCourts()).thenReturn(List.of(validResponse));

        mockMvc.perform(get("/courts/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void getCourtById_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/courts/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addCourt_ShouldReturnForbidden_WhenUserNotAdmin() throws Exception {
        mockMvc.perform(post("/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }
}