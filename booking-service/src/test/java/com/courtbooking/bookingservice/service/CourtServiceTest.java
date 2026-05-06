package com.courtbooking.bookingservice.service;

import com.courtbooking.bookingservice.dto.CourtDTO;
import com.courtbooking.bookingservice.dto.CourtRequest;
import com.courtbooking.bookingservice.entity.Court;
import com.courtbooking.bookingservice.exception.ResourceNotFoundException;
import com.courtbooking.bookingservice.repository.CourtRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {

    @Mock
    private CourtRepository courtRepository;

    private CourtService courtService;

    @BeforeEach
    void setUp() {
        courtService = new CourtService(courtRepository);
    }

    @Test
    void shouldAddCourtSuccessfully() {
        CourtRequest request = new CourtRequest();
        request.setName("Court Alpha");
        request.setSportType("Tennis");
        request.setLocation("Location 1");
        request.setDescription("Test description");

        Court savedCourt = new Court();
        savedCourt.setId(1L);
        savedCourt.setName("Court Alpha");
        savedCourt.setSportType("Tennis");
        savedCourt.setLocation("Location 1");
        savedCourt.setAvailable(true);
        savedCourt.setDescription("Test description");

        when(courtRepository.save(any(Court.class))).thenReturn(savedCourt);

        CourtDTO result = courtService.addCourt(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Court Alpha", result.getName());
        assertEquals("Tennis", result.getSportType());
        assertTrue(result.getAvailable());
        verify(courtRepository, times(1)).save(any(Court.class));
    }

    @Test
    void shouldUpdateCourtSuccessfully() {
        CourtRequest request = new CourtRequest();
        request.setName("Updated Court");
        request.setSportType("Badminton");
        request.setLocation("New Location");
        request.setDescription("Updated description");

        Court existingCourt = new Court();
        existingCourt.setId(1L);
        existingCourt.setName("Court Alpha");
        existingCourt.setSportType("Tennis");
        existingCourt.setLocation("Location 1");
        existingCourt.setAvailable(true);

        when(courtRepository.findById(1L)).thenReturn(Optional.of(existingCourt));
        when(courtRepository.save(any(Court.class))).thenReturn(existingCourt);

        CourtDTO result = courtService.updateCourt(1L, request);

        assertNotNull(result);
        verify(courtRepository).findById(1L);
        verify(courtRepository).save(existingCourt);
    }

    @Test
    void shouldThrowExceptionWhenUpdateNonExistentCourt() {
        CourtRequest request = new CourtRequest();
        request.setName("Updated Court");

        when(courtRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courtService.updateCourt(999L, request));
    }

    @Test
    void shouldDeleteCourtSuccessfully() {
        when(courtRepository.existsById(1L)).thenReturn(true);
        doNothing().when(courtRepository).deleteById(1L);

        assertDoesNotThrow(() -> courtService.deleteCourt(1L));
        verify(courtRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeleteNonExistentCourt() {
        when(courtRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> courtService.deleteCourt(999L));
    }

    @Test
    void shouldGetCourtByIdSuccessfully() {
        Court court = new Court();
        court.setId(1L);
        court.setName("Court Alpha");
        court.setSportType("Tennis");
        court.setLocation("Location 1");
        court.setAvailable(true);
        court.setDescription("Test description");

        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));

        CourtDTO result = courtService.getCourtById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Court Alpha", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenGetCourtByIdNotFound() {
        when(courtRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courtService.getCourtById(999L));
    }

    @Test
    void shouldGetAllCourts() {
        Court court1 = new Court();
        court1.setId(1L);
        court1.setName("Court Alpha");
        court1.setSportType("Tennis");
        court1.setLocation("Location 1");
        court1.setAvailable(true);

        Court court2 = new Court();
        court2.setId(2L);
        court2.setName("Court Beta");
        court2.setSportType("Badminton");
        court2.setLocation("Location 2");
        court2.setAvailable(false);

        when(courtRepository.findAll()).thenReturn(List.of(court1, court2));

        List<CourtDTO> result = courtService.getAllCourts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Court Alpha", result.get(0).getName());
        assertEquals("Court Beta", result.get(1).getName());
    }

    @Test
    void shouldGetAllCourtsEmptyList() {
        when(courtRepository.findAll()).thenReturn(List.of());

        List<CourtDTO> result = courtService.getAllCourts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetAvailableCourts() {
        Court availableCourt = new Court();
        availableCourt.setId(1L);
        availableCourt.setName("Court Alpha");
        availableCourt.setSportType("Tennis");
        availableCourt.setLocation("Location 1");
        availableCourt.setAvailable(true);

        Court unavailableCourt = new Court();
        unavailableCourt.setId(2L);
        unavailableCourt.setName("Court Beta");
        unavailableCourt.setSportType("Badminton");
        unavailableCourt.setLocation("Location 2");
        unavailableCourt.setAvailable(false);

        when(courtRepository.findAll()).thenReturn(List.of(availableCourt, unavailableCourt));

        List<CourtDTO> result = courtService.getAvailableCourts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Court Alpha", result.get(0).getName());
        assertTrue(result.get(0).getAvailable());
    }

    @Test
    void shouldGetAvailableCourtsEmptyList() {
        when(courtRepository.findAll()).thenReturn(List.of());

        List<CourtDTO> result = courtService.getAvailableCourts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}