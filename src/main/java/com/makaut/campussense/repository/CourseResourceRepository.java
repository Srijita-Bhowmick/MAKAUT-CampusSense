package com.makaut.campussense.repository;

import com.makaut.campussense.model.CourseResource;
import com.makaut.campussense.model.enums.ResourceType;
import com.makaut.campussense.model.enums.StreamType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseResourceRepository extends JpaRepository<CourseResource, Long>, JpaSpecificationExecutor<CourseResource> {

    @Query("SELECT r FROM CourseResource r WHERE " +
           "(:stream IS NULL OR r.stream = :stream) AND " +
           "(:semester IS NULL OR r.semester = :semester) AND " +
           "(:subjectCode IS NULL OR LOWER(r.subjectCode) LIKE LOWER(CONCAT('%', :subjectCode, '%'))) AND " +
           "(:resourceType IS NULL OR r.resourceType = :resourceType)")
    Page<CourseResource> searchResources(
            @Param("stream") StreamType stream,
            @Param("semester") Integer semester,
            @Param("subjectCode") String subjectCode,
            @Param("resourceType") ResourceType resourceType,
            Pageable pageable
    );

    List<CourseResource> findBySubjectCodeIgnoreCase(String subjectCode);

    List<CourseResource> findByStreamAndSemester(StreamType stream, Integer semester);
}
