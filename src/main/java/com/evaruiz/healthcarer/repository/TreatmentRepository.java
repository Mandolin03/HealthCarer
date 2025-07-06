package com.evaruiz.healthcarer.repository;

import com.evaruiz.healthcarer.model.TreatmentDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentRepository extends JpaRepository<TreatmentDB, Long> {
    @Query("SELECT t FROM TreatmentDB t WHERE t.user.id = ?1")
    List<TreatmentDB> findAllByUserId(Long id);


}
