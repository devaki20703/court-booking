package com.courtbooking.paymentservice.controller;

import com.courtbooking.paymentservice.dto.PaymentRequest;
import com.courtbooking.paymentservice.dto.PaymentResponse;
import com.courtbooking.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment Controller", description = "Payment management APIs")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Create a new payment", description = "Create a new payment for a booking")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{paymentId}/process")
    @Operation(summary = "Process a payment", description = "Process a pending payment")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.processPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund a payment", description = "Refund a completed payment")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID", description = "Retrieve a payment by its ID")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transaction/{transactionId}")
    @Operation(summary = "Get payment by transaction ID", description = "Retrieve a payment by its transaction ID")
    public ResponseEntity<PaymentResponse> getPaymentByTransactionId(@PathVariable String transactionId) {
        PaymentResponse response = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get payments by user ID", description = "Retrieve all payments for a specific user")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Long userId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payments by booking ID", description = "Retrieve all payments for a specific booking")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByBookingId(@PathVariable Long bookingId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByBookingId(bookingId);
        return ResponseEntity.ok(payments);
    }
}