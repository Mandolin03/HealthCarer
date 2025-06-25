package com.evaruiz.healthcarer.repository;

import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.UserDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;



@Repository
public interface MedicationRepository extends JpaRepository<MedicationDB, Long> {

    List<MedicationDB> findByUser(UserDB user);

}
