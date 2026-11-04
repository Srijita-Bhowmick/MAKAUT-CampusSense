package com.makaut.campussense.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@SuppressWarnings("null")
public class SecurityConfig {

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/images/**", "/css/**", "/js/**", "/favicon.ico", "/assets/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login",
                                "/login.html",
                                "/privacy",
                                "/privacy.html",
                                "/styles.css",
                                "/app.js",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/assets/**",
                                "/resources.json",
                                "/makaut_knowledge_index.json",
                                "/favicon.ico",
                                "/docs/**",
                                "/3rd to 8th sem/**",
                                "/1st_year/**",
                                "/1st year/**",
                                "/CSBS(3rd to 8th sem)/**",
                                "/CSCS(3rd to 8th sem)/**",
                                "/CSDS(3rd to 8th sem)/**",
                                "/CSE(3rd to 8th sem)/**",
                                "/CSE-AIML(3rd to 8th sem)/**",
                                "/IT(3rd to 8th sem)/**",
                                "/*.html",
                                "/*.css",
                                "/*.js",
                                "/*.json",
                                "/*.pdf",
                                "/*.ico",
                                "/*.png",
                                "/*.jpg",
                                "/*.jpeg",
                                "/*.svg",
                                "/*.webp",
                                "/h2-console/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/resources",
                                "/api/resources/**",
                                "/api/v1/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "https://*.pages.dev",
                "https://campussense.in",
                "https://*.campussense.in",
                "http://localhost:8080",
                "http://127.0.0.1:8080",
                "http://localhost:5173"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers", "CF-Ray", "CF-Connecting-IP", "CF-IPCountry"));
        config.setExposedHeaders(List.of("CF-Ray", "X-Cloudflare-Protection", "Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
