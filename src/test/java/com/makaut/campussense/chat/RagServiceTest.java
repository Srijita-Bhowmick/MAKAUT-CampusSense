package com.makaut.campussense.chat;

import com.makaut.campussense.chat.dto.ChatQueryRequest;
import com.makaut.campussense.chat.dto.ChatQueryResponse;
import com.makaut.campussense.model.College;
import com.makaut.campussense.model.CourseResource;
import com.makaut.campussense.model.DocumentEmbedding;
import com.makaut.campussense.model.PlacementRecord;
import com.makaut.campussense.model.ChatSession;
import com.makaut.campussense.model.enums.ResourceType;
import com.makaut.campussense.model.enums.StreamType;
import com.makaut.campussense.repository.ChatMessageRepository;
import com.makaut.campussense.repository.ChatSessionRepository;
import com.makaut.campussense.repository.DocumentEmbeddingRepository;
import com.makaut.campussense.repository.PlacementRecordRepository;
import com.makaut.campussense.service.EmbeddingService;
import com.makaut.campussense.service.LlmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class RagServiceTest {

    private final EmbeddingService embeddingService = new EmbeddingService();

    @Mock
    private LlmService llmService;

    @Mock
    private DocumentEmbeddingRepository documentEmbeddingRepository;

    @Mock
    private PlacementRecordRepository placementRecordRepository;

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private org.springframework.core.env.Environment environment;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(chatSessionRepository.save(any(ChatSession.class))).thenAnswer(inv -> inv.getArgument(0));

        ragService = new RagService(
                embeddingService,
                llmService,
                documentEmbeddingRepository,
                placementRecordRepository,
                chatSessionRepository,
                chatMessageRepository,
                environment
        );
        ReflectionTestUtils.setField(ragService, "topK", 5);
        ReflectionTestUtils.setField(ragService, "similarityThreshold", 0.20f);
    }

    @Test
    @DisplayName("Should detect placement query and route to SQL structured data fallback")
    void testPlacementIntentRouting() {
        College iem = College.builder().id(1L).name("Institute of Engineering & Management").code("109").location("Kolkata").build();
        PlacementRecord rec = PlacementRecord.builder()
                .id(10L)
                .college(iem)
                .batchYear(2023)
                .companyName("Amazon")
                .highestCtc(BigDecimal.valueOf(44.00))
                .averageCtc(BigDecimal.valueOf(44.00))
                .totalOffers(18)
                .build();

        when(placementRecordRepository.findByCollegeKeywordAndYear(eq("109"), eq(2023)))
                .thenReturn(List.of(rec));
        when(documentEmbeddingRepository.findAllWithResource()).thenReturn(List.of());
        when(llmService.generateCompletion(anyString(), anyString())).thenReturn("IEM 2023 Highest package was 44 LPA at Amazon.");

        ChatQueryRequest request = ChatQueryRequest.builder()
                .query("What was the highest package at IEM in 2023 placement?")
                .build();

        ChatQueryResponse response = ragService.processQuery(request);

        assertNotNull(response);
        assertEquals("PLACEMENT_LOOKUP", response.getDetectedIntent());
        assertTrue(response.isVerified());
        assertFalse(response.getSources().isEmpty());
        assertEquals("STRUCTURED_DATA", response.getSources().get(0).getResourceType());
    }

    @Test
    @DisplayName("Should ground academic syllabus query with module citations")
    void testAcademicSyllabusRetrieval() {
        CourseResource resource = CourseResource.builder()
                .id(1L)
                .stream(StreamType.CSE)
                .semester(5)
                .subjectCode("PCC-CS501")
                .subjectName("Operating Systems")
                .resourceType(ResourceType.SYLLABUS)
                .year(2023)
                .fileUrl("https://makautexam.net/syllabus.pdf")
                .build();

        String chunkContent = "Module 3: Inter-process Communication: Critical Section, Race Conditions, Semaphores.";
        float[] vector = embeddingService.generateEmbedding(chunkContent);

        DocumentEmbedding embedding = DocumentEmbedding.builder()
                .id(101L)
                .resource(resource)
                .chunkIndex(2)
                .content(chunkContent)
                .embedding(embeddingService.vectorToString(vector))
                .build();

        when(documentEmbeddingRepository.findAllWithResource()).thenReturn(List.of(embedding));

        when(llmService.generateCompletion(anyString(), anyString()))
                .thenReturn("Operating Systems Module 3 covers Semaphores [Module 3: Inter-process Communication, PCC-CS501].");

        ChatQueryRequest request = ChatQueryRequest.builder()
                .query("What is in Module 3 of PCC-CS501 Operating Systems?")
                .stream(StreamType.CSE)
                .semester(5)
                .build();

        ChatQueryResponse response = ragService.processQuery(request);

        assertNotNull(response);
        assertEquals("ACADEMIC_RAG", response.getDetectedIntent());
        assertTrue(response.isVerified());
        assertFalse(response.getSources().isEmpty());
        assertEquals("PCC-CS501", response.getSources().get(0).getSubjectCode());
    }

    @Test
    @DisplayName("Should invoke negative grounding fallback when no relevant curriculum data exists")
    void testNegativeGroundingFallback() {
        when(documentEmbeddingRepository.findAllWithResource()).thenReturn(List.of());

        ChatQueryRequest request = ChatQueryRequest.builder()
                .query("Who won the 2018 FIFA World Cup in football?")
                .build();

        ChatQueryResponse response = ragService.processQuery(request);

        assertNotNull(response);
        assertFalse(response.isVerified());
        assertEquals(PromptTemplates.UNVERIFIED_FALLBACK_MESSAGE, response.getResponse());
        assertTrue(response.getSources().isEmpty());
    }
}
