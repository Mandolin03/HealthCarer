package com.evaruiz.healthcarer.service;


import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedicationService {


    private final MedicationRepository medicationRepository;


    public List<MedicationDB> findMedicationsByUser(UserDB user) {
        return medicationRepository.findByUser(user);
    }

    public Optional<MedicationDB> findById(Long id) {
        return medicationRepository.findById(id);
    }

    public void saveMedication(MedicationDB medication) {
        medicationRepository.save(medication);
    }

    @Transactional
    public void deleteMedication(Long id) {
        medicationRepository.deleteById(id);
    }
}
