package com.makaut.campussense.chat.dto;

import com.makaut.campussense.model.enums.StreamType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User query payload for MAKAUT CampusSense conversational RAG")
public class ChatQueryRequest {

    @Schema(description = "Session UUID for conversation continuity", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID sessionId;

    @NotBlank(message = "Query cannot be blank")
    @Schema(description = "Student query or natural language question", example = "What are the topics in Module 3 of Operating Systems (PCC-CS501)?")
    private String query;

    @Schema(description = "Optional academic stream filter", example = "CSE")
    private StreamType stream;

    @Schema(description = "Optional semester filter (1-8)", example = "5")
    private Integer semester;
}
