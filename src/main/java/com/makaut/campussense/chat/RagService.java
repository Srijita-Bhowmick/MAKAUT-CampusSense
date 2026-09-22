package com.makaut.campussense.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makaut.campussense.chat.dto.ChatQueryRequest;
import com.makaut.campussense.chat.dto.ChatQueryResponse;
import com.makaut.campussense.chat.dto.SourceCitationDto;
import com.makaut.campussense.model.ChatMessage;
import com.makaut.campussense.model.ChatSession;
import com.makaut.campussense.model.DocumentEmbedding;
import com.makaut.campussense.model.PlacementRecord;
import com.makaut.campussense.model.enums.MessageRole;
import com.makaut.campussense.repository.ChatMessageRepository;
import com.makaut.campussense.repository.ChatSessionRepository;
import com.makaut.campussense.repository.DocumentEmbeddingRepository;
import com.makaut.campussense.repository.PlacementRecordRepository;
import com.makaut.campussense.service.EmbeddingService;
import com.makaut.campussense.service.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class RagService {

    private final EmbeddingService embeddingService;
    private final LlmService llmService;
    private final DocumentEmbeddingRepository documentEmbeddingRepository;
    private final PlacementRecordRepository placementRecordRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${campussense.vector.top-k:5}")
    private int topK;

    @Value("${campussense.vector.similarity-threshold:0.55}")
    private float similarityThreshold;

    private static final Pattern PLACEMENT_PATTERN = Pattern.compile(
            "\\b(placement|ctc|package|highest package|average package|offers|recruiter|recruitment|salary|lpa)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern YEAR_PATTERN = Pattern.compile("\\b(202[0-9])\\b");

    private static final Map<String, String> COLLEGE_ALIASES = Map.of(
            "iem", "109",
            "heritage", "126",
            "hitk", "126",
            "techno", "104",
            "tmsl", "104",
            "haldia", "103",
            "hit", "103",
            "nsec", "108"
    );

    @Transactional
    public ChatQueryResponse processQuery(ChatQueryRequest request) {
        long startTime = System.currentTimeMillis();
        UUID sessionId = ensureSession(request.getSessionId(), request.getQuery());

        // 1. Intent Detection
        String userQuery = request.getQuery().trim();
        String normalizedQuery = normalizeQuery(userQuery);
        boolean isPlacementQuery = isPlacementIntent(userQuery);

        StringBuilder contextBuilder = new StringBuilder();
        List<SourceCitationDto> citations = new ArrayList<>();
        String detectedIntent = "ACADEMIC_RAG";

        // 2. Structured SQL Fallback for Placement Inquiries
        if (isPlacementQuery) {
            detectedIntent = "PLACEMENT_LOOKUP";
            String structuredData = retrieveStructuredPlacementData(userQuery);
            if (!structuredData.isBlank()) {
                contextBuilder.append(structuredData).append("\n\n");
                citations.add(SourceCitationDto.builder()
                        .subjectName("Official Placement Records")
                        .subjectCode("PLACEMENT-DB")
                        .resourceType("STRUCTURED_DATA")
                        .moduleOrSection("College Placement Statistics")
                        .similarity(1.0f)
                        .fileUrl("https://makautexam.net/placement-cell")
                        .build());
            }
        }

        // 3. Unstructured Vector Retrieval
        List<ScoredEmbedding> vectorHits = searchSimilarEmbeddings(
                normalizedQuery.isBlank() ? userQuery : normalizedQuery,
                request.getStream() != null ? request.getStream().name() : null,
                request.getSemester()
        );

        for (ScoredEmbedding hit : vectorHits) {
            if (hit.getSimilarity() >= similarityThreshold) {
                contextBuilder.append("--- CHUNK (Similarity: ")
                        .append(String.format("%.3f", hit.getSimilarity()))
                        .append(") ---\n")
                        .append(hit.getEmbedding().getContent())
                        .append("\n\n");

                String chunkText = hit.getEmbedding().getContent();
                String groupDetected = chunkText.contains("GROUP B") ? "Group B" : (chunkText.contains("GROUP C") ? "Group C" : "Syllabus Module");
                String qNum = null;
                java.util.regex.Matcher qm = java.util.regex.Pattern.compile("(Q[0-9]+)").matcher(chunkText);
                if (qm.find()) {
                    qNum = qm.group(1);
                }

                citations.add(SourceCitationDto.builder()
                        .subjectName(hit.getEmbedding().getResource().getSubjectName())
                        .subjectCode(hit.getEmbedding().getResource().getSubjectCode())
                        .resourceType(hit.getEmbedding().getResource().getResourceType().name())
                        .moduleOrSection("Chunk #" + hit.getEmbedding().getChunkIndex())
                        .year(hit.getEmbedding().getResource().getYear())
                        .similarity(hit.getSimilarity())
                        .fileUrl(hit.getEmbedding().getResource().getFileUrl())
                        .title(hit.getEmbedding().getResource().getSubjectName() + " (" + hit.getEmbedding().getResource().getSubjectCode() + ")")
                        .group(groupDetected)
                        .questionNumber(qNum != null ? qNum : "Syllabus")
                        .pageUrl(hit.getEmbedding().getResource().getFileUrl())
                        .build());
            }
        }

        // 4. Grounded Synthesis or Negative Fallback
        String finalResponse;
        boolean verified = true;

        if (contextBuilder.length() == 0) {
            finalResponse = PromptTemplates.UNVERIFIED_FALLBACK_MESSAGE;
            verified = false;
        } else {
            finalResponse = llmService.generateCompletion(contextBuilder.toString(), userQuery);
            if (finalResponse.contains(PromptTemplates.UNVERIFIED_FALLBACK_MESSAGE)) {
                verified = false;
                citations.clear();
            }
        }

        // 5. Record Chat History
        recordHistory(sessionId, userQuery, finalResponse, citations);

        long latency = System.currentTimeMillis() - startTime;
        log.info("RAG query processed in {} ms with {} sources. Verified: {}", latency, citations.size(), verified);

        return ChatQueryResponse.builder()
                .sessionId(sessionId)
                .response(finalResponse)
                .detectedIntent(detectedIntent)
                .sources(citations)
                .verified(verified)
                .executionTimeMs(latency)
                .build();
    }

    private boolean isPlacementIntent(String query) {
        return PLACEMENT_PATTERN.matcher(query).find();
    }

    private String retrieveStructuredPlacementData(String query) {
        String lower = query.toLowerCase();

        // Detect college code / alias
        String collegeCode = null;
        for (Map.Entry<String, String> entry : COLLEGE_ALIASES.entrySet()) {
            if (lower.contains(entry.getKey())) {
                collegeCode = entry.getValue();
                break;
            }
        }

        // Detect year
        Integer year = null;
        Matcher yearMatcher = YEAR_PATTERN.matcher(query);
        if (yearMatcher.find()) {
            year = Integer.parseInt(yearMatcher.group(1));
        }

        List<PlacementRecord> records;
        if (collegeCode != null) {
            records = placementRecordRepository.findByCollegeKeywordAndYear(collegeCode, year);
            if (records.isEmpty() && year != null) {
                // Fallback to all years for that college
                records = placementRecordRepository.findByCollegeKeyword(collegeCode);
            }
        } else {
            // General query across top colleges
            records = placementRecordRepository.findByCollegeAndBatchYear(null, year != null ? year : 2023);
        }

        if (records.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("PLACEMENT STATISTICS:\n");
        for (PlacementRecord r : records) {
            sb.append(String.format("- %s [%s] Batch %d: Company=%s | Highest CTC=%.2f LPA | Avg CTC=%.2f LPA | Total Offers=%d\n",
                    r.getCollege().getName(),
                    r.getCollege().getCode(),
                    r.getBatchYear(),
                    r.getCompanyName(),
                    r.getHighestCtc() != null ? r.getHighestCtc().doubleValue() : 0.0,
                    r.getAverageCtc() != null ? r.getAverageCtc().doubleValue() : 0.0,
                    r.getTotalOffers() != null ? r.getTotalOffers() : 0));
        }
        return sb.toString();
    }

    private final org.springframework.core.env.Environment environment;

    /**
     * Top-K Cosine Similarity Search supporting both PostgreSQL (native pgvector)
     * and in-memory dev mode.
     */
    private List<ScoredEmbedding> searchSimilarEmbeddings(String query, String stream, Integer semester) {
        float[] queryEmbedding = embeddingService.generateEmbedding(query);
        String queryVectorStr = embeddingService.vectorToString(queryEmbedding);

        boolean isPostgres = Arrays.asList(environment.getActiveProfiles()).contains("postgres");

        // Execute native pgvector query when running in postgres profile
        if (isPostgres) {
            try {
                List<Object[]> nativeResults;
                if (stream != null || semester != null) {
                    nativeResults = documentEmbeddingRepository.searchTopKSimilarWithFilterNative(
                            queryVectorStr, stream, semester, topK
                    );
                } else {
                    nativeResults = documentEmbeddingRepository.searchTopKSimilarNative(queryVectorStr, topK);
                }

                if (nativeResults != null && !nativeResults.isEmpty()) {
                    List<ScoredEmbedding> results = new ArrayList<>();
                    for (Object[] row : nativeResults) {
                        Long id = ((Number) row[0]).longValue();
                        Double similarity = ((Number) row[6]).doubleValue();

                        DocumentEmbedding de = documentEmbeddingRepository.findById(id).orElse(null);
                        if (de != null) {
                            results.add(new ScoredEmbedding(de, similarity.floatValue()));
                        }
                    }
                    return results;
                }
            } catch (Exception e) {
                log.warn("Native pgvector query failed: {}", e.getMessage());
            }
        }

        // In-memory fallback calculation (for dev / test profiles)
        List<DocumentEmbedding> allEmbeddings = documentEmbeddingRepository.findAllWithResource();
        List<ScoredEmbedding> scored = new ArrayList<>();

        for (DocumentEmbedding de : allEmbeddings) {
            if (stream != null && de.getResource().getStream() != null) {
                if (!de.getResource().getStream().name().equalsIgnoreCase(stream)) {
                    continue;
                }
            }
            if (semester != null && de.getResource().getSemester() != null && !de.getResource().getSemester().equals(0)) {
                if (!de.getResource().getSemester().equals(semester)) {
                    continue;
                }
            }

            float[] docVector = de.getVectorArray();
            float sim = EmbeddingService.cosineSimilarity(queryEmbedding, docVector);
            scored.add(new ScoredEmbedding(de, sim));
        }

        scored.sort((a, b) -> Float.compare(b.getSimilarity(), a.getSimilarity()));
        return scored.subList(0, Math.min(scored.size(), topK));
    }

    private UUID ensureSession(UUID sessionId, String firstQuery) {
        if (sessionId == null) {
            ChatSession newSession = ChatSession.builder()
                    .id(UUID.randomUUID())
                    .title(firstQuery.length() > 50 ? firstQuery.substring(0, 47) + "..." : firstQuery)
                    .build();
            return chatSessionRepository.save(newSession).getId();
        }
        return chatSessionRepository.findById(sessionId)
                .map(ChatSession::getId)
                .orElseGet(() -> {
                    ChatSession s = ChatSession.builder().id(sessionId).title(firstQuery).build();
                    return chatSessionRepository.save(s).getId();
                });
    }

    private void recordHistory(UUID sessionId, String userQuery, String response, List<SourceCitationDto> citations) {
        try {
            ChatMessage userMsg = ChatMessage.builder()
                    .sessionId(sessionId)
                    .role(MessageRole.USER)
                    .message(userQuery)
                    .build();
            chatMessageRepository.save(userMsg);

            String sourcesJson = objectMapper.writeValueAsString(citations);
            ChatMessage assistantMsg = ChatMessage.builder()
                    .sessionId(sessionId)
                    .role(MessageRole.ASSISTANT)
                    .message(response)
                    .contextSources(sourcesJson)
                    .build();
            chatMessageRepository.save(assistantMsg);
        } catch (Exception e) {
            log.error("Failed to record chat history: {}", e.getMessage());
        }
    }

    private String normalizeQuery(String raw) {
        if (raw == null) return "";
        String q = raw.toLowerCase();
        q = q.replaceAll("\\beglish\\b", "english");
        q = q.replaceAll("\\beng\\b", "english");
        q = q.replaceAll("\\bphy\\b", "physics");
        q = q.replaceAll("\\bchem\\b", "chemistry");
        q = q.replaceAll("\\bpcc\\b", "programming for problem solving");
        q = q.replaceAll("\\bpps\\b", "programming for problem solving");
        q = q.replaceAll("\\bm1\\b", "mathematics 1");
        q = q.replaceAll("\\bm2\\b", "mathematics 2");
        q = q.replaceAll("\\bee\\b", "basic electrical engineering");
        q = q.replaceAll("\\bbee\\b", "basic electrical engineering");
        q = q.replaceAll("\\bbio\\b", "biology");
        q = q.replaceAll("\\bevs\\b", "environmental science");
        q = q.replaceAll("\\bdsa\\b", "data structure and algorithms");
        q = q.replaceAll("\\bflat\\b", "formal language and automata theory");
        q = q.replaceAll("\\btoc\\b", "formal language and automata theory");
        q = q.replaceAll("\\bdaa\\b", "design and analysis of algorithms");
        q = q.replaceAll("\\bco\\b", "computer organisation");
        q = q.replaceAll("\\bca\\b", "computer architecture");
        q = q.replaceAll("\\bos\\b", "operating systems");
        q = q.replaceAll("\\bdbms\\b", "database management systems");
        q = q.replaceAll("\\bcn\\b", "computer networks");
        q = q.replaceAll("\\bse\\b", "software engineering");
        q = q.replaceAll("\\bai\\b", "artificial intelligence");
        return q;
    }

    @lombok.Value
    public static class ScoredEmbedding {
        DocumentEmbedding embedding;
        float similarity;
    }
}
