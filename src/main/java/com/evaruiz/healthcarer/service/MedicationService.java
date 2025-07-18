package com.evaruiz.healthcarer.service;

import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.repository.MedicationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedicationService {


    private final MedicationRepository medicationRepository;


    public List<MedicationDB> findMedicationsByUserId(Long id) {
        return medicationRepository.findByUserId(id);
    }

    public Optional<MedicationDB> findById(Long id) {
        return medicationRepository.findById(id);
    }

    public MedicationDB saveMedication(MedicationDB medication) {
        medicationRepository.save(medication);
        return medication;
    }

    public void removeMedicationFromUser(Long id) {
        MedicationDB medication = medicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe la medicación con ID: " + id));

        medication.setUser(null);
        medicationRepository.save(medication);
    }
}
