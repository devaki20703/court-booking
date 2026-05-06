package com.courtbooking.paymentservice.controller;

import com.courtbooking.paymentservice.dto.PaymentRequest;
import com.courtbooking.paymentservice.dto.PaymentResponse;
import com.courtbooking.paymentservice.entity.PaymentStatus;
import com.courtbooking.paymentservice.exception.BadRequestException;
import com.courtbooking.paymentservice.exception.ResourceNotFoundException;
import com.courtbooking.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    void shouldCreatePayment() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setUserId(100L);
        request.setAmount(new BigDecimal("50.00"));
        request.setPaymentMethod("CREDIT_CARD");

        PaymentResponse response = new PaymentResponse(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.PENDING, "CREDIT_CARD", "TXN-ABC123",
                null, null, null
        );

        when(paymentService.createPayment(any(PaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.transactionId").value("TXN-ABC123"));
    }

    @Test
    void shouldProcessPayment() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.COMPLETED, "CREDIT_CARD", "TXN-ABC123",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(paymentService.processPayment(1L)).thenReturn(response);

        mockMvc.perform(post("/api/payments/1/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldRefundPayment() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.REFUNDED, "CREDIT_CARD", "TXN-ABC123",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(paymentService.refundPayment(1L)).thenReturn(response);

        mockMvc.perform(post("/api/payments/1/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    void shouldGetPaymentById() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.COMPLETED, "CREDIT_CARD", "TXN-ABC123",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(paymentService.getPaymentById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404WhenPaymentNotFound() throws Exception {
        when(paymentService.getPaymentById(1L)).thenThrow(new ResourceNotFoundException("Payment not found with ID: 1"));

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetPaymentByTransactionId() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.COMPLETED, "CREDIT_CARD", "TXN-ABC123",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(paymentService.getPaymentByTransactionId("TXN-ABC123")).thenReturn(response);

        mockMvc.perform(get("/api/payments/transaction/TXN-ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-ABC123"));
    }

    @Test
    void shouldGetPaymentsByUserId() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.COMPLETED, "CREDIT_CARD", "TXN-ABC123",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(paymentService.getPaymentsByUserId(100L)).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/api/payments/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetPaymentsByBookingId() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.COMPLETED, "CREDIT_CARD", "TXN-ABC123",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(paymentService.getPaymentsByBookingId(1L)).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/api/payments/booking/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReturn400WhenProcessingInvalidPayment() throws Exception {
        when(paymentService.processPayment(1L)).thenThrow(new BadRequestException("Payment cannot be processed"));

        mockMvc.perform(post("/api/payments/1/process"))
                .andExpect(status().isBadRequest());
    }
}