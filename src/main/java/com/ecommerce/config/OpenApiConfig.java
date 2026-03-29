package com.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        // Define JWT Bearer auth scheme for Swagger UI
        SecurityScheme jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("JWT Authentication");

        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce API")
                        .description("Spring Boot e-commerce REST API with JWT authentication, " +
                                "role-based access control, and OAuth2 social login.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("She Can Code")
                                .email("shecancode@example.com")))
                .addSecurityItem(new SecurityRequirement().addList("JWT Authentication"))
                .components(new Components()
                        .addSecuritySchemes("JWT Authentication", jwtScheme));
    }
}
