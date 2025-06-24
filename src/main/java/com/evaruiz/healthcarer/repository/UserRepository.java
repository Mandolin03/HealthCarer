package com.evaruiz.healthcarer.repository;

import com.evaruiz.healthcarer.model.UserDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserDB, Long> {
    Optional<UserDB> findByEmail(String email);
}
