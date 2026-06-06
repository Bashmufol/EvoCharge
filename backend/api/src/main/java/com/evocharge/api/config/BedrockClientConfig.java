package com.evocharge.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.util.Optional;

@Configuration
public class BedrockClientConfig {

    @Bean
    Optional<BedrockRuntimeClient> optionalBedrockClient(
            @org.springframework.beans.factory.annotation.Autowired(required = false) BedrockRuntimeClient client) {
        return Optional.ofNullable(client);
    }
}
