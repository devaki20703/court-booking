package com.courtbooking.bookingservice.controller;

import com.courtbooking.bookingservice.dto.BookingDTO;
import com.courtbooking.bookingservice.dto.BookingRequest;
import com.courtbooking.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Create a booking", description = "Books a court for a specific time slot")
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody BookingRequest request) {
        return new ResponseEntity<>(bookingService.createBooking(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a booking", description = "Cancels an existing booking")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long id,
            @RequestParam Long userId) {
        bookingService.cancelBooking(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all bookings", description = "Returns all bookings")
    public ResponseEntity<List<BookingDTO>> getAllBookings(@RequestHeader(value = "X-User-Role", required = false) String userRole) {
        System.out.println("DEBUG: X-User-Role header = [" + userRole + "]");
        List<BookingDTO> bookings = bookingService.getAllBookings();
        System.out.println("DEBUG: Found " + bookings.size() + " bookings");
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bookings by user", description = "Returns all bookings for a specific user")
    public ResponseEntity<List<BookingDTO>> getBookingsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available slots by date", description = "Returns available time slots for all courts on a specific date")
    public ResponseEntity<List<BookingDTO>> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(bookingService.getAvailableSlots(date));
    }
}