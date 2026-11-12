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
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Context-grounded Gemini 3.8 Flash service for the MAKAUT CampusSense Ask AI drawer.
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
    private static final Pattern RAW_GOOGLE_KEY_PATTERN = Pattern.compile("AIza[0-9A-Za-z_-]{15,}");

    private static final String BASE_SYSTEM_INSTRUCTION =
            "You are the MAKAUT CampusSense Academic Assistant—an expert engineering tutor built specifically " +
            "for Maulana Abul Kalam Azad University of Technology (MAKAUT) B.Tech students. You have access " +
            "to the student's active Department, Semester, Subject Syllabi, and Top 20% Repeated Exam Question " +
            "Dossiers in the provided context. RULES: " +
            "(1) If the student sends a short conversational greeting (e.g., \"hi\", \"hello\", \"hey\", " +
            "\"good morning\", \"good evening\", \"namaste\"), reply with a warm, natural, concise 1-2 sentence " +
            "greeting tailored to their active MAKAUT Department and Semester and ask how you can help—do NOT " +
            "dump the entire syllabus or repeat-question list unless asked. " +
            "(2) When the student asks about syllabus modules, exam topics, important questions, or subject codes, " +
            "ground your answer strictly in the provided MAKAUT context and explicitly point out high-frequency " +
            "repeat exam topics. " +
            "(3) When the student asks general computer science, mathematics, electronics, coding, or engineering " +
            "questions, provide clear, accurate, exam-ready step-by-step explanations with clean formatting, bullet " +
            "points, formulas, and code snippets. " +
            "(4) Keep answers structured, concise, and high-yield for university exam preparation.";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;

    @Autowired
    public GeminiChatService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent}") String apiUrl,
            @Value("${gemini.dotenv.fallback:true}") boolean dotEnvFallbackEnabled) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = restClientBuilder.clone().requestFactory(requestFactory).build();
        this.objectMapper = objectMapper;
        this.apiKey = resolveApiKey(apiKey, dotEnvFallbackEnabled);
        this.apiUrl = apiUrl != null ? apiUrl.trim() : "";
        if (this.apiKey.isBlank()) {
            log.warn("GeminiChatService initialized WITHOUT an active GEMINI_API_KEY; /api/ai/chat will return FALLBACK_REQUIRED until configured.");
        } else {
            log.info("GeminiChatService ready with key {}**** and endpoint {}", maskKeyPrefix(this.apiKey), this.apiUrl);
        }
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

    static String resolveApiKey(String configuredKey, boolean dotEnvFallbackEnabled) {
        if (configuredKey != null && !configuredKey.trim().isBlank()) {
            return configuredKey.trim();
        }
        if (!dotEnvFallbackEnabled) {
            return "";
        }
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isBlank()) {
            return envKey.trim();
        }
        return readGeminiKeyFromDotEnv(Path.of(".env.local"), Path.of(".env"));
    }

    static String readGeminiKeyFromDotEnv(Path... candidateFiles) {
        if (candidateFiles == null) {
            return "";
        }
        for (Path file : candidateFiles) {
            if (file == null || !Files.isRegularFile(file)) {
                continue;
            }
            try {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for (String rawLine : lines) {
                    if (rawLine == null) {
                        continue;
                    }
                    String line = rawLine.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    if (line.startsWith("export ")) {
                        line = line.substring("export ".length()).trim();
                    }
                    int eq = line.indexOf('=');
                    if (eq <= 0) {
                        continue;
                    }
                    String k = line.substring(0, eq).trim();
                    if (!"GEMINI_API_KEY".equals(k)) {
                        continue;
                    }
                    String v = line.substring(eq + 1).trim();
                    if ((v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2)
                            || (v.startsWith("'") && v.endsWith("'") && v.length() >= 2)) {
                        v = v.substring(1, v.length() - 1).trim();
                    } else {
                        int hashIdx = v.indexOf(" #");
                        if (hashIdx >= 0) {
                            v = v.substring(0, hashIdx).trim();
                        }
                    }
                    if (!v.isBlank()) {
                        return v;
                    }
                }
            } catch (IOException ex) {
                log.warn("Unable to read {} for GEMINI_API_KEY fallback: {}", file, ex.getMessage());
            }
        }
        return "";
    }

    private static String maskKeyPrefix(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        return key.substring(0, Math.min(4, key.length())) + "...";
    }

    public AiChatResponse generateReply(AiChatRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY is missing or blank; switching cleanly to client syllabus fallback.");
            return fallbackRequired();
        }
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return fallbackRequired();
        }
        if (apiUrl == null || apiUrl.isBlank()) {
            log.warn("gemini.api.url is blank; switching cleanly to client syllabus fallback.");
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
        } catch (RestClientResponseException ex) {
            String snippet = sanitizeError(ex.getResponseBodyAsString());
            if (snippet.length() > 240) {
                snippet = snippet.substring(0, 240) + "...";
            }
            log.warn("Gemini API returned HTTP {} ({}); switching cleanly to client fallback.",
                    ex.getStatusCode().value(), snippet);
            return fallbackRequired();
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
        String redacted = KEY_REDACTION_PATTERN.matcher(msg).replaceAll("$1[REDACTED]");
        return RAW_GOOGLE_KEY_PATTERN.matcher(redacted).replaceAll("AIza...[REDACTED]");
    }

    private String trimOrEmpty(String s) {
        return s != null ? s.trim() : "";
    }
}

