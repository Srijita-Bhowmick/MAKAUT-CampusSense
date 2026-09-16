package com.makaut.campussense.controller;

import com.makaut.campussense.exception.ResourceNotFoundException;
import com.makaut.campussense.model.CourseResource;
import com.makaut.campussense.model.FrequentQuestion;
import com.makaut.campussense.model.UserBookmark;
import com.makaut.campussense.repository.CourseResourceRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
@SuppressWarnings("null")
@Tag(name = "User Bookmarks", description = "Save revision deck questions and academic resources")
public class BookmarkController {

    private final UserBookmarkRepository userBookmarkRepository;
    private final FrequentQuestionRepository frequentQuestionRepository;
    private final CourseResourceRepository courseResourceRepository;

    @GetMapping
    @Operation(summary = "Get user's saved revision deck")
    public ResponseEntity<List<SavedBookmarkDto>> getBookmarks(@RequestParam String userIdentifier) {
        if (userIdentifier == null || userIdentifier.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<UserBookmark> bookmarks = userBookmarkRepository.findByUserIdentifierOrderByCreatedAtDesc(userIdentifier.trim());
        List<SavedBookmarkDto> dtos = bookmarks.stream()
                .map(b -> {
                    SavedBookmarkDto dto = SavedBookmarkDto.builder()
                            .id(b.getId())
                            .createdAt(b.getCreatedAt())
                            .build();

                    if (b.getQuestion() != null) {
                        FrequentQuestion q = b.getQuestion();
                        dto.setItemType("QUESTION");
                        dto.setQuestionId(q.getId());
                        dto.setSubjectCode(q.getSubjectCode());
                        dto.setTitle(q.getTopicName() + " (" + q.getMarks() + " Marks)");
                        dto.setContentSnippet(q.getQuestionText());
                        dto.setFrequencyBadge("Seen " + q.getFrequencyCount() + " times");
                        dto.setModelAnswer(q.getModelAnswerSnippet());
                    } else if (b.getResource() != null) {
                        CourseResource r = b.getResource();
                        dto.setItemType("RESOURCE");
                        dto.setResourceId(r.getId());
                        dto.setSubjectCode(r.getSubjectCode());
                        dto.setTitle(r.getSubjectName());
                        dto.setContentSnippet(r.getRawText() != null && r.getRawText().length() > 200
                                ? r.getRawText().substring(0, 200) + "..." : r.getRawText());
                        dto.setFileUrl(r.getFileUrl());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/toggle")
    @Transactional
    @Operation(summary = "Toggle bookmark for a question or resource")
    public ResponseEntity<BookmarkToggleResponse> toggleBookmark(@RequestBody BookmarkToggleRequest request) {
        if (request.getUserIdentifier() == null || request.getUserIdentifier().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String userId = request.getUserIdentifier().trim();
        boolean bookmarked;
        Long bookmarkId = null;

        if (request.getQuestionId() != null) {
            Optional<UserBookmark> existing = userBookmarkRepository.findByUserIdentifierAndQuestion_Id(userId, request.getQuestionId());
            if (existing.isPresent()) {
                userBookmarkRepository.delete(existing.get());
                bookmarked = false;
            } else {
                FrequentQuestion q = frequentQuestionRepository.findById(request.getQuestionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + request.getQuestionId()));
                UserBookmark b = userBookmarkRepository.save(UserBookmark.builder()
                        .userIdentifier(userId)
                        .question(q)
                        .createdAt(Instant.now())
                        .build());
                bookmarkId = b.getId();
                bookmarked = true;
            }
        } else if (request.getResourceId() != null) {
            Optional<UserBookmark> existing = userBookmarkRepository.findByUserIdentifierAndResource_Id(userId, request.getResourceId());
            if (existing.isPresent()) {
                userBookmarkRepository.delete(existing.get());
                bookmarked = false;
            } else {
                CourseResource r = courseResourceRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.getResourceId()));
                UserBookmark b = userBookmarkRepository.save(UserBookmark.builder()
                        .userIdentifier(userId)
                        .resource(r)
                        .createdAt(Instant.now())
                        .build());
                bookmarkId = b.getId();
                bookmarked = true;
            }
        } else {
            return ResponseEntity.badRequest().build();
        }

        long totalBookmarks = userBookmarkRepository.countByUserIdentifier(userId);

        return ResponseEntity.ok(BookmarkToggleResponse.builder()
                .bookmarked(bookmarked)
                .bookmarkId(bookmarkId)
                .totalBookmarks(totalBookmarks)
                .build());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookmarkToggleRequest {
        private String userIdentifier;
        private Long questionId;
        private Long resourceId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookmarkToggleResponse {
        private Boolean bookmarked;
        private Long bookmarkId;
        private Long totalBookmarks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavedBookmarkDto {
        private Long id;
        private String itemType;
        private Long questionId;
        private Long resourceId;
        private String subjectCode;
        private String title;
        private String contentSnippet;
        private String frequencyBadge;
        private String modelAnswer;
        private String fileUrl;
        private Instant createdAt;
    }
}
