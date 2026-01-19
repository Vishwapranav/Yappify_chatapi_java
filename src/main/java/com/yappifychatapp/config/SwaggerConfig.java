package com.yappifychatapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI chatApiOpenAPI() {
        // Server configuration
        Server localServer = new Server();
        localServer.setUrl("http://localhost:5050");
        localServer.setDescription("Local Server");

        // Contact information
        Contact contact = new Contact();
        contact.setName("Yappify Support");
        contact.setEmail("support@yappify.com");

        // License
        License license = new License();
        license.setName("MIT License");
        license.setUrl("https://opensource.org/licenses/MIT");

        // API Info
        Info info = new Info()
                .title("Yappify Chat API")
                .version("1.0.0")
                .description("Real-time chat application API with user management, messaging, and group chat features")
                .contact(contact)
                .license(license);

        // Security Schemes
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT Authorization header using the Bearer scheme. Example: 'Bearer {token}'");

        SecurityScheme userIdHeader = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("userId")
                .description("User ID header required for authenticated requests");

        // Security Requirements
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication")
                .addList("User ID Header");

        // Components
        Components components = new Components()
                .addSecuritySchemes("Bearer Authentication", bearerAuth)
                .addSecuritySchemes("User ID Header", userIdHeader);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}