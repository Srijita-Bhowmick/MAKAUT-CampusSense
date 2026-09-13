package com.makaut.campussense.aggregation.parsers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextChunkerTest {

    private TextChunker textChunker;

    @BeforeEach
    void setUp() {
        textChunker = new TextChunker();
    }

    @Test
    @DisplayName("Should chunk long text into 500-token sliding windows with 50-token overlap")
    void testSlidingWindowChunking() {
        // Construct a sample text with ~1200 words
        StringBuilder sb = new StringBuilder();
        sb.append("Module 1: Introduction to Operating Systems. Concept of OS, System Calls, Monolithic and Microkernel architectures. ");
        for (int i = 0; i < 600; i++) {
            sb.append("word").append(i).append(" ");
        }
        sb.append("Module 2: Processes and Threads. Process control block, context switching, CPU scheduling algorithms. ");
        for (int i = 0; i < 600; i++) {
            sb.append("data").append(i).append(" ");
        }

        List<TextChunker.Chunk> chunks = textChunker.chunkText(sb.toString(), 500, 50);

        assertNotNull(chunks);
        assertTrue(chunks.size() >= 3, "Expected at least 3 chunks for 1200+ words with 500 window size");

        // Verify index sequence
        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).getIndex());
            assertTrue(chunks.get(i).getEstimatedTokenCount() <= 500);
            assertFalse(chunks.get(i).getContent().isBlank());
        }

        // Verify header detection
        assertTrue(chunks.get(0).getSectionHeader().toLowerCase().contains("module 1"));
    }

    @Test
    @DisplayName("Should handle empty and short text gracefully")
    void testEmptyAndShortText() {
        List<TextChunker.Chunk> emptyChunks = textChunker.chunkText("");
        assertTrue(emptyChunks.isEmpty());

        List<TextChunker.Chunk> shortChunks = textChunker.chunkText("Module 3: Inter-process Communication. Semaphores.");
        assertEquals(1, shortChunks.size());
        assertEquals(0, shortChunks.get(0).getIndex());
        assertTrue(shortChunks.get(0).getSectionHeader().toLowerCase().contains("module 3"));
    }
}
