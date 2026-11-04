package bo.edu.uagrm.backend.config;

import bo.edu.uagrm.backend.security.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Centralized Spring MVC enterprise configuration.
 *
 * 1. Global API Prefix: Automatically prepends /api/v1 to all @RestController endpoints (DRY).
 * 2. Argument Resolvers: Registers custom parameter resolvers like @CurrentUser for seamless security context binding.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    public static final String API_V1_PREFIX = "/api/v1";

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    public WebMvcConfig(CurrentUserArgumentResolver currentUserArgumentResolver) {
        this.currentUserArgumentResolver = currentUserArgumentResolver;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(API_V1_PREFIX, HandlerTypePredicate.forBasePackage("bo.edu.uagrm.backend.controllers"));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}
