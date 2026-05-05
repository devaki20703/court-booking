package com.courtbooking.bookingservice.repository;

import com.courtbooking.bookingservice.entity.Booking;
import com.courtbooking.bookingservice.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    List<Booking> findByCourtIdAndStatus(Long courtId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.court.id = :courtId AND b.bookingDate = :date AND b.status = 'CONFIRMED' AND " +
           "((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findConflictingBookings(
            @Param("courtId") Long courtId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    Optional<Booking> findByIdAndUserId(Long id, Long userId);
}