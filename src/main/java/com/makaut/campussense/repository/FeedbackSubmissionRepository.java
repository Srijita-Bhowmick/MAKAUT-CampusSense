package com.makaut.campussense.repository;

import com.makaut.campussense.model.FeedbackSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackSubmissionRepository extends JpaRepository<FeedbackSubmission, Long> {

    List<FeedbackSubmission> findByCategory(String category);

    List<FeedbackSubmission> findByUserIdentifierOrderByCreatedAtDesc(String userIdentifier);
}
