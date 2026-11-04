package com.makaut.campussense.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.makaut.campussense.config.CloudflareSecurityFilter;
import com.makaut.campussense.service.OtpAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Passwordless OTP Authentication", description = "Student Sign-In & Sign-Up via Email OTP with Cloudflare Security")
public class AuthController {

    private final OtpAuthService otpAuthService;

    @PostMapping("/send-otp")
    @Operation(summary = "Generate and send 6-digit OTP to student email (Passwordless)")
    public ResponseEntity<OtpAuthService.AuthResult> sendOtp(@RequestBody SendOtpRequest request, HttpServletRequest httpRequest) {
        String clientIp = CloudflareSecurityFilter.resolveClientIp(httpRequest);
        OtpAuthService.AuthResult result = otpAuthService.requestOtp(
                request.getEmail(),
                request.getName(),
                request.getMode(),
                clientIp
        );

        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify 6-digit OTP for email sign-in or sign-up")
    public ResponseEntity<OtpAuthService.AuthResult> verifyOtp(@RequestBody VerifyOtpRequest request, HttpServletRequest httpRequest) {
        String clientIp = CloudflareSecurityFilter.resolveClientIp(httpRequest);
        OtpAuthService.AuthResult result = otpAuthService.verifyOtp(
                request.getEmail(),
                request.getOtp(),
                request.getName(),
                request.getDepartment(),
                clientIp
        );

        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    @Operation(summary = "Cloudflare Security & Auth Status Check")
    public ResponseEntity<Map<String, Object>> getAuthStatus(HttpServletRequest httpRequest) {
        String clientIp = CloudflareSecurityFilter.resolveClientIp(httpRequest);
        String country = httpRequest.getHeader("CF-IPCountry");
        String cfRay = httpRequest.getHeader("CF-Ray");

        return ResponseEntity.ok(Map.of(
                "status", "ONLINE",
                "authMethod", "PASSWORDLESS_EMAIL_OTP",
                "cloudflareProtected", true,
                "clientIp", clientIp,
                "country", country != null ? country : "UNKNOWN",
                "cfRay", cfRay != null ? cfRay : "LOCAL_DEV"
        ));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SendOtpRequest {
        private String email;
        private String name;
        private String mode; // "signin" or "signup"
        private String turnstileToken; // Cloudflare Turnstile CAPTCHA response
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifyOtpRequest {
        private String email;
        private String otp;
        private String name;
        private String department;
        private String mode;
    }
}
