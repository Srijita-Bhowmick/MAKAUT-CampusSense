package com.makaut.campussense.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/CSE(3rd to 8th sem)/**")
                .addResourceLocations("classpath:/static/CSE(3rd to 8th sem)/");
        registry.addResourceHandler("/CSE-AIML(3rd to 8th sem)/**")
                .addResourceLocations("classpath:/static/CSE-AIML(3rd to 8th sem)/");
        registry.addResourceHandler("/IT(3rd to 8th sem)/**")
                .addResourceLocations("classpath:/static/IT(3rd to 8th sem)/");
    }
}
