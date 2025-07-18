package com.evaruiz.healthcarer.integration;

import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.TakeDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.repository.MedicationRepository;
import com.evaruiz.healthcarer.repository.TakeRepository;
import com.evaruiz.healthcarer.repository.UserRepository;
import com.evaruiz.healthcarer.service.TakeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TakeServiceIntegrationTest {

    @Autowired
    private TakeService takeService;

    @Autowired
    private TakeRepository takeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MedicationRepository medicationRepository;


    private UserDB testUser1;
    private UserDB testUser2;
    private MedicationDB medication1;
    private MedicationDB medication2;
    private TakeDB take1;
    private TakeDB take2;

    @BeforeEach
    void setUp() {

        testUser1 = new UserDB(null, "Take User 1", "takeuser1@example.com", "pass1", "USER", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        testUser1 = userRepository.save(testUser1);

        testUser2 = new UserDB(null, "Take User 2", "takeuser2@example.com", "pass2", "USER", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        testUser2 = userRepository.save(testUser2);

        medication1 = new MedicationDB(null, "Paracetamol", 100.0f, "Take with food.", 500.0f, "img1.jpg", new ArrayList<>(), testUser1, new ArrayList<>());
        medication1 = medicationRepository.save(medication1);

        medication2 = new MedicationDB(null, "Ibuprofen", 50.0f, "Take every 8 hours.", 200.0f, "img2.jpg", new ArrayList<>(), testUser1, new ArrayList<>());
        medication2 = medicationRepository.save(medication2);

        LocalDateTime now = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

        take1 = new TakeDB(null, now.minusHours(1), Collections.singletonList(medication1), testUser1);
        take1 = takeRepository.save(take1);

        take2 = new TakeDB(null, now, Arrays.asList(medication2, medication1), testUser1);
        take2 = takeRepository.save(take2);

        TakeDB take3 = new TakeDB(null, now.plusHours(1), Collections.singletonList(medication1), testUser2);
        takeRepository.save(take3);
    }



    @Test
    @DisplayName("GET takes by user ID")
    void takesByUserId() {
        List<TakeDB> foundTakes = takeService.findTakesByUserId(testUser1.getId());

        assertNotNull(foundTakes);
        assertEquals(2, foundTakes.size());
        assertTrue(foundTakes.stream().anyMatch(t -> t.getId().equals(take1.getId())));
        assertTrue(foundTakes.stream().anyMatch(t -> t.getId().equals(take2.getId())));
        assertFalse(foundTakes.stream().anyMatch(t -> t.getUser().getId().equals(testUser2.getId())));
    }

    @Test
    @DisplayName("GET empty list if no takes for user ID")
    void emptyListIfNoTakesForUserId() {
        UserDB userWithNoTakes = new UserDB(null, "No Takes User", "notakes@example.com", "pass", "USER", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        userWithNoTakes = userRepository.save(userWithNoTakes);

        List<TakeDB> foundTakes = takeService.findTakesByUserId(userWithNoTakes.getId());

        assertNotNull(foundTakes);
        assertTrue(foundTakes.isEmpty());
    }


    @Test
    @DisplayName("GET take by ID")
    void takeById() {
        Optional<TakeDB> foundTakeOptional = takeService.findById(take1.getId());

        assertTrue(foundTakeOptional.isPresent());
        TakeDB foundTake = foundTakeOptional.get();
        assertEquals(take1.getId(), foundTake.getId());
        assertEquals(take1.getDate(), foundTake.getDate());
        assertEquals(take1.getUser().getId(), foundTake.getUser().getId());
        assertEquals(take1.getMedications().size(), foundTake.getMedications().size());
        assertTrue(foundTake.getMedications().stream().anyMatch(m -> m.getId().equals(medication1.getId())));
    }

    @Test
    @DisplayName("GET empty optional when take not found by ID")
    void takeNotFoundById() {
        Optional<TakeDB> foundTakeOptional = takeService.findById(999L);

        assertFalse(foundTakeOptional.isPresent());
    }



    @Test
    @DisplayName("POST save a new take")
    void saveNewTake() {
        LocalDateTime newTakeTime = LocalDateTime.now().plusDays(1).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        MedicationDB newMed = new MedicationDB(null, "New Saved Med", 10.0f, "instr", 10f, "url", new ArrayList<>(), testUser1, new ArrayList<>());
        newMed = medicationRepository.save(newMed);

        TakeDB newTake = new TakeDB(null, newTakeTime, List.of(newMed), testUser1);

        takeService.save(newTake);

        assertNotNull(newTake.getId(), "New take should have an ID after being saved");
        Optional<TakeDB> foundInDb = takeRepository.findById(newTake.getId());
        assertTrue(foundInDb.isPresent());
        assertEquals(newTakeTime, foundInDb.get().getDate());
        assertEquals(testUser1.getId(), foundInDb.get().getUser().getId());
        assertEquals(1, foundInDb.get().getMedications().size());
        MedicationDB finalNewMed = newMed;
        assertTrue(foundInDb.get().getMedications().stream().anyMatch(m -> m.getId().equals(finalNewMed.getId())));
    }

    @Test
    @DisplayName("UPDATE an existing take")
    void updateExistingTake() {
        TakeDB takeToUpdate = takeRepository.findById(take1.getId()).get();
        LocalDateTime updatedDate = LocalDateTime.now().plusDays(2).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        takeToUpdate.setDate(updatedDate);
        takeToUpdate.setMedications(new ArrayList<>(Collections.singletonList(medication2)));

        takeService.save(takeToUpdate);

        Optional<TakeDB> foundInDb = takeRepository.findById(takeToUpdate.getId());
        assertTrue(foundInDb.isPresent());
        TakeDB actualUpdatedTake = foundInDb.get();
        assertEquals(takeToUpdate.getId(), actualUpdatedTake.getId());
        assertEquals(updatedDate, actualUpdatedTake.getDate());
        assertEquals(1, actualUpdatedTake.getMedications().size());
        assertTrue(actualUpdatedTake.getMedications().stream().anyMatch(m -> m.getId().equals(medication2.getId())));
        assertEquals(testUser1.getId(), actualUpdatedTake.getUser().getId());
    }


    @Test
    @DisplayName("DELETE a take by ID")
    void deleteTakeById() {
        Long idToDelete = take1.getId();

        takeService.deleteById(idToDelete);

        Optional<TakeDB> foundInDb = takeRepository.findById(idToDelete);
        assertFalse(foundInDb.isPresent(), "Take should be deleted from DB");
    }

    @Test
    @DisplayName("DELETE non-existent take")
    void deletingNonExistentTake() {
        assertDoesNotThrow(() -> takeService.deleteById(999L));
    }
}
