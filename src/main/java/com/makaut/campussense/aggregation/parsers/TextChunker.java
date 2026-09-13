package com.makaut.campussense.aggregation.parsers;

import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TextChunker {

    public static final int DEFAULT_CHUNK_SIZE_TOKENS = 500;
    public static final int DEFAULT_OVERLAP_TOKENS = 50;

    // Pattern to identify MAKAUT syllabus modules, groups, or exam questions
    private static final Pattern MODULE_HEADER_PATTERN = Pattern.compile(
            "(Module\\s+\\d+[^:\\n]*:|GROUP\\s+[A-Z][^\\n]*|Question\\s+\\d+:?)",
            Pattern.CASE_INSENSITIVE
    );

    @Getter
    @Builder
    public static class Chunk {
        private final int index;
        private final String content;
        private final String sectionHeader;
        private final int estimatedTokenCount;
    }

    /**
     * Chunk text using sliding window of target token size and overlap.
     * 1 token is estimated as ~4 characters or space-delimited words.
     */
    public List<Chunk> chunkText(String text, int chunkSizeTokens, int overlapTokens) {
        List<Chunk> chunks = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        String normalizedText = text.replace("\r\n", "\n").replace("\r", "\n").trim();
        String[] words = normalizedText.split("\\s+");

        if (words.length == 0) {
            return chunks;
        }

        int step = Math.max(1, chunkSizeTokens - overlapTokens);
        int chunkIndex = 0;
        String currentSection = "General Context";

        for (int i = 0; i < words.length; i += step) {
            int end = Math.min(words.length, i + chunkSizeTokens);
            StringBuilder chunkBuilder = new StringBuilder();

            for (int j = i; j < end; j++) {
                chunkBuilder.append(words[j]).append(" ");
            }

            String chunkContent = chunkBuilder.toString().trim();

            // Detect current structural section header (Module, Group, etc.)
            Matcher matcher = MODULE_HEADER_PATTERN.matcher(chunkContent);
            if (matcher.find()) {
                currentSection = matcher.group(1).trim();
            }

            // Estimate tokens (approx words.length in this window)
            int tokenCount = end - i;

            chunks.add(Chunk.builder()
                    .index(chunkIndex++)
                    .content(chunkContent)
                    .sectionHeader(currentSection)
                    .estimatedTokenCount(tokenCount)
                    .build());

            if (end >= words.length) {
                break;
            }
        }

        return chunks;
    }

    public List<Chunk> chunkText(String text) {
        return chunkText(text, DEFAULT_CHUNK_SIZE_TOKENS, DEFAULT_OVERLAP_TOKENS);
    }
}
