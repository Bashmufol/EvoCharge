package com.evocharge.api.service;

import com.evocharge.api.config.EvoChargeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Component
public class MistralAdvisorClient {

    private static final Logger log = LoggerFactory.getLogger(MistralAdvisorClient.class);

    private final EvoChargeProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public MistralAdvisorClient(EvoChargeProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Optional<String> complete(String prompt) {
        EvoChargeProperties.Mistral mistral = properties.getMistral();
        if (!mistral.isEnabled() || mistral.getApiKey() == null || mistral.getApiKey().isBlank()) {
            return Optional.empty();
        }

        try {
            String body = objectMapper.writeValueAsString(objectMapper.createObjectNode()
                    .put("model", mistral.getModel())
                    .put("max_tokens", 300)
                    .set("messages", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode()
                                    .put("role", "user")
                                    .put("content", prompt))));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mistral.getBaseUrl() + "/chat/completions"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + mistral.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Mistral API returned status {}: {}", response.statusCode(), response.body());
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(content.asText().trim());
        } catch (Exception e) {
            log.warn("Mistral advisor call failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
