package com.makaut.campussense.repository;

import com.makaut.campussense.model.UserBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookmarkRepository extends JpaRepository<UserBookmark, Long> {

    List<UserBookmark> findByUserIdentifierOrderByCreatedAtDesc(String userIdentifier);

    Optional<UserBookmark> findByUserIdentifierAndQuestion_Id(String userIdentifier, Long questionId);

    Optional<UserBookmark> findByUserIdentifierAndResource_Id(String userIdentifier, Long resourceId);

    void deleteByUserIdentifierAndQuestion_Id(String userIdentifier, Long questionId);

    void deleteByUserIdentifierAndResource_Id(String userIdentifier, Long resourceId);

    long countByUserIdentifier(String userIdentifier);
}
