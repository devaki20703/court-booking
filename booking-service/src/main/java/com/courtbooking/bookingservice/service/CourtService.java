package com.courtbooking.bookingservice.service;

import com.courtbooking.bookingservice.dto.CourtDTO;
import com.courtbooking.bookingservice.dto.CourtRequest;
import com.courtbooking.bookingservice.entity.Court;
import com.courtbooking.bookingservice.exception.ResourceNotFoundException;
import com.courtbooking.bookingservice.repository.CourtRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourtService {

    private final CourtRepository courtRepository;

    public CourtService(CourtRepository courtRepository) {
        this.courtRepository = courtRepository;
    }

    @Transactional
    public CourtDTO addCourt(CourtRequest request) {
        Court court = new Court(
            request.getName(),
            request.getSportType(),
            request.getLocation(),
            true,
            request.getDescription()
        );
        court.setPricePerHour(request.getPricePerHour());

        court = courtRepository.save(court);
        return mapToDTO(court);
    }

    @Transactional
    public CourtDTO updateCourt(Long id, CourtRequest request) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + id));

        court.setName(request.getName());
        court.setSportType(request.getSportType());
        court.setLocation(request.getLocation());
        court.setDescription(request.getDescription());
        if (request.getPricePerHour() != null) {
            court.setPricePerHour(request.getPricePerHour());
        }

        court = courtRepository.save(court);
        return mapToDTO(court);
    }

    @Transactional
    public void deleteCourt(Long id) {
        if (!courtRepository.existsById(id)) {
            throw new ResourceNotFoundException("Court not found with id: " + id);
        }
        courtRepository.deleteById(id);
    }

    public CourtDTO getCourtById(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + id));
        return mapToDTO(court);
    }

    public List<CourtDTO> getAllCourts() {
        return courtRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<CourtDTO> getAvailableCourts() {
        return courtRepository.findAll().stream()
                .filter(c -> c.getAvailable())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CourtDTO mapToDTO(Court court) {
        CourtDTO dto = new CourtDTO();
        dto.setId(court.getId());
        dto.setName(court.getName());
        dto.setSportType(court.getSportType());
        dto.setLocation(court.getLocation());
        dto.setAvailable(court.getAvailable());
        dto.setDescription(court.getDescription());
        dto.setPricePerHour(court.getPricePerHour());
        return dto;
    }
}