package com.courtbooking.bookingservice.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class BookingRequest {
    private Long userId;
    private Long courtId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String notes;

    public BookingRequest() {}
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCourtId() { return courtId; }
    public void setCourtId(Long courtId) { this.courtId = courtId; }
    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}