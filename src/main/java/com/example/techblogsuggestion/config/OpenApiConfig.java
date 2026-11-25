package com.example.techblogsuggestion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI techDigestOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tech Digest Engine API")
                        .description("REST API for managing RSS feed sources, triggering content ingestion pipelines, and retrieving summarized technical digests.")
                        .version("v1.0.0"));
    }
}