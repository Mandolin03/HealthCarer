package com.evaruiz.healthcarer.repository;

import com.evaruiz.healthcarer.model.TreatmentDB;
import com.evaruiz.healthcarer.model.UserDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentRepository extends JpaRepository<TreatmentDB, Long> {
    List<TreatmentDB> findAllByUser(UserDB user);


}
