package com.makaut.campussense;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makaut.campussense.controller.AuthController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@SuppressWarnings("null")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Auth: Send OTP to valid email")
    void testSendOtpValidEmail() throws Exception {
        AuthController.SendOtpRequest request = new AuthController.SendOtpRequest(
                "student.test@makaut.ac.in",
                "Test Student",
                "signin",
                "dummy_turnstile_token"
        );

        mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("CF-Connecting-IP", "1.1.1.1")
                        .header("CF-IPCountry", "IN")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.otp", notNullValue()))
                .andExpect(jsonPath("$.expiresInSeconds", is(300)))
                .andExpect(header().string("X-Cloudflare-Protection", containsString("Turnstile-Enabled")));
    }

    @Test
    @DisplayName("Auth: Send OTP rejects invalid email")
    void testSendOtpInvalidEmail() throws Exception {
        AuthController.SendOtpRequest request = new AuthController.SendOtpRequest(
                "invalid-email",
                null,
                "signup",
                null
        );

        mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Auth: Verify valid OTP signs in user passwordlessly")
    void testVerifyOtpSuccess() throws Exception {
        String email = "verify.scholar@makaut.ac.in";
        AuthController.SendOtpRequest sendReq = new AuthController.SendOtpRequest(
                email,
                "Scholar User",
                "signup",
                null
        );

        MvcResult result = mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendReq)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        String otp = objectMapper.readTree(responseJson).get("otp").asText();
        assertNotNull(otp);

        AuthController.VerifyOtpRequest verifyReq = new AuthController.VerifyOtpRequest(
                email,
                otp,
                "Scholar User",
                "CSE",
                "signup"
        );

        mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("CF-Connecting-IP", "203.0.113.195")
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.email", is(email)))
                .andExpect(jsonPath("$.user.role", is("STUDENT")));
    }

    @Test
    @DisplayName("Auth: Verify incorrect OTP returns error")
    void testVerifyOtpIncorrect() throws Exception {
        String email = "wrong.otp@makaut.ac.in";
        AuthController.SendOtpRequest sendReq = new AuthController.SendOtpRequest(email, null, "signin", null);

        mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendReq)))
                .andExpect(status().isOk());

        AuthController.VerifyOtpRequest verifyReq = new AuthController.VerifyOtpRequest(
                email,
                "000000", // Wrong OTP
                null,
                null,
                "signin"
        );

        mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Incorrect verification code")));
    }

    @Test
    @DisplayName("Cloudflare Security: Malicious path traversal is blocked (403 Forbidden)")
    void testCloudflareMaliciousPathBlocked() throws Exception {
        mockMvc.perform(get("/api/v1/resources/../../etc/passwd"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Cloudflare Security: Status endpoint returns protection info")
    void testCloudflareStatusEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/auth/status")
                        .header("CF-Connecting-IP", "104.21.45.67")
                        .header("CF-IPCountry", "IN")
                        .header("CF-Ray", "8492048cf109"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cloudflareProtected", is(true)))
                .andExpect(jsonPath("$.clientIp", is("104.21.45.67")))
                .andExpect(jsonPath("$.country", is("IN")))
                .andExpect(jsonPath("$.cfRay", is("8492048cf109")));
    }
}
