package com.evaruiz.healthcarer.integration;

import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.TreatmentDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.repository.MedicationRepository;
import com.evaruiz.healthcarer.repository.TreatmentRepository;
import com.evaruiz.healthcarer.repository.UserRepository;
import com.evaruiz.healthcarer.service.TreatmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TreatmentServiceIntegrationTest {

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private TreatmentRepository treatmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MedicationRepository medicationRepository;

    private UserDB testUser1;
    private UserDB testUser2;
    private MedicationDB medicationA;
    private MedicationDB medicationB;
    private TreatmentDB treatment1;
    private TreatmentDB treatment2;

    @BeforeEach
    void setUp() {
        treatmentRepository.deleteAll();
        medicationRepository.deleteAll();
        userRepository.deleteAll();

        testUser1 = new UserDB(null, "Treat User 1", "treatuser1@example.com", "pass1", "USER", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        testUser1 = userRepository.save(testUser1);

        testUser2 = new UserDB(null, "Treat User 2", "treatuser2@example.com", "pass2", "USER", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        testUser2 = userRepository.save(testUser2);

        medicationA = new MedicationDB(null, "Med A for Treat", 200.0f, "Take daily.", 600.0f, "medA.jpg", new ArrayList<>(), testUser1, new ArrayList<>());
        medicationA = medicationRepository.save(medicationA);

        medicationB = new MedicationDB(null, "Med B for Treat", 150.0f, "Take twice daily.", 300.0f, "medB.jpg", new ArrayList<>(), testUser1, new ArrayList<>());
        medicationB = medicationRepository.save(medicationB);

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        treatment1 = new TreatmentDB(null, "Daily Treatment", now.minusDays(7), now.plusDays(7), now.minusHours(2), 24.0f, new ArrayList<>(Collections.singletonList(medicationA)), testUser1);
        treatment1 = treatmentRepository.save(treatment1);

        treatment2 = new TreatmentDB(null, "Night Treatment", now.minusDays(3), now.plusDays(10), now.minusHours(6), 12.0f, new ArrayList<>(Arrays.asList(medicationB, medicationA)), testUser1);
        treatment2 = treatmentRepository.save(treatment2);

        TreatmentDB treatment3 = new TreatmentDB(null, "User2's Treatment", now.minusDays(1), now.plusDays(1), now.minusHours(1), 8.0f, new ArrayList<>(Collections.singletonList(medicationA)), testUser2);
        treatmentRepository.save(treatment3);
    }



    @Test
    @DisplayName("GET treatments by user ID")
    void treatmentsByUserId() {
        List<TreatmentDB> foundTreatments = treatmentService.findTreatmentsByUserId(testUser1.getId());

        assertNotNull(foundTreatments);
        assertEquals(2, foundTreatments.size());
        assertTrue(foundTreatments.stream().anyMatch(t -> t.getId().equals(treatment1.getId())));
        assertTrue(foundTreatments.stream().anyMatch(t -> t.getId().equals(treatment2.getId())));
        assertFalse(foundTreatments.stream().anyMatch(t -> t.getUser().getId().equals(testUser2.getId())));
    }

    @Test
    @DisplayName("GET empty list if no treatments for user ID")
    void emptyListIfNoTreatmentsForUserId() {
        UserDB userWithNoTreats = new UserDB(null, "No Treats User", "notreats@example.com", "pass", "USER", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        userWithNoTreats = userRepository.save(userWithNoTreats);

        List<TreatmentDB> foundTreatments = treatmentService.findTreatmentsByUserId(userWithNoTreats.getId());

        assertNotNull(foundTreatments);
        assertTrue(foundTreatments.isEmpty());
    }



    @Test
    @DisplayName("GET treatment by ID")
    void treatmentById() {
        Optional<TreatmentDB> foundTreatmentOptional = treatmentService.findById(treatment1.getId());

        assertTrue(foundTreatmentOptional.isPresent());
        TreatmentDB foundTreatment = foundTreatmentOptional.get();
        assertEquals(treatment1.getId(), foundTreatment.getId());
        assertEquals(treatment1.getName(), foundTreatment.getName());
        assertEquals(treatment1.getUser().getId(), foundTreatment.getUser().getId());
        assertEquals(treatment1.getMedications().size(), foundTreatment.getMedications().size());
        assertTrue(foundTreatment.getMedications().stream().anyMatch(m -> m.getId().equals(medicationA.getId())));
    }

    @Test
    @DisplayName("GET empty optional when treatment not found by ID")
    void emptyOptionalWhenTreatmentNotFoundById() {
        Optional<TreatmentDB> foundTreatmentOptional = treatmentService.findById(999L);

        assertFalse(foundTreatmentOptional.isPresent());
    }



    @Test
    @DisplayName("POST save a new treatment")
    void saveNewTreatment() {
        LocalDateTime newStartDate = LocalDateTime.now().plusWeeks(1).truncatedTo(ChronoUnit.SECONDS);
        MedicationDB newMedForTreat = new MedicationDB(null, "New Treat Med", 75.0f, "After food", 100.0f, "new_treat_med.jpg", new ArrayList<>(), testUser1, new ArrayList<>());
        newMedForTreat = medicationRepository.save(newMedForTreat);

        TreatmentDB newTreatment = new TreatmentDB(null, "New Saved Treatment", newStartDate, newStartDate.plusDays(10), newStartDate.minusHours(1), 6.0f, new ArrayList<>(List.of(newMedForTreat)), testUser1);

        treatmentService.save(newTreatment);

        assertNotNull(newTreatment.getId(), "New treatment should have an ID after being saved");
        Optional<TreatmentDB> foundInDb = treatmentRepository.findById(newTreatment.getId());
        assertTrue(foundInDb.isPresent());
        assertEquals("New Saved Treatment", foundInDb.get().getName());
        assertEquals(newStartDate, foundInDb.get().getStartDate());
        assertEquals(testUser1.getId(), foundInDb.get().getUser().getId());
        assertEquals(1, foundInDb.get().getMedications().size());
        MedicationDB finalNewMedForTreat = newMedForTreat;
        assertTrue(foundInDb.get().getMedications().stream().anyMatch(m -> m.getId().equals(finalNewMedForTreat.getId())));
    }

    @Test
    @DisplayName("UPDATE an existing treatment")
    void updateExistingTreatment() {
        TreatmentDB treatmentToUpdate = treatmentRepository.findById(treatment1.getId()).get();
        String updatedName = "Updated Daily Treatment";
        LocalDateTime updatedEndDate = LocalDateTime.now().plusMonths(1).truncatedTo(ChronoUnit.SECONDS);
        treatmentToUpdate.setName(updatedName);
        treatmentToUpdate.setEndDate(updatedEndDate);
        treatmentToUpdate.setDispensingFrequency(8.0f);
        treatmentToUpdate.setMedications(new ArrayList<>(Collections.singletonList(medicationB)));

        treatmentService.save(treatmentToUpdate);

        Optional<TreatmentDB> foundInDb = treatmentRepository.findById(treatmentToUpdate.getId());
        assertTrue(foundInDb.isPresent());
        TreatmentDB actualUpdatedTreatment = foundInDb.get();
        assertEquals(treatmentToUpdate.getId(), actualUpdatedTreatment.getId());
        assertEquals(updatedName, actualUpdatedTreatment.getName());
        assertEquals(updatedEndDate, actualUpdatedTreatment.getEndDate());
        assertEquals(8.0f, actualUpdatedTreatment.getDispensingFrequency());
        assertEquals(1, actualUpdatedTreatment.getMedications().size());
        assertTrue(actualUpdatedTreatment.getMedications().stream().anyMatch(m -> m.getId().equals(medicationB.getId())));
        assertEquals(testUser1.getId(), actualUpdatedTreatment.getUser().getId());
    }



    @Test
    @DisplayName("DELETE a treatment by ID")
    void deleteTreatmentById() {
        Long idToDelete = treatment1.getId();

        treatmentService.deleteById(idToDelete);

        Optional<TreatmentDB> foundInDb = treatmentRepository.findById(idToDelete);
        assertFalse(foundInDb.isPresent(), "Treatment should be deleted from DB");
        assertEquals(2, treatmentRepository.count());
    }

    @Test
    @DisplayName("DELETE non-existent treatment")
    void deletingNonExistentTreatment() {
        assertDoesNotThrow(() -> treatmentService.deleteById(999L));
        assertEquals(3, treatmentRepository.count());
    }
}
