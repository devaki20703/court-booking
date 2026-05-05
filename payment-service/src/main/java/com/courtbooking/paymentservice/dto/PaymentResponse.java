package com.courtbooking.paymentservice.dto;

import com.courtbooking.paymentservice.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private Long bookingId;
    private Long userId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentResponse() {}

    public PaymentResponse(Long id, Long bookingId, Long userId, BigDecimal amount, PaymentStatus status,
                            String paymentMethod, String transactionId, LocalDateTime paymentDate,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.paymentDate = paymentDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}