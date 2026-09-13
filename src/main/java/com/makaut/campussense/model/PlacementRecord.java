package com.makaut.campussense.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "placement_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(name = "batch_year", nullable = false)
    private Integer batchYear;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "highest_ctc", precision = 10, scale = 2)
    private BigDecimal highestCtc;

    @Column(name = "average_ctc", precision = 10, scale = 2)
    private BigDecimal averageCtc;

    @Column(name = "total_offers")
    private Integer totalOffers;

    @Column(name = "source_url", length = 1024)
    private String sourceUrl;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
