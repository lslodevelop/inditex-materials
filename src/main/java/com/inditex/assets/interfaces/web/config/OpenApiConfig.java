package com.inditex.assets.interfaces.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI assetServiceOpenAPI() {
        final io.swagger.v3.oas.models.info.Info info = new Info()
                .title("Assets Management API")
                .version("1.0")
                .description("API for Inditex code review.");
        return new OpenAPI().info(info);
    }
}