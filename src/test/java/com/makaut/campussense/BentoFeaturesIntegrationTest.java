package com.makaut.campussense;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makaut.campussense.controller.BookmarkController;
import com.makaut.campussense.controller.FeedbackController;
import com.makaut.campussense.controller.SubjectHubController;
import com.makaut.campussense.model.FrequentQuestion;
import com.makaut.campussense.model.SyllabusUnit;
import com.makaut.campussense.repository.FrequentQuestionRepository;
import com.makaut.campussense.repository.SyllabusUnitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@SuppressWarnings("null")
class BentoFeaturesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FrequentQuestionRepository frequentQuestionRepository;

    @Autowired
    private SyllabusUnitRepository syllabusUnitRepository;

    @Test
    @DisplayName("Integration: GET /api/v1/panic-pack - Frequency Ranked Questions")
    void testGetPanicPack() throws Exception {
        mockMvc.perform(get("/api/v1/panic-pack")
                        .param("subjectCode", "ES-EE101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].subjectCode", is("ES-EE101")))
                .andExpect(jsonPath("$[0].frequencyCount", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].frequencyBadge", containsString("High Yield")))
                .andExpect(jsonPath("$[0].examYears", not(empty())));
    }

    @Test
    @DisplayName("Integration: GET /api/v1/subjects/{code}/hub - Subject Hub & Checklist")
    void testGetSubjectHub() throws Exception {
        mockMvc.perform(get("/api/v1/subjects/BS-M101/hub")
                        .param("userIdentifier", "test-user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectCode", is("BS-M101")))
                .andExpect(jsonPath("$.units", not(empty())))
                .andExpect(jsonPath("$.totalUnits", greaterThanOrEqualTo(4)))
                .andExpect(jsonPath("$.units[0].unitTitle", containsString("Calculus")))
                .andExpect(jsonPath("$.recurringTopics", not(empty())));
    }

    @Test
    @DisplayName("Integration: POST /api/v1/syllabus/progress/toggle - Toggle Checklist Unit")
    void testToggleSyllabusProgress() throws Exception {
        List<SyllabusUnit> units = syllabusUnitRepository.findByCourseResource_SubjectCodeIgnoreCaseOrderByUnitNumberAsc("BS-M101");
        org.junit.jupiter.api.Assertions.assertFalse(units.isEmpty());
        Long unitId = units.get(0).getId();

        SubjectHubController.ProgressToggleRequest request = new SubjectHubController.ProgressToggleRequest();
        request.setUserIdentifier("test-student-progress");
        request.setUnitId(unitId);
        request.setCompleted(true);

        mockMvc.perform(post("/api/v1/syllabus/progress/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitId", is(unitId.intValue())))
                .andExpect(jsonPath("$.completed", is(true)))
                .andExpect(jsonPath("$.completedUnits", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.completionPercentage", greaterThan(0)));
    }

    @Test
    @DisplayName("Integration: POST & GET /api/v1/bookmarks - Bookmark Revision Deck")
    void testBookmarkToggleAndList() throws Exception {
        List<FrequentQuestion> questions = frequentQuestionRepository.findAll();
        org.junit.jupiter.api.Assertions.assertFalse(questions.isEmpty());
        Long questionId = questions.get(0).getId();

        BookmarkController.BookmarkToggleRequest toggleReq = new BookmarkController.BookmarkToggleRequest();
        toggleReq.setUserIdentifier("test-deck-user");
        toggleReq.setQuestionId(questionId);

        // Toggle ON
        mockMvc.perform(post("/api/v1/bookmarks/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toggleReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarked", is(true)))
                .andExpect(jsonPath("$.totalBookmarks", greaterThanOrEqualTo(1)));

        // Retrieve deck
        mockMvc.perform(get("/api/v1/bookmarks")
                        .param("userIdentifier", "test-deck-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].itemType", is("QUESTION")));
    }

    @Test
    @DisplayName("Integration: POST /api/v1/feedback - Submit Student Feedback")
    void testSubmitFeedback() throws Exception {
        FeedbackController.FeedbackRequest request = new FeedbackController.FeedbackRequest();
        request.setUserIdentifier("student-feedback-user");
        request.setCategory("MISSING_PYQ");
        request.setSubjectCode("ES-CS101");
        request.setMessage("Please upload 2024 question paper with solutions.");

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.feedbackId", notNullValue()));
    }
}
