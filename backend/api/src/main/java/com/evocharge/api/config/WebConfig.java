package com.evocharge.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** CORS rules for browser clients calling {@code /api/**}. */
@Configuration
@EnableConfigurationProperties(EvoChargeProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final EvoChargeProperties properties;

    public WebConfig(EvoChargeProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = properties.getCorsOrigins().split(",");
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
