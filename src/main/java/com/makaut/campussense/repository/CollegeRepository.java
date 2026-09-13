package com.makaut.campussense.repository;

import com.makaut.campussense.model.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    Optional<College> findByCode(String code);

    Optional<College> findByNameContainingIgnoreCase(String name);
}
