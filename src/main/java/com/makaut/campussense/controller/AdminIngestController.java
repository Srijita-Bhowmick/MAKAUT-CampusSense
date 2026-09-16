package com.makaut.campussense.controller;

import com.makaut.campussense.aggregation.IngestionService;
import com.makaut.campussense.model.enums.ResourceType;
import com.makaut.campussense.model.enums.StreamType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Ingestion", description = "Endpoints for ingesting PDFs, syllabus documents, and URLs into the vector store")
public class AdminIngestController {

    private final IngestionService ingestionService;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @PostMapping(value = "/ingest/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ingest uploaded PDF document into vector database")
    public ResponseEntity<IngestionService.IngestionResult> ingestPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "CSE") StreamType stream,
            @RequestParam(required = false, defaultValue = "5") Integer semester,
            @RequestParam(required = false) String subjectCode,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false, defaultValue = "SYLLABUS") ResourceType resourceType,
            @RequestParam(required = false, defaultValue = "2023") Integer year
    ) {
        try (InputStream is = file.getInputStream()) {
            IngestionService.IngestionResult result = ingestionService.ingestPdf(
                    is, stream, semester, subjectCode,
                    subjectName != null ? subjectName : file.getOriginalFilename(),
                    resourceType, year, file.getOriginalFilename()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to ingest PDF: {}", e.getMessage());
            throw new RuntimeException("Error processing PDF upload: " + e.getMessage(), e);
        }
    }

    @PostMapping("/ingest/text")
    @Operation(summary = "Ingest raw syllabus or regulation text into vector database")
    public ResponseEntity<IngestionService.IngestionResult> ingestText(@RequestBody TextIngestPayload payload) {
        IngestionService.IngestionResult result = ingestionService.ingestRawText(
                payload.getText(),
                payload.getStream(),
                payload.getSemester(),
                payload.getSubjectCode(),
                payload.getSubjectName(),
                payload.getResourceType(),
                payload.getYear(),
                payload.getFileUrl()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/ingest/url")
    @Operation(summary = "Ingest online PDF from MAKAUT portal URL into vector database")
    public ResponseEntity<IngestionService.IngestionResult> ingestFromUrl(@RequestBody UrlIngestPayload payload) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(payload.getUrl()))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to download PDF from URL, HTTP status: " + response.statusCode());
            }

            IngestionService.IngestionResult result = ingestionService.ingestPdf(
                    response.body(),
                    payload.getStream(),
                    payload.getSemester(),
                    payload.getSubjectCode(),
                    payload.getSubjectName(),
                    payload.getResourceType(),
                    payload.getYear(),
                    payload.getUrl()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to ingest from URL: {}", e.getMessage());
            throw new RuntimeException("Error ingesting from URL: " + e.getMessage(), e);
        }
    }

    @Data
    public static class TextIngestPayload {
        private String text;
        private StreamType stream;
        private Integer semester;
        private String subjectCode;
        private String subjectName;
        private ResourceType resourceType;
        private Integer year;
        private String fileUrl;
    }

    @Data
    public static class UrlIngestPayload {
        private String url;
        private StreamType stream;
        private Integer semester;
        private String subjectCode;
        private String subjectName;
        private ResourceType resourceType;
        private Integer year;
    }
}
