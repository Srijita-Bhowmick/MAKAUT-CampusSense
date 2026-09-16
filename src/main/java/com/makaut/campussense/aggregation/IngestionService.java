package com.makaut.campussense.aggregation;

import com.makaut.campussense.aggregation.parsers.PdfBoxParser;
import com.makaut.campussense.aggregation.parsers.TextChunker;
import com.makaut.campussense.model.CourseResource;
import com.makaut.campussense.model.DocumentEmbedding;
import com.makaut.campussense.model.enums.ResourceType;
import com.makaut.campussense.model.enums.StreamType;
import com.makaut.campussense.repository.CourseResourceRepository;
import com.makaut.campussense.repository.DocumentEmbeddingRepository;
import com.makaut.campussense.service.EmbeddingService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class IngestionService {

    private final PdfBoxParser pdfBoxParser;
    private final TextChunker textChunker;
    private final EmbeddingService embeddingService;
    private final CourseResourceRepository courseResourceRepository;
    private final DocumentEmbeddingRepository documentEmbeddingRepository;

    @Getter
    @Builder
    public static class IngestionResult {
        private final Long resourceId;
        private final String subjectCode;
        private final int chunkCount;
        private final int pageCount;
        private final String status;
    }

    /**
     * Ingests a syllabus or PYQ PDF document, extracts text, chunks it, and generates embeddings.
     */
    @Transactional
    public IngestionResult ingestPdf(
            InputStream pdfStream,
            StreamType stream,
            Integer semester,
            String subjectCode,
            String subjectName,
            ResourceType resourceType,
            Integer year,
            String fileUrl
    ) {
        try {
            log.info("Starting ingestion for PDF resource: {} (Code: {})", subjectName, subjectCode);

            // 1. Extract text and structural components via PDFBox 3.0.3
            PdfBoxParser.ParsedPdfResult parsedPdf = pdfBoxParser.parsePdf(pdfStream);

            String finalSubjectCode = (subjectCode != null && !subjectCode.isBlank())
                    ? subjectCode
                    : (parsedPdf.getDetectedSubjectCode() != null ? parsedPdf.getDetectedSubjectCode() : "GEN-001");

            // 2. Persist CourseResource metadata
            CourseResource resource = CourseResource.builder()
                    .stream(stream != null ? stream : StreamType.CSE)
                    .semester(semester != null ? semester : 5)
                    .subjectCode(finalSubjectCode)
                    .subjectName(subjectName != null ? subjectName : "Academic Document")
                    .resourceType(resourceType != null ? resourceType : ResourceType.SYLLABUS)
                    .year(year != null ? year : 2023)
                    .fileUrl(fileUrl)
                    .rawText(parsedPdf.getRawText())
                    .metadata("{\"pages\": " + parsedPdf.getPageCount() + ", \"modulesCount\": " + parsedPdf.getExtractedModules().size() + "}")
                    .build();

            resource = courseResourceRepository.save(resource);

            // 3. Chunk text dynamically (500-token sliding window, 50-token overlap)
            List<TextChunker.Chunk> chunks = textChunker.chunkText(parsedPdf.getRawText());
            log.info("Generated {} chunks for resource ID {}", chunks.size(), resource.getId());

            // 4. Generate embeddings and save
            List<DocumentEmbedding> embeddings = new ArrayList<>();
            for (TextChunker.Chunk chunk : chunks) {
                float[] vector = embeddingService.generateEmbedding(chunk.getContent());
                String vectorStr = embeddingService.vectorToString(vector);

                DocumentEmbedding docEmbedding = DocumentEmbedding.builder()
                        .resource(resource)
                        .chunkIndex(chunk.getIndex())
                        .content(chunk.getContent())
                        .embedding(vectorStr)
                        .build();

                embeddings.add(docEmbedding);
            }

            documentEmbeddingRepository.saveAll(embeddings);

            return IngestionResult.builder()
                    .resourceId(resource.getId())
                    .subjectCode(finalSubjectCode)
                    .chunkCount(chunks.size())
                    .pageCount(parsedPdf.getPageCount())
                    .status("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("Ingestion failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ingest PDF document: " + e.getMessage(), e);
        }
    }

    /**
     * Ingests raw text directly (e.g. for curriculum regulations, MAR, MOOCs notices).
     */
    @Transactional
    public IngestionResult ingestRawText(
            String text,
            StreamType stream,
            Integer semester,
            String subjectCode,
            String subjectName,
            ResourceType resourceType,
            Integer year,
            String fileUrl
    ) {
        log.info("Ingesting raw text for: {} ({})", subjectName, subjectCode);

        CourseResource resource = CourseResource.builder()
                .stream(stream != null ? stream : StreamType.GENERAL)
                .semester(semester != null ? semester : 0)
                .subjectCode(subjectCode)
                .subjectName(subjectName)
                .resourceType(resourceType != null ? resourceType : ResourceType.REGULATION)
                .year(year != null ? year : 2023)
                .fileUrl(fileUrl)
                .rawText(text)
                .metadata("{\"type\": \"RAW_TEXT\"}")
                .build();

        resource = courseResourceRepository.save(resource);

        List<TextChunker.Chunk> chunks = textChunker.chunkText(text);
        List<DocumentEmbedding> embeddings = new ArrayList<>();

        for (TextChunker.Chunk chunk : chunks) {
            float[] vector = embeddingService.generateEmbedding(chunk.getContent());
            String vectorStr = embeddingService.vectorToString(vector);

            DocumentEmbedding docEmbedding = DocumentEmbedding.builder()
                    .resource(resource)
                    .chunkIndex(chunk.getIndex())
                    .content(chunk.getContent())
                    .embedding(vectorStr)
                    .build();

            embeddings.add(docEmbedding);
        }

        documentEmbeddingRepository.saveAll(embeddings);

        return IngestionResult.builder()
                .resourceId(resource.getId())
                .subjectCode(subjectCode)
                .chunkCount(chunks.size())
                .pageCount(1)
                .status("SUCCESS")
                .build();
    }
}
