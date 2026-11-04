package com.makaut.campussense.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/config")
@Tag(name = "Configuration", description = "Public runtime configuration and client parameters")
public class ConfigController {

    @Value("${clerk.publishable-key:}")
    private String clerkPublishableKey;

    @GetMapping("/clerk")
    @Operation(summary = "Get configured Clerk publishable key safely")
    public ResponseEntity<ClerkConfigResponse> getClerkConfig() {
        boolean configured = clerkPublishableKey != null && !clerkPublishableKey.isBlank();
        return ResponseEntity.ok(ClerkConfigResponse.builder()
                .publishableKey(configured ? clerkPublishableKey.trim() : "")
                .configured(configured)
                .build());
    }

    @GetMapping("/auth-public")
    @Operation(summary = "Get unauthenticated public authentication configuration")
    public ResponseEntity<AuthPublicConfigResponse> getAuthPublicConfig() {
        boolean configured = clerkPublishableKey != null && !clerkPublishableKey.isBlank();
        return ResponseEntity.ok(AuthPublicConfigResponse.builder()
                .clerkPublishableKey(configured ? clerkPublishableKey.trim() : "")
                .configured(configured)
                .authMode(configured ? "CLERK" : "GUEST")
                .guestAllowed(true)
                .build());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClerkConfigResponse {
        private String publishableKey;
        private boolean configured;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthPublicConfigResponse {
        private String clerkPublishableKey;
        private boolean configured;
        private String authMode;
        private boolean guestAllowed;
    }
}
