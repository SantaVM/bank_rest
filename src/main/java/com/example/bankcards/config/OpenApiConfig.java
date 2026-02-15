package com.example.bankcards.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

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

        // ---- Security requirement (applied globally) ----
//        SecurityRequirement securityRequirement = new SecurityRequirement()
//                .addList("JWT Bearer");

        return new OpenAPI()
            .info(new Info()
                .title("Bank Cards management system")
                .version("0.0.1")
                .description("API Description for Bank Cards management system")
                .contact(new Contact().name("Alex VM").email("my@email.com")));
    }
}
