package com.makaut.campussense.controller;

import com.makaut.campussense.model.FrequentQuestion;
import com.makaut.campussense.repository.FrequentQuestionRepository;
import com.makaut.campussense.repository.UserBookmarkRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/panic-pack")
@RequiredArgsConstructor
@SuppressWarnings("null")
@Tag(name = "Panic Pack", description = "High-yield repeated PYQ questions ranked by occurrence frequency")
public class PanicPackController {

    private final FrequentQuestionRepository frequentQuestionRepository;
    private final UserBookmarkRepository userBookmarkRepository;

    @GetMapping
    @Operation(summary = "Get high-yield repeated questions for a subject",
            description = "Returns top 20% recurring questions across 5 years with frequency badges and model answers")
    public ResponseEntity<List<PanicQuestionDto>> getPanicPack(
            @RequestParam(required = false) String subjectCode,
            @RequestParam(defaultValue = "2") Integer minFrequency,
            @RequestParam(required = false) String userIdentifier) {

        List<FrequentQuestion> questions;
        if (subjectCode != null && !subjectCode.isBlank()) {
            questions = frequentQuestionRepository.findBySubjectCodeIgnoreCaseAndFrequencyCountGreaterThanEqualOrderByFrequencyCountDesc(
                    subjectCode.trim(), minFrequency);
        } else {
            questions = frequentQuestionRepository.findByFrequencyCountGreaterThanEqualOrderByFrequencyCountDesc(minFrequency);
        }

        Set<Long> bookmarkedQuestionIds = Set.of();
        if (userIdentifier != null && !userIdentifier.isBlank()) {
            bookmarkedQuestionIds = userBookmarkRepository.findByUserIdentifierOrderByCreatedAtDesc(userIdentifier)
                    .stream()
                    .filter(b -> b.getQuestion() != null)
                    .map(b -> b.getQuestion().getId())
                    .collect(Collectors.toSet());
        }

        Set<Long> finalBookmarkedIds = bookmarkedQuestionIds;
        List<PanicQuestionDto> dtos = questions.stream()
                .map(q -> {
                    List<String> years = Arrays.stream(q.getExamYears().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());

                    String badge = String.format("Seen %d times (High Yield)", q.getFrequencyCount());

                    return PanicQuestionDto.builder()
                            .id(q.getId())
                            .subjectCode(q.getSubjectCode())
                            .topicName(q.getTopicName())
                            .questionText(q.getQuestionText())
                            .marks(q.getMarks())
                            .frequencyCount(q.getFrequencyCount())
                            .frequencyBadge(badge)
                            .examYears(years)
                            .groupType(q.getGroupType())
                            .modelAnswerSnippet(q.getModelAnswerSnippet())
                            .bookmarked(finalBookmarkedIds.contains(q.getId()))
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PanicQuestionDto {
        private Long id;
        private String subjectCode;
        private String topicName;
        private String questionText;
        private Integer marks;
        private Integer frequencyCount;
        private String frequencyBadge;
        private List<String> examYears;
        private String groupType;
        private String modelAnswerSnippet;
        private Boolean bookmarked;
    }
}
