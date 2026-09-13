package com.makaut.campussense.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI makautCampusSenseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MAKAUT CampusSense API")
                        .description("Enterprise Academic Resource Aggregator & Conversational RAG Engine for Maulana Abul Kalam Azad University of Technology (MAKAUT)")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("MAKAUT CampusSense Development Team")
                                .url("https://makautwb.ac.in")
                                .email("campussense@makaut.ac.in"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("MAKAUT Official Portal & Examinations")
                        .url("https://makautexam.net"));
    }
}
