package com.makaut.campussense.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingServiceTest {

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService();
    }

    @Test
    @DisplayName("Should generate deterministic 1536-dimensional L2-normalized float vector")
    void testDeterministicEmbeddingProperties() {
        String text = "MAKAUT PCC-CS501 Operating Systems Module 3 Semaphores and Deadlocks";
        float[] vector = embeddingService.generateDeterministicVector(text);

        assertNotNull(vector);
        assertEquals(1536, vector.length, "Embedding vector must have exactly 1536 dimensions");

        // Verify L2 norm == 1.0 (within float precision)
        double sumSq = 0.0;
        for (float v : vector) {
            sumSq += v * v;
        }
        double norm = Math.sqrt(sumSq);
        assertEquals(1.0, norm, 1e-4, "Embedding vector must be L2-normalized to 1.0");

        // Verify determinism (same input produces exact same vector)
        float[] vector2 = embeddingService.generateDeterministicVector(text);
        assertArrayEquals(vector, vector2, 1e-6f, "Same text must yield identical vectors");
    }

    @Test
    @DisplayName("Should compute higher cosine similarity for semantically related MAKAUT texts")
    void testCosineSimilarityRanking() {
        float[] queryVec = embeddingService.generateDeterministicVector("What is banker algorithm for deadlock in operating systems?");
        float[] relatedVec = embeddingService.generateDeterministicVector("Module 4: Deadlocks, Banker Algorithm avoidance in PCC-CS501");
        float[] unrelatedVec = embeddingService.generateDeterministicVector("History of ancient Roman architecture and pottery techniques");

        float simRelated = EmbeddingService.cosineSimilarity(queryVec, relatedVec);
        float simUnrelated = EmbeddingService.cosineSimilarity(queryVec, unrelatedVec);

        assertTrue(simRelated > simUnrelated,
                "Related MAKAUT text should have higher cosine similarity (" + simRelated + ") than unrelated text (" + simUnrelated + ")");
    }

    @Test
    @DisplayName("Should serialize and deserialize vector string cleanly")
    void testVectorSerialization() {
        float[] original = new float[]{0.123f, -0.456f, 0.789f};
        String serialized = embeddingService.vectorToString(original);

        assertTrue(serialized.startsWith("["));
        assertTrue(serialized.endsWith("]"));

        com.makaut.campussense.model.DocumentEmbedding de = new com.makaut.campussense.model.DocumentEmbedding();
        de.setVectorArray(original);
        float[] parsed = de.getVectorArray();

        assertEquals(original.length, parsed.length);
        assertArrayEquals(original, parsed, 1e-5f);
    }
}
