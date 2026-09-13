package com.makaut.campussense.repository;

import com.makaut.campussense.model.UserSyllabusProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSyllabusProgressRepository extends JpaRepository<UserSyllabusProgress, Long> {

    List<UserSyllabusProgress> findByUserIdentifier(String userIdentifier);

    Optional<UserSyllabusProgress> findByUserIdentifierAndSyllabusUnit_Id(String userIdentifier, Long syllabusUnitId);

    List<UserSyllabusProgress> findByUserIdentifierAndSyllabusUnit_CourseResource_SubjectCodeIgnoreCase(
            String userIdentifier, String subjectCode);

    long countByUserIdentifierAndCompletedTrue(String userIdentifier);
}
