package com.makaut.campussense.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Cloudflare Security & Attack Prevention Filter.
 * - Extracts real visitor IP via CF-Connecting-IP
 * - Blocks known vulnerability probes, SQLi, and path traversal attacks
 * - Injects hardened security headers aligned with Cloudflare WAF best practices
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@SuppressWarnings("all")
public class CloudflareSecurityFilter extends OncePerRequestFilter {

    public static final String CF_CLIENT_IP_ATTR = "CF_CLIENT_IP";
    public static final String CF_COUNTRY_ATTR = "CF_IPCOUNTRY";
    public static final String CF_RAY_ATTR = "CF_RAY";

    private static final Pattern MALICIOUS_URI_PATTERN = Pattern.compile(
            "(?i)(\\.\\./|\\.\\.\\\\|/etc/passwd|/proc/|/win\\.ini|<script|javascript:|union\\s+select|benchmark\\(|waitfor\\s+delay)"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Resolve Genuine Client IP from Cloudflare Header
        String clientIp = resolveClientIp(request);
        String country = request.getHeader("CF-IPCountry");
        String cfRay = request.getHeader("CF-Ray");

        request.setAttribute(CF_CLIENT_IP_ATTR, clientIp);
        if (country != null) request.setAttribute(CF_COUNTRY_ATTR, country);
        if (cfRay != null) request.setAttribute(CF_RAY_ATTR, cfRay);

        // 2. Attack Detection: Block Malicious Path Traversal and Injection Probes
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String combinedPath = uri + (queryString != null ? "?" + queryString : "");

        if (MALICIOUS_URI_PATTERN.matcher(combinedPath).find()) {
            log.warn("[Cloudflare WAF Blocked] Malicious pattern detected in URI from IP: {} (Path: {})", clientIp, combinedPath);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Request blocked by Cloudflare Security WAF Guardrail.\"}");
            return;
        }

        // 3. Inject Cloudflare-Hardened Defense Headers
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        response.setHeader("X-Cloudflare-Protection", "Turnstile-Enabled; WAF-Active");

        String cfVisitor = request.getHeader("CF-Visitor");
        String xForwardedProto = request.getHeader("X-Forwarded-Proto");
        if ((cfVisitor != null && cfVisitor.contains("https")) ||
                (xForwardedProto != null && xForwardedProto.equalsIgnoreCase("https")) ||
                request.isSecure()) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        }

        if (cfRay != null) {
            response.setHeader("CF-Ray", cfRay);
        }

        filterChain.doFilter(request, response);
    }

    public static String resolveClientIp(HttpServletRequest request) {
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isBlank()) {
            return cfConnectingIp.trim();
        }

        String trueClientIp = request.getHeader("True-Client-IP");
        if (trueClientIp != null && !trueClientIp.isBlank()) {
            return trueClientIp.trim();
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
