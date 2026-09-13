package com.makaut.campussense.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "frequent_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrequentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_code", nullable = false, length = 50)
    private String subjectCode;

    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false)
    private Integer marks;

    @Column(name = "frequency_count", nullable = false)
    @Builder.Default
    private Integer frequencyCount = 2;

    @Column(name = "exam_years", nullable = false)
    private String examYears;

    @Column(name = "group_type", nullable = false, length = 50)
    private String groupType;

    @Column(name = "model_answer_snippet", columnDefinition = "TEXT")
    private String modelAnswerSnippet;
}
