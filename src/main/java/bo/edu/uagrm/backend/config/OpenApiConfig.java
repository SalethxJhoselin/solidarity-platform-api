package bo.edu.uagrm.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Enterprise OpenAPI 3.0 configuration for Swagger UI and API specifications.
 * Provides interactive documentation and JWT Bearer authorization schema.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("UAGRM Solidarity Community Platform - Enterprise REST API")
                        .version("1.0.0")
                        .description("Production-grade RESTful API for university mutual aid, community volunteering, donation drives, algorithmic matching, and content moderation.")
                        .contact(new Contact()
                                .name("UAGRM Engineering Team")
                                .email("contacto@uagrm.edu.bo")
                                .url("https://ficct.uagrm.edu.bo"))
                        .license(new License()
                                .name("Proprietary / University Degree License")
                                .url("https://uagrm.edu.bo/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api/v1").description("Local Development Server (API v1)"),
                        new Server().url("/api/v1").description("Direct Relative API Endpoint")
                ))
                .tags(List.of(
                        new Tag().name("Authentication").description("Registration, JWT authentication, and credential recovery"),
                        new Tag().name("Publications").description("Help requests, volunteering offers, donation items, and collective activities"),
                        new Tag().name("Donations").description("Confirmed donation fulfillment and state resolution"),
                        new Tag().name("Comments").description("Community feedback and discussion threads"),
                        new Tag().name("Reports & Moderation").description("Community safety, offensive content flags, and administrative triage"),
                        new Tag().name("Tracking & Timeline").description("Lifecycle progression and milestone history"),
                        new Tag().name("Smart Matching").description("Algorithmic recommendation engine for community needs"),
                        new Tag().name("Administration").description("User roles, system settings, and audit logs")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token obtained from `/auth/login` to authenticate requests.")));
    }
}
