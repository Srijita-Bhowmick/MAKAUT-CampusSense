package com.makaut.campussense.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/privacy").setViewName("forward:/privacy.html");
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 1. Heavy immutable assets (*.pdf, fonts, vendor icons) -> Cache-Control: public, max-age=31536000, immutable
        CacheControl immutableControl = CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable();

        registry.addResourceHandler("/*.pdf", "/*.ico", "/*.svg", "/*.woff", "/*.woff2", "/*.ttf", "/*.png", "/*.jpg", "/*.jpeg", "/*.webp")
                .addResourceLocations("file:docs/", "classpath:/static/")
                .setCacheControl(immutableControl);

        registry.addResourceHandler("/assets/**", "/docs/**")
                .addResourceLocations("file:docs/", "classpath:/static/", "classpath:/static/assets/")
                .setCacheControl(immutableControl);

        registry.addResourceHandler("/assets/resources/1st_year/pyq/**")
                .addResourceLocations("file:docs/1st year/pyqs/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/assets/resources/1st_year/syllabus/**")
                .addResourceLocations("file:docs/1st year/syllabus/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/1st year/**")
                .addResourceLocations("file:docs/1st year/", "classpath:/static/1st year/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/1st_year/**")
                .addResourceLocations("file:docs/1st year/", "classpath:/static/1st year/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/3rd to 8th sem/**")
                .addResourceLocations("file:docs/3rd to 8th sem/", "classpath:/static/3rd to 8th sem/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/cscs/**")
                .addResourceLocations("file:docs/3rd to 8th sem/cscs/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/csbs/**")
                .addResourceLocations("file:docs/3rd to 8th sem/csbs/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/csds/**")
                .addResourceLocations("file:docs/3rd to 8th sem/csds/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/cse/**")
                .addResourceLocations("file:docs/3rd to 8th sem/cse/", "file:docs/3rd to 8th sem/cse/syllabus/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/cse-aiml/**")
                .addResourceLocations("file:docs/3rd to 8th sem/cse-aiml/", "file:docs/3rd to 8th sem/cse-aiml/syllabus/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/it/**")
                .addResourceLocations("file:docs/3rd to 8th sem/it/", "file:docs/3rd to 8th sem/it/syllabus/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/CSBS(3rd to 8th sem)/**")
                .addResourceLocations("file:docs/3rd to 8th sem/csbs/", "file:docs/3rd to 8th sem/csbs/syllabus/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/CSCS(3rd to 8th sem)/**")
                .addResourceLocations("file:docs/3rd to 8th sem/cscs/", "file:docs/3rd to 8th sem/cscs/syllabus/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/CSDS(3rd to 8th sem)/**")
                .addResourceLocations("file:docs/3rd to 8th sem/csds/", "file:docs/3rd to 8th sem/csds/syllabus/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/CSE(3rd to 8th sem)/**")
                .addResourceLocations("file:docs/3rd to 8th sem/cse/", "file:docs/3rd to 8th sem/cse/syllabus/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/CSE-AIML(3rd to 8th sem)/**")
                .addResourceLocations("file:docs/3rd to 8th sem/cse-aiml/", "file:docs/3rd to 8th sem/cse-aiml/syllabus/")
                .setCacheControl(immutableControl);
        registry.addResourceHandler("/IT(3rd to 8th sem)/**")
                .addResourceLocations("file:docs/3rd to 8th sem/it/", "file:docs/3rd to 8th sem/it/syllabus/")
                .setCacheControl(immutableControl);

        // 2. Static structural catalogs (resources.json, makaut_knowledge_index.json)
        CacheControl catalogControl = CacheControl.maxAge(3600, TimeUnit.SECONDS)
                .cachePublic()
                .staleWhileRevalidate(86400, TimeUnit.SECONDS);

        registry.addResourceHandler("/resources.json", "/makaut_knowledge_index.json")
                .addResourceLocations("file:docs/", "classpath:/static/")
                .setCacheControl(catalogControl);

        // 3. Core entry points (index.html, login.html, app.js, styles.css) -> Cache-Control: public, no-cache, must-revalidate
        CacheControl entryPointControl = CacheControl.noCache()
                .mustRevalidate()
                .cachePublic();

        registry.addResourceHandler("/index.html", "/login.html", "/privacy.html", "/app.js", "/styles.css")
                .addResourceLocations("file:docs/", "classpath:/static/")
                .setCacheControl(entryPointControl);

        // 4. Default fallback
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "file:docs/");
    }
}
