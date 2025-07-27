package com.evaruiz.healthcarer.unitary;

import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.repository.MedicationRepository;
import com.evaruiz.healthcarer.service.MedicationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private MedicationService medicationService;


    private MedicationDB medication1;
    private MedicationDB medication2;
    private UserDB user1;
    private UserDB user2;

    @BeforeEach
    void setUp() {

        user1 = new UserDB(1L, "Test User 1", "user1@example.com", "encodedPass1", "USER", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        user2 = new UserDB(2L, "Test User 2", "user2@example.com", "encodedPass2", "USER", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());


        medication1 = new MedicationDB(101L, "Paracetamol", 100.0f, "Take with food.", 500.0f, "src/main/resources/static/images/Noticia1.jpg", new ArrayList<>(), user1, new ArrayList<>());
        medication2 = new MedicationDB(102L, "Ibuprofen", 50.0f, "Take every 8 hours.", 200.0f, "src/main/resources/static/images/Producto1.jpg", new ArrayList<>(), user1, new ArrayList<>());
    }

    @Test
    @DisplayName("GET a list of medications from a user's ID when medications exist")
    void medicationsByUserId() {


        Long userId = user1.getId();
        List<MedicationDB> expectedMedications = Arrays.asList(medication1, medication2);
        when(medicationRepository.findByUserId(userId)).thenReturn(expectedMedications);


        List<MedicationDB> actualMedications = medicationService.findMedicationsByUserId(userId);


        assertNotNull(actualMedications);
        assertEquals(2, actualMedications.size());
        assertTrue(actualMedications.containsAll(expectedMedications));
        verify(medicationRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("GET an empty list of medications from a user's ID when no medications exist")
    void emptyMedicationsByUserId() {

        Long userId = user2.getId();
        when(medicationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());


        List<MedicationDB> actualMedications = medicationService.findMedicationsByUserId(userId);


        assertNotNull(actualMedications);
        assertTrue(actualMedications.isEmpty());
        verify(medicationRepository, times(1)).findByUserId(userId);
    }


    @Test
    @DisplayName("GET a medication given its ID when medication exists")
    void medication() {

        Long medicationId = medication1.getId();
        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(medication1));


        Optional<MedicationDB> foundMedication = medicationService.findById(medicationId);


        assertTrue(foundMedication.isPresent());
        assertEquals(medication1.getName(), foundMedication.get().getName());
        verify(medicationRepository, times(1)).findById(medicationId);
    }

    @Test
    @DisplayName("GET an empty Optional when medication does not exist")
    void emptyOptional() {

        Long nonExistentId = 999L;
        when(medicationRepository.findById(nonExistentId)).thenReturn(Optional.empty());


        Optional<MedicationDB> foundMedication = medicationService.findById(nonExistentId);


        assertFalse(foundMedication.isPresent());
        verify(medicationRepository, times(1)).findById(nonExistentId);
    }


    @Test
    @DisplayName("POST a new medication and save it")
    void newMedication() {

        MedicationDB newMedication = new MedicationDB(null, "Aspirin", 75.0f, "Take after meal.", 100.0f, null, new ArrayList<>(), user1, new ArrayList<>());
        when(medicationRepository.save(any(MedicationDB.class))).thenReturn(newMedication);

        medicationService.saveMedication(newMedication);

        verify(medicationRepository, times(1)).save(newMedication);
    }

    @Test
    @DisplayName("POST an update of an existing medication and save it")
    void updateMedication() {
        medication1.setStock(90.0f); // Simulate a change
        when(medicationRepository.save(any(MedicationDB.class))).thenReturn(medication1);

        medicationService.saveMedication(medication1);

        verify(medicationRepository, times(1)).save(medication1);
    }

    @Test
    @DisplayName("Remove user association from medication")
    void removeMedicationFromUser() {
        Long medicationId = medication1.getId();
        MedicationDB medicationToModify = new MedicationDB(medication1.getId(), medication1.getName(), medication1.getStock(),
                medication1.getInstructions(), medication1.getDose(), medication1.getImagePath(),
                new ArrayList<>(medication1.getTreatments()), user1, new ArrayList<>(medication1.getTakes()));

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(medicationToModify));
        when(medicationRepository.save(any(MedicationDB.class))).thenReturn(medicationToModify);

        medicationService.removeMedicationFromUser(medicationId);

        assertNull(medicationToModify.getUser(), "Medication's user should be set to null");
        verify(medicationRepository, times(1)).findById(medicationId);
        verify(medicationRepository, times(1)).save(medicationToModify);
    }

    @Test
    @DisplayName("DELETE a medication that does not exist")
    void deleteMedicationThatDoesNotExist() {
        Long nonExistentId = 999L;
        when(medicationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> medicationService.removeMedicationFromUser(nonExistentId));

        assertEquals("No existe la medicación con ID: " + nonExistentId, thrown.getMessage());
        verify(medicationRepository, times(1)).findById(nonExistentId);
        verify(medicationRepository, never()).save(any(MedicationDB.class));
    }
}
