package com.evaruiz.healthcarer.service;


import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.repository.MedicationRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MedicationService {


    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public List<MedicationDB> findMedicationsByUser(UserDB user) {
        return medicationRepository.findByUser(user);
    }


    public Optional<MedicationDB> findById(Long id) {
        return medicationRepository.findById(id);
    }

    public MedicationDB saveMedication(MedicationDB medication) {
        return medicationRepository.save(medication);
    }

    public void deleteMedication(Long id) {
        medicationRepository.deleteById(id);
    }
}
