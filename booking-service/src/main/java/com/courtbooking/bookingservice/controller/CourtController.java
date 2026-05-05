package com.courtbooking.bookingservice.controller;

import com.courtbooking.bookingservice.dto.CourtDTO;
import com.courtbooking.bookingservice.dto.CourtRequest;
import com.courtbooking.bookingservice.service.CourtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courts")
@Tag(name = "Courts", description = "Court management endpoints")
public class CourtController {

    private final CourtService courtService;

    public CourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @PostMapping
    @Operation(summary = "Add a new court", description = "Creates a new court (ADMIN only)")
    public ResponseEntity<CourtDTO> addCourt(@Valid @RequestBody CourtRequest request) {
        return new ResponseEntity<>(courtService.addCourt(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update court", description = "Updates an existing court")
    public ResponseEntity<CourtDTO> updateCourt(@PathVariable Long id, @Valid @RequestBody CourtRequest request) {
        return ResponseEntity.ok(courtService.updateCourt(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete court", description = "Deletes a court by ID")
    public ResponseEntity<Void> deleteCourt(@PathVariable Long id) {
        courtService.deleteCourt(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get court by ID", description = "Returns court details by ID")
    public ResponseEntity<CourtDTO> getCourtById(@PathVariable Long id) {
        return ResponseEntity.ok(courtService.getCourtById(id));
    }

    @GetMapping
    @Operation(summary = "Get all courts", description = "Returns list of all courts")
    public ResponseEntity<List<CourtDTO>> getAllCourts() {
        return ResponseEntity.ok(courtService.getAllCourts());
    }

    @GetMapping("/available")
    @Operation(summary = "Get available courts by date", description = "Returns courts available for booking on a specific date")
    public ResponseEntity<List<CourtDTO>> getAvailableCourts() {
        return ResponseEntity.ok(courtService.getAvailableCourts());
    }
}