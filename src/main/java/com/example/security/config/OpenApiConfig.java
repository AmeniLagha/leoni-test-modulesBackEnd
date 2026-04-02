package com.example.security.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gestion Industrielle")
                        .version("1.0")
                        .description("Documentation de l'application de gestion des cahiers de charges, maintenance, stock et utilisateurs"));
    }
}