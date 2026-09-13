package com.makaut.campussense.repository;

import com.makaut.campussense.model.SyllabusUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyllabusUnitRepository extends JpaRepository<SyllabusUnit, Long> {

    List<SyllabusUnit> findByCourseResourceIdOrderByUnitNumberAsc(Long courseResourceId);

    List<SyllabusUnit> findByCourseResource_SubjectCodeIgnoreCaseOrderByUnitNumberAsc(String subjectCode);
}
