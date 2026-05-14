package com.courtbooking.bookingservice.service;

import com.courtbooking.bookingservice.dto.BookingDTO;
import com.courtbooking.bookingservice.dto.BookingRequest;
import com.courtbooking.bookingservice.entity.Booking;
import com.courtbooking.bookingservice.entity.BookingStatus;
import com.courtbooking.bookingservice.entity.Court;
import com.courtbooking.bookingservice.exception.BadRequestException;
import com.courtbooking.bookingservice.exception.ResourceNotFoundException;
import com.courtbooking.bookingservice.repository.BookingRepository;
import com.courtbooking.bookingservice.repository.CourtRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final UserServiceClient userServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    public BookingService(BookingRepository bookingRepository, CourtRepository courtRepository, 
                          UserServiceClient userServiceClient, PaymentServiceClient paymentServiceClient) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.userServiceClient = userServiceClient;
        this.paymentServiceClient = paymentServiceClient;
    }

    @Transactional
    public BookingDTO createBooking(BookingRequest request) {
        if (!userServiceClient.validateUser(request.getUserId())) {
            throw new BadRequestException("Invalid user ID: " + request.getUserId());
        }

        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + request.getCourtId()));

        if (!court.getAvailable()) {
            throw new BadRequestException("Court is not available");
        }

        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                request.getCourtId(),
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            throw new BadRequestException("Court is already booked for the selected time slot");
        }

        double hours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();
        double amount = 0;
        if (court.getPricePerHour() != null) {
            amount = hours * court.getPricePerHour();
        }

        Booking booking = new Booking(
            request.getUserId(),
            court,
            request.getBookingDate(),
            request.getStartTime(),
            request.getEndTime(),
            BookingStatus.CONFIRMED,
            request.getNotes()
        );

        booking = bookingRepository.save(booking);

        if (amount > 0) {
            Long paymentId = paymentServiceClient.createPayment(
                booking.getId(),
                request.getUserId(),
                BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
            );
            booking.setPaymentId(paymentId);
            booking = bookingRepository.save(booking);
        }

        return mapToDTO(booking, amount);
    }

    @Transactional
    public void cancelBooking(Long id, Long userId) {
        Booking booking = bookingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    public List<BookingDTO> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getAvailableSlots(LocalDate date) {
        return courtRepository.findAll().stream()
                .filter(c -> c.getAvailable())
                .map(court -> {
                    List<Booking> bookings = bookingRepository.findConflictingBookings(
                            court.getId(), date,
                            java.time.LocalTime.of(6, 0),
                            java.time.LocalTime.of(22, 0)
                    );
                    return mapToDTO(bookings.isEmpty() ? null : bookings.get(0));
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    public List<Court> getAvailableCourtsByDate(LocalDate date) {
        return courtRepository.findAll().stream()
                .filter(c -> c.getAvailable())
                .filter(court -> {
                    List<Booking> bookings = bookingRepository.findConflictingBookings(
                            court.getId(), date,
                            java.time.LocalTime.of(6, 0),
                            java.time.LocalTime.of(22, 0)
                    );
                    return bookings.isEmpty();
                })
                .collect(Collectors.toList());
    }

    private BookingDTO mapToDTO(Booking booking) {
        if (booking == null) {
            return null;
        }
        return mapToDTO(booking, 0);
    }

    private BookingDTO mapToDTO(Booking booking, double amount) {
        if (booking == null) {
            return null;
        }
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUserId());
        dto.setCourtId(booking.getCourt().getId());
        dto.setCourtName(booking.getCourt().getName());
        dto.setBookingDate(booking.getBookingDate());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setStatus(booking.getStatus().name());
        dto.setNotes(booking.getNotes());
        dto.setPaymentId(booking.getPaymentId());
        dto.setAmount(amount);
        return dto;
    }
}