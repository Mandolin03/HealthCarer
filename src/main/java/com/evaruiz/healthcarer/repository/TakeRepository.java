package com.evaruiz.healthcarer.repository;

import com.evaruiz.healthcarer.model.TakeDB;
import com.evaruiz.healthcarer.model.UserDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TakeRepository extends JpaRepository<TakeDB, Long> {
    List<TakeDB> findAllByUser(UserDB user);
}
