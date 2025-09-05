package com.weeklyreport.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the Weekly Report System API
 * Provides comprehensive API documentation with security configuration
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                    new Server()
                        .url("http://localhost:" + serverPort + contextPath)
                        .description("Development server"),
                    new Server()
                        .url("https://api-test.weekly-report.example.com")
                        .description("Test server"),
                    new Server()
                        .url("https://api.weekly-report.example.com")
                        .description("Production server")
                ))
                .components(new Components()
                    .addSecuritySchemes("JWT", jwtSecurityScheme())
                )
                .addSecurityItem(new SecurityRequirement().addList("JWT"));
    }

    private Info apiInfo() {
        return new Info()
                .title("Weekly Report System API")
                .description("""
                    Comprehensive REST API for the Weekly Report Management System.
                    
                    ## Features
                    - User authentication and authorization with JWT
                    - Weekly report creation, management, and approval workflow
                    - Project management with member roles and permissions
                    - Dashboard analytics and reporting
                    - File upload and attachment management
                    - Real-time notifications
                    
                    ## Authentication
                    This API uses JWT (JSON Web Token) for authentication. Include the token in the Authorization header:
                    `Authorization: Bearer <your-jwt-token>`
                    
                    ## Rate Limiting
                    - Authenticated requests: 1000 requests per hour per user
                    - Authentication requests: 10 requests per minute per IP
                    - File uploads: 20 requests per minute per user
                    
                    ## Error Handling
                    All error responses follow a consistent format with HTTP status codes and detailed error messages.
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Weekly Report System Team")
                    .email("support@weekly-report.example.com")
                    .url("https://github.com/weekly-report/backend"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT"));
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .name("JWT")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("""
                    JWT Authorization header using the Bearer scheme.
                    
                    Enter 'Bearer' [space] and then your token in the text input below.
                    
                    Example: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    """);
    }
}