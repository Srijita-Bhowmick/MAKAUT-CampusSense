package com.makaut.campussense.controller;

import com.makaut.campussense.exception.ResourceNotFoundException;
import com.makaut.campussense.model.CourseResource;
import com.makaut.campussense.model.FrequentQuestion;
import com.makaut.campussense.model.SyllabusUnit;
import com.makaut.campussense.model.UserSyllabusProgress;
import com.makaut.campussense.model.enums.ResourceType;
import com.makaut.campussense.repository.CourseResourceRepository;
import com.makaut.campussense.repository.FrequentQuestionRepository;
import com.makaut.campussense.repository.SyllabusUnitRepository;
import com.makaut.campussense.repository.UserSyllabusProgressRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SuppressWarnings("null")
@Tag(name = "Subject Hub & Syllabus Checklist", description = "Subject details, interactive syllabus checklist, PYQ archive, and recurring topics")
public class SubjectHubController {

    private final CourseResourceRepository courseResourceRepository;
    private final SyllabusUnitRepository syllabusUnitRepository;
    private final FrequentQuestionRepository frequentQuestionRepository;
    private final UserSyllabusProgressRepository userSyllabusProgressRepository;

    @GetMapping("/subjects/{subjectCode}/hub")
    @Operation(summary = "Get Subject Hub data", description = "Returns units with completion checklist, 5-year PYQs, and recurring topics")
    public ResponseEntity<SubjectHubDto> getSubjectHub(
            @PathVariable String subjectCode,
            @RequestParam(required = false) String userIdentifier) {

        List<CourseResource> resources = courseResourceRepository.findBySubjectCodeIgnoreCase(subjectCode.trim());
        if (resources.isEmpty()) {
            throw new ResourceNotFoundException("No resources found for subject code: " + subjectCode);
        }

        CourseResource syllabusRes = resources.stream()
                .filter(r -> r.getResourceType() == ResourceType.SYLLABUS)
                .findFirst()
                .orElse(resources.get(0));

        // Units
        List<SyllabusUnit> units = syllabusUnitRepository.findByCourseResource_SubjectCodeIgnoreCaseOrderByUnitNumberAsc(subjectCode.trim());
        if (units.isEmpty() && syllabusRes.getId() != null) {
            units = syllabusUnitRepository.findByCourseResourceIdOrderByUnitNumberAsc(syllabusRes.getId());
        }

        // Check user progress
        Set<Long> completedUnitIds = new HashSet<>();
        if (userIdentifier != null && !userIdentifier.isBlank()) {
            List<UserSyllabusProgress> progressList = userSyllabusProgressRepository
                    .findByUserIdentifierAndSyllabusUnit_CourseResource_SubjectCodeIgnoreCase(userIdentifier, subjectCode.trim());
            for (UserSyllabusProgress p : progressList) {
                if (Boolean.TRUE.equals(p.getCompleted())) {
                    completedUnitIds.add(p.getSyllabusUnit().getId());
                }
            }
        }

        List<SyllabusUnitChecklistDto> unitDtos = units.stream()
                .map(u -> SyllabusUnitChecklistDto.builder()
                        .id(u.getId())
                        .unitNumber(u.getUnitNumber())
                        .unitTitle(u.getUnitTitle())
                        .subtopics(u.getSubtopics())
                        .hours(u.getHours())
                        .completed(completedUnitIds.contains(u.getId()))
                        .build())
                .collect(Collectors.toList());

        int totalUnits = unitDtos.size();
        long completedCount = unitDtos.stream().filter(SyllabusUnitChecklistDto::getCompleted).count();
        int completionPercentage = totalUnits > 0 ? (int) Math.round(((double) completedCount / totalUnits) * 100) : 0;

        // PYQs
        List<PyqSummaryDto> pyqs = resources.stream()
                .filter(r -> r.getResourceType() == ResourceType.PYQ)
                .map(r -> PyqSummaryDto.builder()
                        .id(r.getId())
                        .year(r.getYear())
                        .title(r.getSubjectName() + " (" + r.getYear() + ")")
                        .fileUrl(r.getFileUrl() != null ? r.getFileUrl() : "#")
                        .resourceType(r.getResourceType().name())
                        .build())
                .sorted(Comparator.comparing(PyqSummaryDto::getYear, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        // Recurring questions / topics
        List<FrequentQuestion> frequentQuestions = frequentQuestionRepository.findBySubjectCodeIgnoreCaseOrderByFrequencyCountDesc(subjectCode.trim());
        List<TopicFrequencyDto> recurringTopics = frequentQuestions.stream()
                .map(fq -> TopicFrequencyDto.builder()
                        .topicName(fq.getTopicName())
                        .repeatCount(fq.getFrequencyCount())
                        .marks(fq.getMarks())
                        .examYears(fq.getExamYears())
                        .build())
                .collect(Collectors.toList());

        SubjectHubDto hub = SubjectHubDto.builder()
                .subjectCode(syllabusRes.getSubjectCode())
                .subjectName(syllabusRes.getSubjectName())
                .stream(syllabusRes.getStream().name())
                .semester(syllabusRes.getSemester())
                .fileUrl(syllabusRes.getFileUrl())
                .units(unitDtos)
                .totalUnits(totalUnits)
                .completedUnits((int) completedCount)
                .completionPercentage(completionPercentage)
                .pyqs(pyqs)
                .recurringTopics(recurringTopics)
                .panicPackCount(frequentQuestions.size())
                .build();

        return ResponseEntity.ok(hub);
    }

    @PostMapping("/syllabus/progress/toggle")
    @Transactional
    @Operation(summary = "Toggle unit completion in syllabus checklist")
    public ResponseEntity<ProgressToggleResponse> toggleProgress(@RequestBody ProgressToggleRequest request) {
        if (request.getUserIdentifier() == null || request.getUserIdentifier().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getUnitId() == null) {
            return ResponseEntity.badRequest().build();
        }

        SyllabusUnit unit = syllabusUnitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus unit not found with id: " + request.getUnitId()));

        UserSyllabusProgress progress = userSyllabusProgressRepository
                .findByUserIdentifierAndSyllabusUnit_Id(request.getUserIdentifier(), request.getUnitId())
                .orElseGet(() -> UserSyllabusProgress.builder()
                        .userIdentifier(request.getUserIdentifier())
                        .syllabusUnit(unit)
                        .completed(false)
                        .build());

        boolean newStatus = request.getCompleted() != null ? request.getCompleted() : !Boolean.TRUE.equals(progress.getCompleted());
        progress.setCompleted(newStatus);
        progress.setUpdatedAt(Instant.now());
        userSyllabusProgressRepository.save(progress);

        // Recalculate progress for this subject
        String subjectCode = unit.getCourseResource().getSubjectCode();
        List<SyllabusUnit> allSubjectUnits = syllabusUnitRepository
                .findByCourseResource_SubjectCodeIgnoreCaseOrderByUnitNumberAsc(subjectCode);
        List<UserSyllabusProgress> userProgress = userSyllabusProgressRepository
                .findByUserIdentifierAndSyllabusUnit_CourseResource_SubjectCodeIgnoreCase(request.getUserIdentifier(), subjectCode);

        long comp = userProgress.stream().filter(p -> Boolean.TRUE.equals(p.getCompleted())).count();
        int total = allSubjectUnits.size();
        int percentage = total > 0 ? (int) Math.round(((double) comp / total) * 100) : 0;

        return ResponseEntity.ok(ProgressToggleResponse.builder()
                .unitId(unit.getId())
                .completed(newStatus)
                .subjectCode(subjectCode)
                .completedUnits((int) comp)
                .totalUnits(total)
                .completionPercentage(percentage)
                .build());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectHubDto {
        private String subjectCode;
        private String subjectName;
        private String stream;
        private Integer semester;
        private String fileUrl;
        private List<SyllabusUnitChecklistDto> units;
        private Integer totalUnits;
        private Integer completedUnits;
        private Integer completionPercentage;
        private List<PyqSummaryDto> pyqs;
        private List<TopicFrequencyDto> recurringTopics;
        private Integer panicPackCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyllabusUnitChecklistDto {
        private Long id;
        private Integer unitNumber;
        private String unitTitle;
        private String subtopics;
        private Integer hours;
        private Boolean completed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PyqSummaryDto {
        private Long id;
        private Integer year;
        private String title;
        private String fileUrl;
        private String resourceType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicFrequencyDto {
        private String topicName;
        private Integer repeatCount;
        private Integer marks;
        private String examYears;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressToggleRequest {
        private String userIdentifier;
        private Long unitId;
        private Boolean completed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressToggleResponse {
        private Long unitId;
        private Boolean completed;
        private String subjectCode;
        private Integer completedUnits;
        private Integer totalUnits;
        private Integer completionPercentage;
    }
}
