package com.evaruiz.healthcarer.integration;

import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.repository.MedicationRepository;
import com.evaruiz.healthcarer.repository.UserRepository;
import com.evaruiz.healthcarer.service.MedicationService;
import jakarta.persistence.EntityNotFoundException; // Import the specific exception
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MedicationServiceIntegrationTest {

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private MedicationRepository medicationRepository;
    @Autowired
    private UserRepository userRepository;


    private UserDB testUser1;
    private UserDB testUser2;
    private MedicationDB medication1;
    private MedicationDB medication2;
    private MedicationDB medication3;

    @BeforeEach
    void setUp() {

        testUser1 = new UserDB(null, "Med User 1", "meduser1@example.com", "pass1", "USER", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        testUser1 = userRepository.save(testUser1);

        testUser2 = new UserDB(null, "Med User 2", "meduser2@example.com", "pass2", "USER", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        testUser2 = userRepository.save(testUser2);


        medication1 = new MedicationDB(null, "Paracetamol", 100.0f, "Take with food.", 500.0f, "img1.jpg", new ArrayList<>(), testUser1, new ArrayList<>());
        medication1 = medicationRepository.save(medication1);

        medication2 = new MedicationDB(null, "Ibuprofen", 50.0f, "Take every 8 hours.", 200.0f, "img2.jpg", new ArrayList<>(), testUser1, new ArrayList<>());
        medication2 = medicationRepository.save(medication2);

        medication3 = new MedicationDB(null, "Aspirin", 75.0f, "Daily.", 100.0f, "img3.jpg", new ArrayList<>(), testUser2, new ArrayList<>());
        medication3 = medicationRepository.save(medication3);
    }

    @Test
    @DisplayName("GET medications by user ID")
    void findMedications() {
        List<MedicationDB> foundMedications = medicationService.findMedicationsByUserId(testUser1.getId());

        assertNotNull(foundMedications);
        assertEquals(2, foundMedications.size());
        assertTrue(foundMedications.stream().anyMatch(m -> m.getName().equals("Paracetamol")));
        assertTrue(foundMedications.stream().anyMatch(m -> m.getName().equals("Ibuprofen")));
        assertFalse(foundMedications.stream().anyMatch(m -> m.getName().equals("Aspirin")));
    }

    @Test
    @DisplayName("GET empty list when user has no medications")
    void emptyListMedications() {
        UserDB userWithNoMeds = new UserDB(null, "No Meds User", "nomeds@example.com", "pass", "USER", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        userWithNoMeds = userRepository.save(userWithNoMeds);

        List<MedicationDB> foundMedications = medicationService.findMedicationsByUserId(userWithNoMeds.getId());

        assertNotNull(foundMedications);
        assertTrue(foundMedications.isEmpty());
    }

    @Test
    @DisplayName("GET medication by ID")
    void medicationById() {
        Optional<MedicationDB> foundMedicationOptional = medicationService.findById(medication1.getId());

        assertTrue(foundMedicationOptional.isPresent());
        assertEquals(medication1.getName(), foundMedicationOptional.get().getName());
        assertEquals(medication1.getDose(), foundMedicationOptional.get().getDose());
        assertEquals(medication1.getUser().getId(), foundMedicationOptional.get().getUser().getId());
    }

    @Test
    @DisplayName("GET empty optional when medication not found by ID")
    void emptyOptionalWhenMedicationNotFoundById() {
        Optional<MedicationDB> foundMedicationOptional = medicationService.findById(999L);

        assertFalse(foundMedicationOptional.isPresent());
    }

    @Test
    @DisplayName("POST save a new medication")
    void saveNewMedication() {
        MedicationDB newMedication = new MedicationDB(null, "New Test Med", 25.0f, "New instructions.", 100.0f, "new_img.jpg", new ArrayList<>(), testUser1, new ArrayList<>());

        medicationService.saveMedication(newMedication);

        assertNotNull(newMedication.getId(), "New medication should have an ID after being saved (if passed by reference)");
        Optional<MedicationDB> foundInDb = medicationRepository.findById(newMedication.getId());
        assertTrue(foundInDb.isPresent());
        assertEquals("New Test Med", foundInDb.get().getName());
        assertEquals(testUser1.getId(), foundInDb.get().getUser().getId());
    }

    @Test
    @DisplayName("UPDATE an existing medication")
    void updateExistingMedication() {
        MedicationDB medToUpdate = medicationRepository.findById(medication1.getId()).get();
        String updatedName = "Updated Paracetamol";
        medToUpdate.setName(updatedName);
        medToUpdate.setDose(125.0f);

        medicationService.saveMedication(medToUpdate);

        Optional<MedicationDB> foundInDb = medicationRepository.findById(medToUpdate.getId());
        assertTrue(foundInDb.isPresent());
        MedicationDB actualUpdatedMed = foundInDb.get();
        assertEquals(medToUpdate.getId(), actualUpdatedMed.getId());
        assertEquals(updatedName, actualUpdatedMed.getName());
        assertEquals(125.0f, actualUpdatedMed.getDose());
        assertEquals(testUser1.getId(), actualUpdatedMed.getUser().getId());
    }


    @Test
    @DisplayName("Remove medication from user but not delete it")
    void removeMedicationFromUser() {
        Long medicationIdToRemove = medication1.getId();
        Long userIdBefore = medication1.getUser().getId();
        assertEquals(userIdBefore, testUser1.getId(), "Medication should initially be linked to testUser1");

        medicationService.removeMedicationFromUser(medicationIdToRemove);

        Optional<MedicationDB> foundMedicationOptional = medicationRepository.findById(medicationIdToRemove);
        assertTrue(foundMedicationOptional.isPresent(), "Medication should still exist in the database");

        MedicationDB disassociatedMedication = foundMedicationOptional.get();
        assertNull(disassociatedMedication.getUser(), "Medication's user should be null after disassociation");

        List<MedicationDB> user1Medications = medicationService.findMedicationsByUserId(testUser1.getId());
        assertFalse(user1Medications.stream().anyMatch(m -> m.getId().equals(medicationIdToRemove)), "Disassociated medication should not appear for original user");
        assertEquals(1, user1Medications.size(), "Only medication2 should remain for testUser1");
    }

    @Test
    @DisplayName("EntityNotFoundException if medication not found when removing from user")
    void removingNonExistentMedication() {
        Long nonExistentId = 999L;
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                medicationService.removeMedicationFromUser(nonExistentId)
        );

        assertEquals("No existe la medicación con ID: " + nonExistentId, exception.getMessage());
    }
}
