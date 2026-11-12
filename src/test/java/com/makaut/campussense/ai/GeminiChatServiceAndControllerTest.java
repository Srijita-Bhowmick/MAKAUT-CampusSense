package com.makaut.campussense.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.client.match.MockRestRequestMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "gemini.api.key=",
        "gemini.dotenv.fallback=false",
        "gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"
})
@SuppressWarnings("null")
class GeminiChatServiceAndControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("AI Controller: permits unauthenticated POST /api/ai/chat and signals FALLBACK_REQUIRED when key is unset")
    void controllerReturnsFallbackRequiredWhenKeyBlank() throws Exception {
        AiChatRequest req = new AiChatRequest(
                "Explain Dijkstras algorithm for PCC-CS404",
                "CSE",
                "4",
                "SUBJECT: PCC-CS404 Design and Analysis of Algorithms",
                List.of(new AiChatRequest.ChatTurn("user", "Hi"))
        );

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceMode", is(GeminiChatService.MODE_FALLBACK_REQUIRED)))
                .andExpect(jsonPath("$.groundedInSyllabus", is(false)))
                .andExpect(jsonPath("$.reply", is("")));
    }

    @Test
    @DisplayName("GeminiChatService: builds systemInstruction + history and parses live Gemini reply")
    void serviceCallsGeminiAndParsesGroundedReply() throws Exception {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiChatService service = new GeminiChatService(
                builder.build(),
                objectMapper,
                "test-ephemeral-token",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"
        );

        String mockGeminiJson = """
                {
                  "candidates": [
                    {
                      "content": {
                        "role": "model",
                        "parts": [
                          {
                            "text": "**PCC-CS404 Module 3:** Dijkstra finds shortest paths in O(E log V) with non-negative weights."
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=test-ephemeral-token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.jsonPath("$.systemInstruction.parts[0].text", containsString("Maulana Abul Kalam Azad University of Technology")))
                .andExpect(MockRestRequestMatchers.jsonPath("$.systemInstruction.parts[0].text", containsString("PCC-CS404")))
                .andExpect(MockRestRequestMatchers.jsonPath("$.contents[0].role", is("user")))
                .andExpect(MockRestRequestMatchers.jsonPath("$.contents[1].role", is("model")))
                .andExpect(MockRestRequestMatchers.jsonPath("$.contents[2].role", is("user")))
                .andExpect(MockRestRequestMatchers.jsonPath("$.contents[2].parts[0].text", is("Summarize PCC-CS404 Unit 3")))
                .andRespond(withSuccess(mockGeminiJson, MediaType.APPLICATION_JSON));

        AiChatRequest req = new AiChatRequest(
                "Summarize PCC-CS404 Unit 3",
                "CSE",
                "4",
                "SUBJECT: PCC-CS404 Design and Analysis of Algorithms\nUnit 3: Graph and Tree Algorithms",
                List.of(
                        new AiChatRequest.ChatTurn("user", "What is in Sem 4?"),
                        new AiChatRequest.ChatTurn("assistant", "Sem 4 covers PCC-CS401..404.")
                )
        );

        AiChatResponse response = service.generateReply(req);
        server.verify();

        assertEquals(GeminiChatService.MODE_GEMINI_LIVE, response.getSourceMode());
        assertTrue(response.isGroundedInSyllabus());
        assertTrue(response.getReply().contains("Dijkstra"));
    }

    @Test
    @DisplayName("GeminiChatService: degrades cleanly to FALLBACK_REQUIRED when upstream returns 500")
    void serviceDegradesGracefullyOnUpstreamError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiChatService service = new GeminiChatService(
                builder.build(),
                objectMapper,
                "test-ephemeral-token",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"
        );

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=test-ephemeral-token"))
                .andRespond(withServerError());

        AiChatRequest req = new AiChatRequest("What is ACID?", "CSE", "6", "", List.of());
        AiChatResponse response = service.generateReply(req);
        server.verify();

        assertEquals(GeminiChatService.MODE_FALLBACK_REQUIRED, response.getSourceMode());
        assertFalse(response.isGroundedInSyllabus());
        assertEquals("", response.getReply());
    }

    @Test
    @DisplayName("GeminiChatService: reads GEMINI_API_KEY from .env.local file and includes greeting instructions")
    void readsDotEnvKeyAndBuildsGreetingInstruction(@TempDir Path tempDir) throws Exception {
        Path dotEnvLocal = tempDir.resolve(".env.local");
        Files.writeString(dotEnvLocal, "# local test config\nGEMINI_API_KEY=\"synthetic-dotenv-token-123\"\n");

        assertEquals("synthetic-dotenv-token-123", GeminiChatService.readGeminiKeyFromDotEnv(dotEnvLocal));

        GeminiChatService service = new GeminiChatService(
                RestClient.builder().build(),
                objectMapper,
                "synthetic-token",
                "https://example.invalid"
        );
        AiChatRequest req = new AiChatRequest("hi", "CSE", "4", "SUBJECT: PCC-CS404", List.of());
        String instruction = service.buildSystemInstruction(req);
        assertTrue(instruction.contains("conversational greeting"));
        assertTrue(instruction.contains("do NOT dump the entire syllabus"));
    }
}
