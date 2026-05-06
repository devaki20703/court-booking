package com.courtbooking.bookingservice.controller;

import com.courtbooking.bookingservice.dto.BookingDTO;
import com.courtbooking.bookingservice.dto.BookingRequest;
import com.courtbooking.bookingservice.exception.BadRequestException;
import com.courtbooking.bookingservice.exception.ResourceNotFoundException;
import com.courtbooking.bookingservice.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingRequest validRequest;
    private BookingDTO validResponse;

    @BeforeEach
    void setUp() {
        validRequest = new BookingRequest();
        validRequest.setUserId(1L);
        validRequest.setCourtId(1L);
        validRequest.setBookingDate(LocalDate.now().plusDays(1));
        validRequest.setStartTime(LocalTime.of(10, 0));
        validRequest.setEndTime(LocalTime.of(11, 0));
        validRequest.setNotes("Test booking");

        validResponse = new BookingDTO();
        validResponse.setId(1L);
        validResponse.setUserId(1L);
        validResponse.setCourtId(1L);
        validResponse.setCourtName("Court Alpha");
        validResponse.setBookingDate(LocalDate.now().plusDays(1));
        validResponse.setStartTime(LocalTime.of(10, 0));
        validResponse.setEndTime(LocalTime.of(11, 0));
        validResponse.setStatus("CONFIRMED");
        validResponse.setNotes("Test booking");
    }

    @Test
    void createBooking_ShouldReturnCreated_WhenValidRequest() throws Exception {
        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(validResponse);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createBooking_ShouldReturnBadRequest_WhenInvalidUser() throws Exception {
        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenThrow(new BadRequestException("Invalid user ID: 999"));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        BookingRequest invalidRequest = new BookingRequest();
        invalidRequest.setUserId(null);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelBooking_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        doNothing().when(bookingService).cancelBooking(1L, 1L);

        mockMvc.perform(delete("/bookings/1")
                        .param("userId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancelBooking_ShouldReturnNotFound_WhenBookingNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Booking not found with id: 999"))
                .when(bookingService).cancelBooking(999L, 1L);

        mockMvc.perform(delete("/bookings/999")
                        .param("userId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingsByUser_ShouldReturnOk_WhenUserHasBookings() throws Exception {
        when(bookingService.getBookingsByUser(1L)).thenReturn(List.of(validResponse));

        mockMvc.perform(get("/bookings/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].courtName").value("Court Alpha"));
    }

    @Test
    void getBookingsByUser_ShouldReturnEmptyList_WhenUserHasNoBookings() throws Exception {
        when(bookingService.getBookingsByUser(1L)).thenReturn(List.of());

        mockMvc.perform(get("/bookings/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAvailableSlots_ShouldReturnOk_WhenSlotsExist() throws Exception {
        when(bookingService.getAvailableSlots(any(LocalDate.class))).thenReturn(List.of(validResponse));

        mockMvc.perform(get("/bookings/available")
                        .param("date", "2026-12-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAvailableSlots_ShouldReturnOk_WhenNoSlotsAvailable() throws Exception {
        when(bookingService.getAvailableSlots(any(LocalDate.class))).thenReturn(List.of());

        mockMvc.perform(get("/bookings/available")
                        .param("date", "2026-12-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}