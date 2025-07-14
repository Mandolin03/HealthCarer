package com.evaruiz.healthcarer.unitary;

import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.TakeDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.repository.TakeRepository;
import com.evaruiz.healthcarer.service.TakeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TakeServiceTest {

    @Mock
    private TakeRepository takeRepository;

    @InjectMocks
    private TakeService takeService;


    private MedicationDB medication1;
    private MedicationDB medication2;
    private UserDB user1;


    @BeforeEach
    void setUp() {

        user1 = new UserDB(1L, "Test User 1", "user1@example.com", "encodedPass1", "USER", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        medication1 = new MedicationDB(101L, "Paracetamol", 100.0f, "Take with food.", 500.0f, "src/main/resources/static/images/Noticia1.jpg", new ArrayList<>(), user1, new ArrayList<>());
        medication2 = new MedicationDB(102L, "Ibuprofen", 50.0f, "Take every 8 hours.", 200.0f, "src/main/resources/static/images/Producto1.jpg", new ArrayList<>(), user1, new ArrayList<>());
        user1.getMedications().add(medication1);
        user1.getMedications().add(medication2);
    }

    @Test
    @DisplayName("GET takes by user ID")
    void takesByUserId() {
        Long userId = user1.getId();
        LocalDateTime now = LocalDateTime.now();

        TakeDB take1 = new TakeDB(1L, now, Collections.singletonList(medication1), user1);
        TakeDB take2 = new TakeDB(2L, now.plusHours(1), Arrays.asList(medication2, medication1), user1);
        List<TakeDB> expectedTakes = Arrays.asList(take1, take2);

        when(takeRepository.findAllByUserId(userId)).thenReturn(expectedTakes);

        List<TakeDB> actualTakes = takeService.findTakesByUserId(userId);

        assertNotNull(actualTakes);
        assertEquals(2, actualTakes.size());
        assertEquals(expectedTakes, actualTakes);
        verify(takeRepository, times(1)).findAllByUserId(userId);
        verifyNoMoreInteractions(takeRepository);
    }

    @Test
    @DisplayName("GET take by ID")
    void takeById() {
        Long takeId = 1L;
        LocalDateTime now = LocalDateTime.now();
        TakeDB expectedTake = new TakeDB(takeId, now, Collections.singletonList(medication1), user1);

        when(takeRepository.findById(takeId)).thenReturn(Optional.of(expectedTake));

        Optional<TakeDB> actualTakeOptional = takeService.findById(takeId);

        assertTrue(actualTakeOptional.isPresent());
        assertEquals(expectedTake, actualTakeOptional.get());
        verify(takeRepository, times(1)).findById(takeId);
        verifyNoMoreInteractions(takeRepository);
    }

    @Test
    @DisplayName("GET empty optional when take not found by ID")
    void emptyOptional() {
        Long nonExistentId = 99L;

        when(takeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<TakeDB> actualTakeOptional = takeService.findById(nonExistentId);

        assertFalse(actualTakeOptional.isPresent());
        assertTrue(actualTakeOptional.isEmpty());
        verify(takeRepository, times(1)).findById(nonExistentId);
        verifyNoMoreInteractions(takeRepository);
    }

    @Test
    @DisplayName("POST save a take")
    void saveTake() {
        LocalDateTime now = LocalDateTime.now();
        TakeDB takeToSave = new TakeDB(null, now, Collections.singletonList(medication1), user1);
        TakeDB savedTake = new TakeDB(3L, now, Collections.singletonList(medication1), user1);

        when(takeRepository.save(any(TakeDB.class))).thenReturn(savedTake);

        takeService.save(takeToSave);

        verify(takeRepository, times(1)).save(takeToSave);
        verifyNoMoreInteractions(takeRepository);
    }

    @Test
    @DisplayName("DELETE a take by ID")
    void deleteTakeById() {
        Long takeIdToDelete = 4L;

        takeService.deleteById(takeIdToDelete);

        verify(takeRepository, times(1)).deleteById(takeIdToDelete);
        verifyNoMoreInteractions(takeRepository);
    }
}
