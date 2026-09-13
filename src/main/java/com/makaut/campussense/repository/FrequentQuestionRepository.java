package com.makaut.campussense.repository;

import com.makaut.campussense.model.FrequentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrequentQuestionRepository extends JpaRepository<FrequentQuestion, Long> {

    List<FrequentQuestion> findBySubjectCodeIgnoreCaseOrderByFrequencyCountDesc(String subjectCode);

    List<FrequentQuestion> findBySubjectCodeIgnoreCaseAndFrequencyCountGreaterThanEqualOrderByFrequencyCountDesc(
            String subjectCode, Integer minFrequency);

    List<FrequentQuestion> findByFrequencyCountGreaterThanEqualOrderByFrequencyCountDesc(Integer minFrequency);
}
