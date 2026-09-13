package com.makaut.campussense.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Random;

@Slf4j
@Service
public class EmbeddingService {

    public static final int EMBEDDING_DIMENSIONS = 1536;

    @Value("${campussense.embedding.provider:mock}")
    private String provider;

    @Value("${campussense.embedding.api-key:}")
    private String apiKey;

    @Value("${campussense.embedding.model:text-embedding-3-small}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Generates a normalized 1536-dimensional float vector for the input text.
     */
    public float[] generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return generateZeroVector();
        }

        if ("openai".equalsIgnoreCase(provider) && apiKey != null && !apiKey.isBlank()) {
            try {
                return fetchOpenAiEmbedding(text);
            } catch (Exception e) {
                log.warn("Failed to fetch OpenAI embedding ({}), falling back to deterministic local embedding", e.getMessage());
                return generateDeterministicVector(text);
            }
        }

        return generateDeterministicVector(text);
    }

    /**
     * Calls OpenAI Embeddings API (text-embedding-3-small).
     */
    private float[] fetchOpenAiEmbedding(String text) throws Exception {
        String requestBody = objectMapper.writeValueAsString(new OpenAiEmbeddingRequest(model, text));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/embeddings"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("OpenAI API returned status " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode embeddingNode = root.path("data").get(0).path("embedding");

        float[] vector = new float[EMBEDDING_DIMENSIONS];
        for (int i = 0; i < EMBEDDING_DIMENSIONS; i++) {
            vector[i] = (float) embeddingNode.get(i).asDouble();
        }

        return normalize(vector);
    }

    /**
     * Generates a deterministic, semantic-aware, L2-normalized 1536-dimensional vector.
     * Maps word tokens and n-grams consistently into 1536 feature buckets.
     */
    public float[] generateDeterministicVector(String text) {
        float[] vector = new float[EMBEDDING_DIMENSIONS];
        String clean = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] tokens = clean.split("\\s+");

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // 1. Seed projection with overall text hash
            byte[] textHash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            long textSeed = 0;
            for (int i = 0; i < 8; i++) {
                textSeed = (textSeed << 8) | (textHash[i] & 0xff);
            }
            Random textRandom = new Random(textSeed);
            for (int i = 0; i < EMBEDDING_DIMENSIONS; i++) {
                vector[i] += (float) (textRandom.nextGaussian() * 0.05);
            }

            // 2. Token-level feature projection
            for (String token : tokens) {
                if (token.length() < 2) continue;
                byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));

                // Project each token onto multiple distinct dimensions
                for (int h = 0; h < 4; h++) {
                    int dimIndex = Math.abs(((hash[h * 4] & 0xFF) << 24) |
                                            ((hash[h * 4 + 1] & 0xFF) << 16) |
                                            ((hash[h * 4 + 2] & 0xFF) << 8) |
                                            (hash[h * 4 + 3] & 0xFF)) % EMBEDDING_DIMENSIONS;

                    // Boost domain keywords
                    float weight = 1.0f;
                    if (isDomainKeyword(token)) {
                        weight = 3.5f;
                    }
                    vector[dimIndex] += weight;
                }
            }
        } catch (Exception e) {
            log.error("Error generating deterministic vector: {}", e.getMessage());
        }

        return normalize(vector);
    }

    private boolean isDomainKeyword(String token) {
        return token.contains("makaut") || token.contains("syllabus") || token.contains("module") ||
               token.contains("cs501") || token.contains("cs502") || token.contains("operating") ||
               token.contains("database") || token.contains("semaphore") || token.contains("deadlock") ||
               token.contains("mar") || token.contains("mooc") || token.contains("swayam") ||
               token.contains("nptel") || token.contains("iem") || token.contains("heritage") ||
               token.contains("tmsl") || token.contains("placement") || token.contains("package");
    }

    public float[] normalize(float[] vector) {
        double sumSq = 0.0;
        for (float v : vector) {
            sumSq += v * v;
        }
        double norm = Math.sqrt(sumSq);
        if (norm < 1e-9) {
            norm = 1.0;
        }
        float[] normalized = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = (float) (vector[i] / norm);
        }
        return normalized;
    }

    public static float cosineSimilarity(float[] vecA, float[] vecB) {
        if (vecA == null || vecB == null || vecA.length != vecB.length || vecA.length == 0) {
            return 0.0f;
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.length; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        if (denominator < 1e-9) {
            return 0.0f;
        }
        return (float) (dotProduct / denominator);
    }

    public String vectorToString(float[] vector) {
        if (vector == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private float[] generateZeroVector() {
        return new float[EMBEDDING_DIMENSIONS];
    }

    private record OpenAiEmbeddingRequest(String model, String input) {}
}
