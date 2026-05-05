package com.courtbooking.paymentservice.service;

import com.courtbooking.paymentservice.dto.PaymentRequest;
import com.courtbooking.paymentservice.dto.PaymentResponse;
import com.courtbooking.paymentservice.entity.Payment;
import com.courtbooking.paymentservice.entity.PaymentStatus;
import com.courtbooking.paymentservice.exception.BadRequestException;
import com.courtbooking.paymentservice.exception.ResourceNotFoundException;
import com.courtbooking.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for booking: {}, user: {}", request.getBookingId(), request.getUserId());

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = new Payment(
                null,
                request.getBookingId(),
                request.getUserId(),
                request.getAmount(),
                PaymentStatus.PENDING,
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "ONLINE",
                transactionId,
                null,
                null,
                null
        );

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created with transaction ID: {}", transactionId);

        return mapToResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse processPayment(Long paymentId) {
        log.info("Processing payment with ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Payment cannot be processed. Current status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(java.time.LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment processed successfully. Transaction ID: {}", payment.getTransactionId());

        return mapToResponse(updatedPayment);
    }

    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        log.info("Processing refund for payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Only completed payments can be refunded. Current status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);

        Payment refundedPayment = paymentRepository.save(payment);
        log.info("Payment refunded successfully. Transaction ID: {}", payment.getTransactionId());

        return mapToResponse(refundedPayment);
    }

    public PaymentResponse getPaymentById(Long paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        return mapToResponse(payment);
    }

    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        log.info("Fetching payment with transaction ID: {}", transactionId);

        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction ID: " + transactionId));

        return mapToResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        log.info("Fetching payments for user ID: {}", userId);

        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsByBookingId(Long bookingId) {
        log.info("Fetching payments for booking ID: {}", bookingId);

        List<Payment> payments = paymentRepository.findByBookingId(bookingId);
        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getPaymentDate(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}