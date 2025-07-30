package com.evaruiz.healthcarer.service;

import com.evaruiz.healthcarer.model.TreatmentDB;
import com.evaruiz.healthcarer.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;

    public List<TreatmentDB> findTreatmentsByUserId(Long id) {
        return treatmentRepository.findAllByUserId(id);
    }
    public Optional<TreatmentDB> findById(Long id) {
        return treatmentRepository.findById(id);
    }
    public TreatmentDB save(TreatmentDB take) {
        return treatmentRepository.save(take);
    }
    public void deleteById(Long id) {
        treatmentRepository.deleteById(id);
    }

}
