package com.example.bankcards.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = "JWT Bearer",
    description = "JWT in header",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
            .components(new Components()
                    .addResponses("NotFound", createApiResponse("Entity not found"))
                    .addResponses("401", createApiResponse("Authentication required"))
                    .addResponses("403", createApiResponse("Access denied"))
                    .addResponses("Conflict", createApiResponse("Conflict"))
                    .addResponses("Validation", createApiResponse("Validation error"))
            )
            .info(new Info()
                .title("Bank Cards management system")
                .version("0.0.1")
                .description("API Description for Bank Cards management system")
                .contact(new Contact().name("Alex VM").email("my@email.com")));
    }

    private ApiResponse createApiResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                new MediaType()
                                        .schema(new Schema<>()
                                                .$ref("#/components/schemas/ErrorResponseSchema"))));
    }
}
