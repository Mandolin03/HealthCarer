package com.evaruiz.healthcarer.repository;

import com.evaruiz.healthcarer.model.TakeDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TakeRepository extends JpaRepository<TakeDB, Long> {
    @Query("SELECT t FROM TakeDB t WHERE t.user.id = ?1")
    List<TakeDB> findAllByUserId(Long id);
}
