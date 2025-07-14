package com.evaruiz.healthcarer.unitary;

import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.TreatmentDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.repository.TreatmentRepository;
import com.evaruiz.healthcarer.service.TreatmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceTest {

    @Mock
    private TreatmentRepository treatmentRepository;

    @InjectMocks
    private TreatmentService treatmentService;

    private UserDB testUser;
    private MedicationDB testMedication1;
    private MedicationDB testMedication2;

    @BeforeEach
    void setUp() {
        testUser = new UserDB(1L, "Test User 1", "user1@example.com", "encodedPass1", "USER", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        testMedication1 = new MedicationDB(101L, "Paracetamol", 100.0f, "Take with food.", 500.0f, "src/main/resources/static/images/Noticia1.jpg", new ArrayList<>(), testUser, new ArrayList<>());
        testMedication2 = new MedicationDB(102L, "Ibuprofen", 50.0f, "Take every 8 hours.", 200.0f, "src/main/resources/static/images/Producto1.jpg", new ArrayList<>(), testUser, new ArrayList<>());
    }

    @Test
    @DisplayName("GET treatments by user ID")
    void treatmentsByUserId() {
        Long userId = testUser.getId();
        LocalDateTime now = LocalDateTime.now();

        TreatmentDB treatment1 = new TreatmentDB(1L, "Treatment A", now.minusDays(5), now.plusDays(5), now.minusDays(1), 24.0f, Collections.singletonList(testMedication1), testUser);
        TreatmentDB treatment2 = new TreatmentDB(2L, "Treatment B", now.minusDays(10), now.plusDays(10), now.minusDays(2), 48.0f, Collections.singletonList(testMedication2), testUser);
        List<TreatmentDB> expectedTreatments = Arrays.asList(treatment1, treatment2);

        when(treatmentRepository.findAllByUserId(userId)).thenReturn(expectedTreatments);

        List<TreatmentDB> actualTreatments = treatmentService.findTreatmentsByUserId(userId);

        assertNotNull(actualTreatments);
        assertEquals(2, actualTreatments.size());
        assertEquals(expectedTreatments, actualTreatments);
        verify(treatmentRepository, times(1)).findAllByUserId(userId);
        verifyNoMoreInteractions(treatmentRepository);
    }

    @Test
    @DisplayName("GET treatment by ID")
    void treatmentById() {
        Long treatmentId = 1L;
        LocalDateTime now = LocalDateTime.now();
        TreatmentDB expectedTreatment = new TreatmentDB(treatmentId, "Treatment X", now.minusDays(7), now.plusDays(7), now.minusDays(2), 12.0f, Arrays.asList(testMedication1, testMedication2), testUser);

        when(treatmentRepository.findById(treatmentId)).thenReturn(Optional.of(expectedTreatment));

        Optional<TreatmentDB> actualTreatmentOptional = treatmentService.findById(treatmentId);

        assertTrue(actualTreatmentOptional.isPresent());
        assertEquals(expectedTreatment, actualTreatmentOptional.get());
        verify(treatmentRepository, times(1)).findById(treatmentId);
        verifyNoMoreInteractions(treatmentRepository);
    }

    @Test
    @DisplayName("GET empty optional when treatment not found by ID")
    void emptyOptional() {
        Long nonExistentId = 99L;

        when(treatmentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<TreatmentDB> actualTreatmentOptional = treatmentService.findById(nonExistentId);

        assertFalse(actualTreatmentOptional.isPresent());
        assertTrue(actualTreatmentOptional.isEmpty());
        verify(treatmentRepository, times(1)).findById(nonExistentId);
        verifyNoMoreInteractions(treatmentRepository);
    }

    @Test
    @DisplayName("POST save a treatment")
    void saveTreatment() {
        LocalDateTime now = LocalDateTime.now();
        TreatmentDB treatmentToSave = new TreatmentDB(null, "New Treatment", now, now.plusDays(30), now.minusHours(5), 6.0f, Collections.singletonList(testMedication1), testUser);
        TreatmentDB savedTreatment = new TreatmentDB(3L, "New Treatment", now, now.plusDays(30), now.minusHours(5), 6.0f, Collections.singletonList(testMedication1), testUser);

        when(treatmentRepository.save(any(TreatmentDB.class))).thenReturn(savedTreatment);

        treatmentService.save(treatmentToSave);

        verify(treatmentRepository, times(1)).save(treatmentToSave);
        verifyNoMoreInteractions(treatmentRepository);
    }

    @Test
    @DisplayName("DELETE a treatment by ID")
    void deleteTreatmentById() {
        Long treatmentIdToDelete = 4L;

        treatmentService.deleteById(treatmentIdToDelete);

        verify(treatmentRepository, times(1)).deleteById(treatmentIdToDelete);
        verifyNoMoreInteractions(treatmentRepository);
    }
}
