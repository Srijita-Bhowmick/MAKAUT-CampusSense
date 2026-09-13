package com.makaut.campussense.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_syllabus_progress",
       uniqueConstraints = @UniqueConstraint(name = "uq_user_syllabus_unit", columnNames = {"user_identifier", "syllabus_unit_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSyllabusProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_identifier", nullable = false, length = 100)
    private String userIdentifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_unit_id", nullable = false)
    private SyllabusUnit syllabusUnit;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
