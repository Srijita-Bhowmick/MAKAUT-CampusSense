package com.makaut.campussense.chat;

import com.makaut.campussense.chat.dto.ChatQueryRequest;
import com.makaut.campussense.chat.dto.ChatQueryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Conversational RAG Chat", description = "Endpoints for grounded academic advising and placement inquiries")
public class ChatController {

    private final RagService ragService;

    @PostMapping("/query")
    @Operation(
            summary = "Execute conversational RAG query",
            description = "Accepts a user query, identifies intent, performs semantic vector retrieval or structured fallback, and returns an answer grounded in MAKAUT regulations."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Query processed successfully",
                    content = @Content(schema = @Schema(implementation = ChatQueryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "429", description = "LLM rate limit reached"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ChatQueryResponse> query(@Valid @RequestBody ChatQueryRequest request) {
        ChatQueryResponse response = ragService.processQuery(request);
        return ResponseEntity.ok(response);
    }
}
