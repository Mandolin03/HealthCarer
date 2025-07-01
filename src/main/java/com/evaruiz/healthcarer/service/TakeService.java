package com.evaruiz.healthcarer.service;

import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.TakeDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.repository.TakeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class TakeService {

    private final TakeRepository takeRepository;


    public List<TakeDB> findTakesByUser(UserDB user) {
        return takeRepository.findAllByUser(user);
    }
    public Optional<TakeDB> findById(Long id) {
        return takeRepository.findById(id);
    }
    public void save(TakeDB take) {
        takeRepository.save(take);
    }
    public void deleteById(Long id) {
        takeRepository.deleteById(id);
    }
}
