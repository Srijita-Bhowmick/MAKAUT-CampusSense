package com.makaut.campussense;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makaut.campussense.chat.dto.ChatQueryRequest;
import com.makaut.campussense.controller.AdminIngestController;
import com.makaut.campussense.model.enums.ResourceType;
import com.makaut.campussense.model.enums.StreamType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@SuppressWarnings("null")
class CampusSenseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Integration: POST /api/v1/chat/query - Academic RAG Query")
    void testChatAcademicQuery() throws Exception {
        ChatQueryRequest request = ChatQueryRequest.builder()
                .query("What are the topics in Module 3 of Operating Systems PCC-CS501?")
                .stream(StreamType.CSE)
                .semester(5)
                .build();

        mockMvc.perform(post("/api/v1/chat/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andExpect(jsonPath("$.response").isNotEmpty())
                .andExpect(jsonPath("$.verified", is(true)))
                .andExpect(jsonPath("$.sources", not(empty())));
    }

    @Test
    @DisplayName("Integration: POST /api/v1/chat/query - Placement Lookup Query")
    void testChatPlacementQuery() throws Exception {
        ChatQueryRequest request = ChatQueryRequest.builder()
                .query("What was the highest package at IEM in 2023 placements?")
                .build();

        mockMvc.perform(post("/api/v1/chat/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detectedIntent", is("PLACEMENT_LOOKUP")))
                .andExpect(jsonPath("$.verified", is(true)))
                .andExpect(jsonPath("$.response", containsString("IEM")));
    }

    @Test
    @DisplayName("Integration: POST /api/v1/chat/query - Negative Grounding Guardrail (Cooking Recipe)")
    void testChatNegativeGroundingCooking() throws Exception {
        ChatQueryRequest request = ChatQueryRequest.builder()
                .query("Teach me cooking recipes")
                .build();

        mockMvc.perform(post("/api/v1/chat/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified", is(false)))
                .andExpect(jsonPath("$.response", is("This information is not verified by official MAKAUT curriculum data.")))
                .andExpect(jsonPath("$.sources", empty()));
    }

    @Test
    @DisplayName("Integration: POST /api/v1/chat/query - Negative Grounding Guardrail (MIT Aeronautics)")
    void testChatNegativeGroundingMitAeronautics() throws Exception {
        ChatQueryRequest request = ChatQueryRequest.builder()
                .query("What is the syllabus for MIT Aeronautics?")
                .build();

        mockMvc.perform(post("/api/v1/chat/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified", is(false)))
                .andExpect(jsonPath("$.response", is("This information is not verified by official MAKAUT curriculum data.")))
                .andExpect(jsonPath("$.sources", empty()));
    }

    @Test
    @DisplayName("Integration: GET /api/v1/resources/search - Filter by Stream and Semester")
    void testResourceSearch() throws Exception {
        mockMvc.perform(get("/api/v1/resources/search")
                        .param("stream", "CSE")
                        .param("semester", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.content[0].subjectCode", anyOf(is("PCC-CS501"), is("PCC-CS502"))));
    }

    @Test
    @DisplayName("Integration: GET /api/v1/placements - Filter by Batch Year")
    void testPlacementsQuery() throws Exception {
        mockMvc.perform(get("/api/v1/placements")
                        .param("batchYear", "2023"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records", not(empty())))
                .andExpect(jsonPath("$.maxCtc", notNullValue()))
                .andExpect(jsonPath("$.totalOffers", greaterThan(0)));
    }

    @Test
    @DisplayName("Integration: POST /api/v1/admin/ingest/text - Ingest and Chunk Document")
    void testAdminTextIngest() throws Exception {
        AdminIngestController.TextIngestPayload payload = new AdminIngestController.TextIngestPayload();
        payload.setSubjectCode("PCC-CS601");
        payload.setSubjectName("Compiler Design");
        payload.setStream(StreamType.CSE);
        payload.setSemester(6);
        payload.setResourceType(ResourceType.SYLLABUS);
        payload.setYear(2024);
        payload.setText("Module 1: Lexical Analysis, Token, Pattern, Lexeme. Module 2: Syntax Analysis, Context-Free Grammars, LL and LR parsing.");

        mockMvc.perform(post("/api/v1/admin/ingest/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectCode", is("PCC-CS601")))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.chunkCount", greaterThan(0)));
    }

    @Test
    @DisplayName("Integration: GET /assets/resources/1st_year/syllabus/Physics.pdf - Static Resource Delivery")
    void testStaticSyllabusDelivery() throws Exception {
        mockMvc.perform(get("/assets/resources/1st_year/syllabus/Physics.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @DisplayName("Integration: GET /assets/resources/1st_year/pyq/physics/... - Static PYQ Delivery")
    void testStaticPyqDelivery() throws Exception {
        mockMvc.perform(get("/assets/resources/1st_year/pyq/physics/btech-1-sem-physics-1-bs-ph101-2026.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @DisplayName("Integration: GET /CSE(3rd to 8th sem)/CSE20.pdf - Master Syllabus Delivery")
    void testMasterCseSyllabusDelivery() throws Exception {
        mockMvc.perform(get("/CSE(3rd to 8th sem)/CSE20.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @DisplayName("Integration: GET /1st year/syllabus/Physics.pdf - Canonical 1st Year Syllabus Delivery")
    void testCanonical1stYearSyllabusDelivery() throws Exception {
        mockMvc.perform(get("/1st year/syllabus/Physics.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @DisplayName("Integration: GET /3rd to 8th sem/cse/syllabus/CSE20.pdf - Canonical 3rd to 8th sem Syllabus Delivery")
    void testCanonical3rdTo8thSemSyllabusDelivery() throws Exception {
        mockMvc.perform(get("/3rd to 8th sem/cse/syllabus/CSE20.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @DisplayName("Integration: GET /api/resources - Dynamic Resource Catalog Endpoint")
    void testDynamicResourceCatalogEndpoint() throws Exception {
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResources", greaterThan(1400)))
                .andExpect(jsonPath("$.summary.pyqCount", greaterThan(1400)))
                .andExpect(jsonPath("$.summary.syllabusCount", greaterThanOrEqualTo(15)));
    }

    @Test
    @DisplayName("Integration: GET /api/v1/config/auth-public - Public Auth Configuration Endpoint")
    void testAuthPublicConfigEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/config/auth-public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestAllowed", is(true)))
                .andExpect(jsonPath("$.authMode", notNullValue()));
    }

    @Test
    @DisplayName("Integration: Edge Cache-Control - Immutable PDFs, Catalog Caching & Dynamic Entry Points")
    void testEdgeCacheControlPolicies() throws Exception {
        // Heavy immutable PDF assets: max-age=31536000, immutable
        mockMvc.perform(get("/1st year/syllabus/Physics.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("max-age=31536000")))
                .andExpect(header().string("Cache-Control", containsString("immutable")));

        // Static structural catalogs: max-age=3600, stale-while-revalidate=86400
        mockMvc.perform(get("/resources.json"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("max-age=3600")))
                .andExpect(header().string("Cache-Control", containsString("stale-while-revalidate=86400")));

        // Core entry points: no-cache, must-revalidate
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-cache")))
                .andExpect(header().string("Cache-Control", containsString("must-revalidate")));
    }

    @Test
    @DisplayName("Integration: CORS - Cloudflare Pages & Configured Domains Authorized")
    void testCorsOriginPolicies() throws Exception {
        mockMvc.perform(get("/api/v1/config/auth-public")
                        .header("Origin", "https://campussense.pages.dev"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://campussense.pages.dev"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
