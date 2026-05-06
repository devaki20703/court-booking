package com.courtbooking.paymentservice.service;

import com.courtbooking.paymentservice.dto.PaymentRequest;
import com.courtbooking.paymentservice.dto.PaymentResponse;
import com.courtbooking.paymentservice.entity.Payment;
import com.courtbooking.paymentservice.entity.PaymentStatus;
import com.courtbooking.paymentservice.exception.BadRequestException;
import com.courtbooking.paymentservice.exception.ResourceNotFoundException;
import com.courtbooking.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository);
    }

    @Test
    void shouldCreatePaymentSuccessfully() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setUserId(100L);
        request.setAmount(new BigDecimal("50.00"));
        request.setPaymentMethod("CREDIT_CARD");

        Payment savedPayment = new Payment(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.PENDING, "CREDIT_CARD", "TXN-ABC123",
                null, null, null
        );

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponse response = paymentService.createPayment(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertNotNull(response.getTransactionId());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldCreatePaymentWithDefaultPaymentMethod() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setUserId(100L);
        request.setAmount(new BigDecimal("50.00"));

        Payment savedPayment = new Payment(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.PENDING, "ONLINE", "TXN-ABC123",
                null, null, null
        );

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals("ONLINE", response.getPaymentMethod());
    }

    @Test
    void shouldProcessPaymentSuccessfully() {
        Payment payment = new Payment(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.PENDING, "CREDIT_CARD", "TXN-ABC123",
                null, null, null
        );

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(1L);
            p.setPaymentDate(LocalDateTime.now());
            return p;
        });

        PaymentResponse response = paymentService.processPayment(1L);

        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        assertNotNull(response.getPaymentDate());
    }

    @Test
    void shouldThrowExceptionWhenProcessingNonPendingPayment() {
        Payment payment = new Payment(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.COMPLETED, "CREDIT_CARD", "TXN-ABC123",
                null, null, null
        );

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThrows(BadRequestException.class, () -> paymentService.processPayment(1L));
    }

    @Test
    void shouldThrowExceptionWhenProcessingNonExistentPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.processPayment(1L));
    }

    @Test
    void shouldRefundPaymentSuccessfully() {
        Payment payment = new Payment(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.COMPLETED, "CREDIT_CARD", "TXN-ABC123",
                null, null, null
        );

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PaymentResponse response = paymentService.refundPayment(1L);

        assertEquals(PaymentStatus.REFUNDED, response.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenRefundingNonCompletedPayment() {
        Payment payment = new Payment(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.PENDING, "CREDIT_CARD", "TXN-ABC123",
                null, null, null
        );

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThrows(BadRequestException.class, () -> paymentService.refundPayment(1L));
    }

    @Test
    void shouldThrowExceptionWhenRefundingNonExistentPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.refundPayment(1L));
    }

    @Test
    void shouldGetPaymentById() {
        Payment payment = new Payment(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.COMPLETED, "CREDIT_CARD", "TXN-ABC123",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("TXN-ABC123", response.getTransactionId());
    }

    @Test
    void shouldThrowExceptionWhenPaymentNotFoundById() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentById(1L));
    }

    @Test
    void shouldGetPaymentByTransactionId() {
        Payment payment = new Payment(
                1L, 1L, 100L, new BigDecimal("50.00"),
                PaymentStatus.COMPLETED, "CREDIT_CARD", "TXN-ABC123",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(paymentRepository.findByTransactionId("TXN-ABC123")).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentByTransactionId("TXN-ABC123");

        assertNotNull(response);
        assertEquals("TXN-ABC123", response.getTransactionId());
    }

    @Test
    void shouldThrowExceptionWhenPaymentNotFoundByTransactionId() {
        when(paymentRepository.findByTransactionId("TXN-NOTFOUND")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentByTransactionId("TXN-NOTFOUND"));
    }

    @Test
    void shouldGetPaymentsByUserId() {
        List<Payment> payments = Arrays.asList(
                new Payment(1L, 1L, 100L, new BigDecimal("50.00"), PaymentStatus.COMPLETED, "CREDIT_CARD", "TXN-1", null, null, null),
                new Payment(2L, 2L, 100L, new BigDecimal("75.00"), PaymentStatus.PENDING, "DEBIT_CARD", "TXN-2", null, null, null)
        );

        when(paymentRepository.findByUserId(100L)).thenReturn(payments);

        List<PaymentResponse> responses = paymentService.getPaymentsByUserId(100L);

        assertEquals(2, responses.size());
    }

    @Test
    void shouldGetPaymentsByBookingId() {
        List<Payment> payments = Arrays.asList(
                new Payment(1L, 1L, 100L, new BigDecimal("50.00"), PaymentStatus.COMPLETED, "CREDIT_CARD", "TXN-1", null, null, null)
        );

        when(paymentRepository.findByBookingId(1L)).thenReturn(payments);

        List<PaymentResponse> responses = paymentService.getPaymentsByBookingId(1L);

        assertEquals(1, responses.size());
    }
}