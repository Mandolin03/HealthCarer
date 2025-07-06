package com.evaruiz.healthcarer.repository;

import com.evaruiz.healthcarer.model.MedicationDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface MedicationRepository extends JpaRepository<MedicationDB, Long> {

    @Query("SELECT m FROM MedicationDB m WHERE m.user.id = ?1")
    List<MedicationDB> findByUserId(Long userId);

}
