package com.evocharge.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.location.LocationClient;

import java.util.Optional;

/** Registers AWS SDK clients when the matching {@code evocharge.*} feature flags are enabled. */
@Configuration
public class AwsConfig {

    @Bean
    @ConditionalOnProperty(name = "evocharge.storage", havingValue = "dynamodb")
    DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "evocharge.bedrock.enabled", havingValue = "true")
    BedrockRuntimeClient bedrockRuntimeClient() {
        return BedrockRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "evocharge.bedrock.enabled", havingValue = "true")
    LocationClient locationClient() {
        return LocationClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    /** Wraps the Bedrock client so advisor code can inject {@code Optional} when Bedrock is disabled. */
    @Bean
    Optional<BedrockRuntimeClient> optionalBedrockClient(@Autowired(required = false) BedrockRuntimeClient client) {
        return Optional.ofNullable(client);
    }
}
