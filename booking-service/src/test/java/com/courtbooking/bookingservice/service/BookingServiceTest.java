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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingRepository, courtRepository, userServiceClient, paymentServiceClient);
    }

    @Test
    void shouldCreateBookingSuccessfully() {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setCourtId(1L);
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(11, 0));
        request.setNotes("Test booking");

        Court court = new Court();
        court.setName("Court Alpha");
        court.setSportType("Tennis");
        court.setLocation("Location 1");
        court.setAvailable(true);
        court.setDescription("Description");
        court.setId(1L);
        court.setPricePerHour(50.0);

        when(userServiceClient.validateUser(1L)).thenReturn(true);
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(bookingRepository.findConflictingBookings(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });
        when(paymentServiceClient.createPayment(any(), any(), any())).thenReturn(100L);

        BookingDTO result = bookingService.createBooking(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getCourtId());
        assertEquals("CONFIRMED", result.getStatus());
        verify(bookingRepository, times(2)).save(any(Booking.class));
        verify(paymentServiceClient).createPayment(any(), any(), any());
    }

    @Test
    void shouldCreateBookingWithoutPaymentWhenPriceIsNull() {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setCourtId(1L);
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(11, 0));

        Court court = new Court();
        court.setName("Court Alpha");
        court.setSportType("Tennis");
        court.setLocation("Location 1");
        court.setAvailable(true);
        court.setDescription("Description");
        court.setId(1L);
        court.setPricePerHour(null);

        when(userServiceClient.validateUser(1L)).thenReturn(true);
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(bookingRepository.findConflictingBookings(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });

        BookingDTO result = bookingService.createBooking(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(paymentServiceClient, never()).createPayment(any(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenInvalidUser() {
        BookingRequest request = new BookingRequest();
        request.setUserId(999L);
        request.setCourtId(1L);

        when(userServiceClient.validateUser(999L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(request));
    }

    @Test
    void shouldThrowExceptionWhenCourtNotFound() {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setCourtId(999L);

        when(userServiceClient.validateUser(1L)).thenReturn(true);
        when(courtRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(request));
    }

    @Test
    void shouldThrowExceptionWhenCourtNotAvailable() {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setCourtId(1L);

        Court court = new Court();
        court.setName("Court Alpha");
        court.setSportType("Tennis");
        court.setLocation("Location 1");
        court.setAvailable(false);
        court.setDescription("Description");
        court.setId(1L);

        when(userServiceClient.validateUser(1L)).thenReturn(true);
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(request));
    }

    @Test
    void shouldThrowExceptionWhenTimeSlotConflict() {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setCourtId(1L);
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(11, 0));

        Court court = new Court();
        court.setName("Court Alpha");
        court.setSportType("Tennis");
        court.setLocation("Location 1");
        court.setAvailable(true);
        court.setDescription("Description");
        court.setId(1L);

        Booking existingBooking = new Booking();
        existingBooking.setId(2L);

        when(userServiceClient.validateUser(1L)).thenReturn(true);
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(bookingRepository.findConflictingBookings(any(), any(), any(), any()))
                .thenReturn(List.of(existingBooking));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(request));
    }

    @Test
    void shouldCancelBookingSuccessfully() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUserId(1L);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.cancelBooking(1L, 1L);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void shouldThrowExceptionWhenCancelNonExistentBooking() {
        when(bookingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.cancelBooking(1L, 1L));
    }

    @Test
    void shouldGetBookingsByUser() {
        Court court = new Court("Court Alpha", "Tennis", "Location 1", true, "Description");
        court.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUserId(1L);
        booking.setCourt(court);
        booking.setBookingDate(LocalDate.now());
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(11, 0));
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(booking));

        List<BookingDTO> result = bookingService.getBookingsByUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void shouldGetAvailableSlots() {
        Court court = new Court();
        court.setId(1L);
        court.setName("Court Alpha");
        court.setSportType("Tennis");
        court.setLocation("Location 1");
        court.setAvailable(true);

        when(courtRepository.findAll()).thenReturn(List.of(court));
        when(bookingRepository.findConflictingBookings(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<BookingDTO> result = bookingService.getAvailableSlots(LocalDate.now());

        assertNotNull(result);
    }

    @Test
    void shouldGetAvailableSlotsWithExistingBookings() {
        Court court = new Court();
        court.setId(1L);
        court.setName("Court Alpha");
        court.setSportType("Tennis");
        court.setLocation("Location 1");
        court.setAvailable(true);

        Booking existingBooking = new Booking();
        existingBooking.setId(1L);
        existingBooking.setUserId(1L);
        existingBooking.setCourt(court);
        existingBooking.setBookingDate(LocalDate.now());
        existingBooking.setStartTime(java.time.LocalTime.of(10, 0));
        existingBooking.setEndTime(java.time.LocalTime.of(11, 0));
        existingBooking.setStatus(BookingStatus.CONFIRMED);

        when(courtRepository.findAll()).thenReturn(List.of(court));
        when(bookingRepository.findConflictingBookings(any(), any(), any(), any()))
                .thenReturn(List.of(existingBooking));

        List<BookingDTO> result = bookingService.getAvailableSlots(LocalDate.now());

        assertNotNull(result);
    }

    @Test
    void shouldGetAvailableCourtsByDate() {
        Court availableCourt = new Court();
        availableCourt.setId(1L);
        availableCourt.setName("Court Alpha");
        availableCourt.setSportType("Tennis");
        availableCourt.setLocation("Location 1");
        availableCourt.setAvailable(true);

        when(courtRepository.findAll()).thenReturn(List.of(availableCourt));
        when(bookingRepository.findConflictingBookings(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<Court> result = bookingService.getAvailableCourtsByDate(LocalDate.now());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Court Alpha", result.get(0).getName());
    }

    @Test
    void shouldGetAvailableCourtsByDateWithNoAvailability() {
        Court court = new Court();
        court.setId(1L);
        court.setName("Court Alpha");
        court.setSportType("Tennis");
        court.setLocation("Location 1");
        court.setAvailable(true);

        Booking existingBooking = new Booking();
        existingBooking.setId(1L);
        existingBooking.setCourt(court);

        when(courtRepository.findAll()).thenReturn(List.of(court));
        when(bookingRepository.findConflictingBookings(any(), any(), any(), any()))
                .thenReturn(List.of(existingBooking));

        List<Court> result = bookingService.getAvailableCourtsByDate(LocalDate.now());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}