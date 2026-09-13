package com.makaut.campussense.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "syllabus_units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_resource_id", nullable = false)
    private CourseResource courseResource;

    @Column(name = "unit_number", nullable = false)
    private Integer unitNumber;

    @Column(name = "unit_title", nullable = false)
    private String unitTitle;

    @Column(columnDefinition = "TEXT")
    private String subtopics;

    @Column(name = "hours")
    @Builder.Default
    private Integer hours = 8;
}
