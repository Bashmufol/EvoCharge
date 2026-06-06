package com.evocharge.api.service;

import com.evocharge.api.config.EvoChargeProperties;
import com.evocharge.api.dto.AdvisorRequest;
import com.evocharge.api.dto.AdvisorResponse;
import com.evocharge.api.dto.RecommendRequest;
import com.evocharge.api.dto.RecommendResponse.RankedStation;
import com.evocharge.api.model.StationStatus;
import com.evocharge.api.repository.StationRepository;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class AdvisorService {

    private final StationService stationService;
    private final EvoChargeProperties properties;
    private final Optional<BedrockRuntimeClient> bedrockClient;

    public AdvisorService(StationService stationService,
                          EvoChargeProperties properties,
                          Optional<BedrockRuntimeClient> bedrockClient) {
        this.stationService = stationService;
        this.properties = properties;
        this.bedrockClient = bedrockClient;
    }

    public AdvisorResponse advise(AdvisorRequest request) {
        RecommendRequest rec = new RecommendRequest();
        rec.setLat(request.getLat());
        rec.setLng(request.getLng());

        String query = request.getQuery().toLowerCase();
        if (query.contains("ccs") || query.contains("fast")) {
            rec.setConnectorType("CCS2");
        } else if (query.contains("type 2") || query.contains("type2")) {
            rec.setConnectorType("Type2");
        }

        if (query.contains("low battery") || query.contains("20%") || query.contains("15%")) {
            rec.setBatteryPercent(20);
        } else if (query.contains("half") || query.contains("50%")) {
            rec.setBatteryPercent(50);
        }

        var recommendations = stationService.recommend(rec);
        List<RankedStation> stations = recommendations.getRecommendations();

        AdvisorResponse response = new AdvisorResponse();
        response.setStations(stations);

        if (properties.getBedrock().isEnabled() && bedrockClient.isPresent()) {
            response.setAnswer(invokeBedrock(request.getQuery(), stations));
        } else {
            response.setAnswer(buildFallbackAnswer(request.getQuery(), stations));
        }
        return response;
    }

    private String invokeBedrock(String query, List<RankedStation> stations) {
        try {
            String stationSummary = stations.stream()
                    .map(rs -> rs.getStation().getName() + " (" + rs.getStation().getStatus() + ", wait " + rs.getStation().getWaitMinutes() + "min)")
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("none");

            String prompt = """
                    You are EvoCharge AI Advisor for Nigerian EV drivers. Answer concisely in 2-3 sentences.
                    User query: %s
                    Top stations: %s
                    Mention wait times and availability. Be helpful and specific to Lagos, Nigeria.
                    """.formatted(query, stationSummary);

            String body = """
                    {"anthropic_version":"bedrock-2023-05-31","max_tokens":300,"messages":[{"role":"user","content":"%s"}]}
                    """.formatted(prompt.replace("\"", "\\\"").replace("\n", " "));

            InvokeModelResponse modelResponse = bedrockClient.get().invokeModel(InvokeModelRequest.builder()
                    .modelId(properties.getBedrock().getModelId())
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromString(body, StandardCharsets.UTF_8))
                    .build());

            String raw = modelResponse.body().asString(StandardCharsets.UTF_8);
            int contentIdx = raw.indexOf("\"text\":");
            if (contentIdx > 0) {
                int start = raw.indexOf('"', contentIdx + 7) + 1;
                int end = raw.indexOf('"', start);
                return raw.substring(start, end);
            }
        } catch (Exception ignored) {
            // fall through to fallback
        }
        return buildFallbackAnswer(query, stations);
    }

    private String buildFallbackAnswer(String query, List<RankedStation> stations) {
        if (stations.isEmpty()) {
            return "I couldn't find available charging stations matching your request. Try expanding your search radius or checking back shortly as our Network Pulse updates station status in real time.";
        }
        RankedStation top = stations.get(0);
        String availability = top.getStation().getStatus() == StationStatus.AVAILABLE ? "available now" : "busy but your best option";
        return String.format(
                "Based on your query \"%s\", I recommend %s — it's %.1f km away with an EvoScore of %.0f. " +
                        "The station is %s with approximately %d minutes wait. %s",
                query,
                top.getStation().getName(),
                top.getDistanceKm(),
                top.getEvoScore(),
                availability,
                top.getStation().getWaitMinutes(),
                stations.size() > 1
                        ? String.format("Alternatives include %s and %s.",
                        stations.get(1).getStation().getName(),
                        stations.size() > 2 ? stations.get(2).getStation().getName() : "none")
                        : ""
        );
    }
}
