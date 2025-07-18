package com.evaruiz.healthcarer.service;

import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.TakeDB;
import com.evaruiz.healthcarer.model.TreatmentDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.repository.MedicationRepository;
import com.evaruiz.healthcarer.repository.TakeRepository;
import com.evaruiz.healthcarer.repository.TreatmentRepository;
import com.evaruiz.healthcarer.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
@Profile({"dev", "test"})
public class DatabaseInitializer{

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final MedicationRepository medicationRepository;
    private final TreatmentRepository treatmentRepository;
    private final TakeRepository takeRepository;


    @PostConstruct
    public void initDatabase() {
        if (userRepository.count() == 0) {

            UserDB userAlice = new UserDB();
            userAlice.setName("Alice Smith");
            userAlice.setEmail("alice@example.com");
            userAlice.setEncodedPassword(passwordEncoder.encode("password"));
            userAlice.setRole("ADMIN");


            UserDB userBob = new UserDB();
            userBob.setName("Bob Johnson");
            userBob.setEmail("bob@example.com");
            userBob.setEncodedPassword(passwordEncoder.encode("password"));
            userBob.setRole("USER");

            userRepository.save(userAlice);
            userRepository.save(userBob);

            MedicationDB paracetamol = new MedicationDB();
            paracetamol.setName("Paracetamol");
            paracetamol.setStock(100.0f);
            paracetamol.setInstructions("Take 2 pills every 6 hours with food.");
            paracetamol.setDose(500.0f);
            paracetamol.setImagePath("images/paracetamol.png");
            paracetamol.setUser(userAlice);


            MedicationDB ibuprofen = new MedicationDB();
            ibuprofen.setName("Ibuprofen");
            ibuprofen.setStock(50.0f);
            ibuprofen.setInstructions("Take 1 pill every 8 hours after meals.");
            ibuprofen.setDose(200.0f);
            ibuprofen.setImagePath("images/ibuprofen.png");
            ibuprofen.setUser(userAlice);


            MedicationDB amoxicillin = new MedicationDB();
            amoxicillin.setName("Amoxicillin");
            amoxicillin.setStock(30.0f);
            amoxicillin.setInstructions("Take 1 capsule every 12 hours for 7 days.");
            amoxicillin.setDose(250.0f);
            amoxicillin.setImagePath("images/amoxicillin.png");
            amoxicillin.setUser(userBob);

            medicationRepository.save(paracetamol);
            medicationRepository.save(ibuprofen);
            medicationRepository.save(amoxicillin);

            TreatmentDB headacheRelief = new TreatmentDB();
            headacheRelief.setName("Headache Relief");
            headacheRelief.setStartDate(LocalDateTime.of(2023, 1, 10, 0, 0));
            headacheRelief.setLastTakenDate(LocalDateTime.of(2025, 7, 17, 14, 0));
            headacheRelief.setDispensingFrequency(6.0f);
            headacheRelief.setUser(userAlice);
            headacheRelief.setMedications(Arrays.asList(paracetamol, ibuprofen));


            TreatmentDB antibioticCourse = new TreatmentDB();
            antibioticCourse.setName("Antibiotic Course");
            antibioticCourse.setStartDate(LocalDateTime.of(2025, 7, 15, 0, 0));
            antibioticCourse.setEndDate(LocalDateTime.of(2025, 7, 22, 0, 0));
            antibioticCourse.setLastTakenDate(LocalDateTime.of(2025, 7, 16, 20, 0));
            antibioticCourse.setDispensingFrequency(12.0f);
            antibioticCourse.setUser(userBob);
            antibioticCourse.setMedications(List.of(amoxicillin));

            treatmentRepository.save(headacheRelief);
            treatmentRepository.save(antibioticCourse);

            TakeDB take1 = new TakeDB();
            take1.setDate(LocalDateTime.of(2025, 7, 17, 10, 30));
            take1.setUser(userAlice);
            take1.setMedications(List.of(paracetamol));


            TakeDB take2 = new TakeDB();
            take2.setDate(LocalDateTime.of(2025, 7, 17, 14, 0));
            take2.setUser(userAlice);
            take2.setMedications(Arrays.asList(paracetamol, ibuprofen));


            TakeDB take3 = new TakeDB();
            take3.setDate(LocalDateTime.of(2025, 7, 16, 20, 0));
            take3.setUser(userBob);
            take3.setMedications(List.of(amoxicillin));

            takeRepository.save(take1);
            takeRepository.save(take2);
            takeRepository.save(take3);


        }
    }
}
