package com.makaut.campussense.controller;

import com.makaut.campussense.model.CourseResource;
import com.makaut.campussense.model.enums.ResourceType;
import com.makaut.campussense.model.enums.StreamType;
import com.makaut.campussense.service.AcademicResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Academic Resources", description = "Query MAKAUT syllabus, past question papers (PYQs), and study notes")
public class AcademicResourceController {

    private final AcademicResourceService academicResourceService;

    @GetMapping("/search")
    @Operation(
            summary = "Search academic resources",
            description = "Filters curriculum resources by stream, semester, subject code, and resource type with pagination."
    )
    public ResponseEntity<Page<CourseResource>> searchResources(
            @Parameter(description = "Engineering stream (e.g., CSE, IT, ECE)")
            @RequestParam(required = false) StreamType stream,

            @Parameter(description = "Academic semester (1 to 8)")
            @RequestParam(required = false) Integer semester,

            @Parameter(description = "Course / Paper code (e.g., PCC-CS501)")
            @RequestParam(required = false) String subjectCode,

            @Parameter(description = "Resource type (SYLLABUS, PYQ, ORGANIZER, NOTES, REGULATION)")
            @RequestParam(required = false) ResourceType type,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CourseResource> result = academicResourceService.searchResources(stream, semester, subjectCode, type, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get academic resource by ID")
    public ResponseEntity<CourseResource> getResourceById(@PathVariable Long id) {
        return ResponseEntity.ok(academicResourceService.getById(id));
    }
}
