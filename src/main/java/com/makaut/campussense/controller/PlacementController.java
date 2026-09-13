package com.makaut.campussense.controller;

import com.makaut.campussense.service.PlacementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/placements")
@RequiredArgsConstructor
@Tag(name = "Placement Statistics", description = "Query college-specific placement statistics (CTC, companies, offers)")
public class PlacementController {

    private final PlacementService placementService;

    @GetMapping
    @Operation(
            summary = "Get aggregated placement records",
            description = "Retrieves placement statistics, highest CTC, average CTC, and offer metrics filtered by college and batch year."
    )
    public ResponseEntity<PlacementService.PlacementAnalyticsResponse> getPlacements(
            @Parameter(description = "Optional College Database ID")
            @RequestParam(required = false) Long collegeId,

            @Parameter(description = "Optional Batch Year (e.g., 2023, 2024)")
            @RequestParam(required = false) Integer batchYear
    ) {
        PlacementService.PlacementAnalyticsResponse response = placementService.getPlacementAnalytics(collegeId, batchYear);
        return ResponseEntity.ok(response);
    }
}
