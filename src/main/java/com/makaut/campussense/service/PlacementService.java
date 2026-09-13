package com.makaut.campussense.service;

import com.makaut.campussense.model.PlacementRecord;
import com.makaut.campussense.repository.PlacementRecordRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlacementService {

    private final PlacementRecordRepository placementRecordRepository;

    @Getter
    @Builder
    public static class PlacementAnalyticsResponse {
        private final List<PlacementRecord> records;
        private final BigDecimal maxCtc;
        private final BigDecimal avgCtc;
        private final Long totalOffers;
        private final int totalCompanies;
    }

    public PlacementAnalyticsResponse getPlacementAnalytics(Long collegeId, Integer batchYear) {
        List<PlacementRecord> records = placementRecordRepository.findByCollegeAndBatchYear(collegeId, batchYear);

        BigDecimal maxCtc = BigDecimal.ZERO;
        BigDecimal sumCtc = BigDecimal.ZERO;
        long totalOffers = 0;
        int count = 0;

        for (PlacementRecord r : records) {
            if (r.getHighestCtc() != null && r.getHighestCtc().compareTo(maxCtc) > 0) {
                maxCtc = r.getHighestCtc();
            }
            if (r.getAverageCtc() != null) {
                sumCtc = sumCtc.add(r.getAverageCtc());
                count++;
            }
            if (r.getTotalOffers() != null) {
                totalOffers += r.getTotalOffers();
            }
        }

        BigDecimal avgCtc = count > 0 ? sumCtc.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return PlacementAnalyticsResponse.builder()
                .records(records)
                .maxCtc(maxCtc)
                .avgCtc(avgCtc)
                .totalOffers(totalOffers)
                .totalCompanies(records.size())
                .build();
    }
}
