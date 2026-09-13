package com.makaut.campussense.repository;

import com.makaut.campussense.model.PlacementRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PlacementRecordRepository extends JpaRepository<PlacementRecord, Long> {

    @Query("SELECT p FROM PlacementRecord p WHERE " +
           "(:collegeId IS NULL OR p.college.id = :collegeId) AND " +
           "(:batchYear IS NULL OR p.batchYear = :batchYear) " +
           "ORDER BY p.batchYear DESC, p.highestCtc DESC")
    List<PlacementRecord> findByCollegeAndBatchYear(
            @Param("collegeId") Long collegeId,
            @Param("batchYear") Integer batchYear
    );

    @Query("SELECT p FROM PlacementRecord p WHERE " +
           "LOWER(p.college.name) LIKE LOWER(CONCAT('%', :collegeKeyword, '%')) OR " +
           "LOWER(p.college.code) = LOWER(:collegeKeyword)")
    List<PlacementRecord> findByCollegeKeyword(@Param("collegeKeyword") String collegeKeyword);

    @Query("SELECT p FROM PlacementRecord p WHERE " +
           "(LOWER(p.college.name) LIKE LOWER(CONCAT('%', :collegeKeyword, '%')) OR " +
           " LOWER(p.college.code) = LOWER(:collegeKeyword)) AND " +
           "(:batchYear IS NULL OR p.batchYear = :batchYear)")
    List<PlacementRecord> findByCollegeKeywordAndYear(
            @Param("collegeKeyword") String collegeKeyword,
            @Param("batchYear") Integer batchYear
    );

    @Query("SELECT MAX(p.highestCtc) FROM PlacementRecord p WHERE " +
           "(:collegeId IS NULL OR p.college.id = :collegeId) AND " +
           "(:batchYear IS NULL OR p.batchYear = :batchYear)")
    BigDecimal findMaxCtc(@Param("collegeId") Long collegeId, @Param("batchYear") Integer batchYear);

    @Query("SELECT AVG(p.averageCtc) FROM PlacementRecord p WHERE " +
           "(:collegeId IS NULL OR p.college.id = :collegeId) AND " +
           "(:batchYear IS NULL OR p.batchYear = :batchYear)")
    BigDecimal findAvgCtc(@Param("collegeId") Long collegeId, @Param("batchYear") Integer batchYear);

    @Query("SELECT SUM(p.totalOffers) FROM PlacementRecord p WHERE " +
           "(:collegeId IS NULL OR p.college.id = :collegeId) AND " +
           "(:batchYear IS NULL OR p.batchYear = :batchYear)")
    Long findTotalOffers(@Param("collegeId") Long collegeId, @Param("batchYear") Integer batchYear);
}
