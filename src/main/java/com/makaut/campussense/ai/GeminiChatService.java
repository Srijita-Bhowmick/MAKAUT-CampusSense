package com.makaut.campussense.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Context-grounded Gemini 2.5 Flash service for the MAKAUT CampusSense Ask AI drawer.
 * Returns FALLBACK_REQUIRED gracefully whenever GEMINI_API_KEY is unset or upstream fails.
 */
@Service
@SuppressWarnings("null")
public class GeminiChatService {

    private static final Logger log = LoggerFactory.getLogger(GeminiChatService.class);

    public static final String MODE_GEMINI_LIVE = "GEMINI_LIVE";
    public static final String MODE_FALLBACK_REQUIRED = "FALLBACK_REQUIRED";

    private static final int MAX_HISTORY_TURNS = 6;
    private static final int MAX_ACADEMIC_CONTEXT_CHARS = 4500;
    private static final Pattern CODE_TOKEN_PATTERN = Pattern.compile("\\b[A-Z]{2,5}[- ]?[A-Z]{0,4}\\d{3}[A-Z]?\\b");
    private static final Pattern KEY_REDACTION_PATTERN = Pattern.compile("(?i)([?&]key=)[^&\\s]+");

    private static final String BASE_SYSTEM_INSTRUCTION =
            "You are the MAKAUT CampusSense Academic Assistant—an expert engineering tutor built specifically " +
            "for Maulana Abul Kalam Azad University of Technology (MAKAUT) B.Tech students. You have access " +
            "to the student's active Department, Semester, Subject Syllabi, and Top 20% Repeated Exam Question " +
            "Dossiers in the provided context. RULES: (1) When the student asks about syllabus modules, exam " +
            "topics, important questions, or subject codes, ground your answer strictly in the provided MAKAUT " +
            "context and explicitly point out high-frequency repeat exam topics. (2) When the student asks " +
            "general computer science, mathematics, electronics, coding, or engineering questions, provide clear, " +
            "accurate, exam-ready step-by-step explanations with clean formatting, bullet points, formulas, and " +
            "code snippets. (3) Keep answers structured, concise, and high-yield for university exam preparation.";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;

    @Autowired
    public GeminiChatService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}") String apiUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = restClientBuilder.clone().requestFactory(requestFactory).build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.apiUrl = apiUrl != null ? apiUrl.trim() : "";
    }

    /**
     * Package-private constructor for unit and mock-server testing.
     */
    GeminiChatService(RestClient restClient, ObjectMapper objectMapper, String apiKey, String apiUrl) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.apiUrl = apiUrl != null ? apiUrl.trim() : "";
    }

    public AiChatResponse generateReply(AiChatRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackRequired();
        }
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return fallbackRequired();
        }
        if (apiUrl == null || apiUrl.isBlank()) {
            return fallbackRequired();
        }

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("systemInstruction", Map.of(
                    "parts", List.of(Map.of("text", buildSystemInstruction(request)))
            ));
            payload.put("contents", buildContents(request));
            payload.put("generationConfig", Map.of(
                    "temperature", 0.35,
                    "maxOutputTokens", 1200
            ));

            String targetUrl = buildTargetUrl(apiUrl, apiKey);
            String rawResponse = restClient.post()
                    .uri(targetUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            if (rawResponse == null || rawResponse.isBlank()) {
                return fallbackRequired();
            }

            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode textNode = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode() || textNode.isNull()) {
                return fallbackRequired();
            }

            String reply = textNode.asText("").trim();
            if (reply.isBlank()) {
                return fallbackRequired();
            }

            boolean grounded = detectSyllabusGrounding(request, reply);
            return new AiChatResponse(reply, grounded, MODE_GEMINI_LIVE);
        } catch (Exception ex) {
            log.warn("Gemini live call unavailable ({}); switching cleanly to client fallback.", sanitizeError(ex.getMessage()));
            return fallbackRequired();
        }
    }

    private AiChatResponse fallbackRequired() {
        return new AiChatResponse("", false, MODE_FALLBACK_REQUIRED);
    }

    String buildSystemInstruction(AiChatRequest request) {
        StringBuilder sb = new StringBuilder(BASE_SYSTEM_INSTRUCTION);
        String dept = trimOrEmpty(request.getDepartment());
        String sem = trimOrEmpty(request.getSemester());
        String ctx = trimOrEmpty(request.getAcademicContext());

        if (!dept.isEmpty() || !sem.isEmpty()) {
            sb.append("\n\nACTIVE STUDENT PROFILE:\n")
                    .append("Department: ").append(dept.isEmpty() ? "MAKAUT Engineering" : dept)
                    .append(" | Semester: ").append(sem.isEmpty() ? "Active" : sem);
        }

        if (!ctx.isEmpty()) {
            String boundedCtx = ctx.length() > MAX_ACADEMIC_CONTEXT_CHARS
                    ? ctx.substring(0, MAX_ACADEMIC_CONTEXT_CHARS) + "\n...[context truncated]"
                    : ctx;
            sb.append("\n\nACTIVE MAKAUT SYLLABUS & TOP 20% REPEAT PYQ CONTEXT:\n")
                    .append(boundedCtx);
        }
        return sb.toString();
    }

    List<Map<String, Object>> buildContents(AiChatRequest request) {
        List<Map<String, Object>> contents = new ArrayList<>();
        List<AiChatRequest.ChatTurn> history = request.getHistory();

        if (history != null && !history.isEmpty()) {
            int start = Math.max(0, history.size() - MAX_HISTORY_TURNS);
            for (int i = start; i < history.size(); i++) {
                AiChatRequest.ChatTurn turn = history.get(i);
                if (turn == null || turn.getText() == null || turn.getText().isBlank()) {
                    continue;
                }
                String normalizedRole = normalizeGeminiRole(turn.getRole());
                contents.add(Map.of(
                        "role", normalizedRole,
                        "parts", List.of(Map.of("text", turn.getText().trim()))
                ));
            }
        }

        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", request.getMessage().trim()))
        ));
        return contents;
    }

    private String normalizeGeminiRole(String rawRole) {
        if (rawRole == null) {
            return "user";
        }
        String r = rawRole.trim().toLowerCase(Locale.ROOT);
        if ("assistant".equals(r) || "model".equals(r)) {
            return "model";
        }
        return "user";
    }

    private boolean detectSyllabusGrounding(AiChatRequest request, String reply) {
        String ctx = trimOrEmpty(request.getAcademicContext());
        if (ctx.isEmpty()) {
            return false;
        }
        String combined = (trimOrEmpty(request.getMessage()) + " " + reply).toLowerCase(Locale.ROOT);
        if (combined.contains("syllabus") || combined.contains("module") || combined.contains("unit ")
                || combined.contains("pyq") || combined.contains("repeat") || combined.contains("makaut")
                || combined.contains("exam pattern") || combined.contains("study guide")) {
            return true;
        }
        Matcher matcher = CODE_TOKEN_PATTERN.matcher(ctx.toUpperCase(Locale.ROOT));
        while (matcher.find()) {
            String code = matcher.group().toLowerCase(Locale.ROOT);
            if (combined.contains(code)) {
                return true;
            }
        }
        return false;
    }

    private String buildTargetUrl(String baseUrl, String key) {
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + "key=" + key;
    }

    private String sanitizeError(String msg) {
        if (msg == null || msg.isBlank()) {
            return "unknown error";
        }
        return KEY_REDACTION_PATTERN.matcher(msg).replaceAll("$1[REDACTED]");
    }

    private String trimOrEmpty(String s) {
        return s != null ? s.trim() : "";
    }
}

