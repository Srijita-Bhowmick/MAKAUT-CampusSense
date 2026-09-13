package com.makaut.campussense.controller;

import com.makaut.campussense.model.FeedbackSubmission;
import com.makaut.campussense.repository.FeedbackSubmissionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback & Reports", description = "Student feedback, broken paper reporting, and missing PYQ requests")
public class FeedbackController {

    private final FeedbackSubmissionRepository feedbackSubmissionRepository;

    @PostMapping
    @Operation(summary = "Submit student feedback or report a broken document link")
    public ResponseEntity<FeedbackResponse> submitFeedback(@RequestBody FeedbackRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body(FeedbackResponse.builder()
                    .success(false)
                    .message("Message content cannot be empty.")
                    .build());
        }

        FeedbackSubmission submission = feedbackSubmissionRepository.save(FeedbackSubmission.builder()
                .userIdentifier(request.getUserIdentifier() != null ? request.getUserIdentifier().trim() : "anonymous")
                .category(request.getCategory() != null ? request.getCategory() : "FEATURE_REQUEST")
                .subjectCode(request.getSubjectCode())
                .message(request.getMessage().trim())
                .createdAt(Instant.now())
                .build());

        return ResponseEntity.ok(FeedbackResponse.builder()
                .success(true)
                .feedbackId(submission.getId())
                .message("Feedback recorded successfully. Thank you for contributing to MAKAUT CampusSense!")
                .build());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackRequest {
        private String userIdentifier;
        private String category;
        private String subjectCode;
        @NotBlank
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackResponse {
        private Boolean success;
        private Long feedbackId;
        private String message;
    }
}
