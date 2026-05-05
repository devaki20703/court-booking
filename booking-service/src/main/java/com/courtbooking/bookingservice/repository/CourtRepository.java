package com.courtbooking.bookingservice.repository;

import com.courtbooking.bookingservice.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
}