package com.evocharge.api.service;

import com.evocharge.api.config.EvoChargeProperties;
import com.evocharge.api.model.GridStatus;
import com.evocharge.api.model.Operator;
import com.evocharge.api.model.Station;
import com.evocharge.api.model.StationStatus;
import com.evocharge.api.repository.OperatorRepository;
import com.evocharge.api.repository.StationRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final StationRepository stationRepository;
    private final OperatorRepository operatorRepository;
    private final EvoChargeProperties properties;
    private final ObjectMapper objectMapper;

    public DataSeeder(StationRepository stationRepository,
                      OperatorRepository operatorRepository,
                      EvoChargeProperties properties,
                      ObjectMapper objectMapper) {
        this.stationRepository = stationRepository;
        this.operatorRepository = operatorRepository;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void seed() {
        boolean empty = stationRepository.findAll().isEmpty();
        if (!empty && !properties.getSeed().isResyncOnStartup()) {
            return;
        }
        try {
            Path seedPath = resolveSeedPath();
            List<Operator> operators = loadOperators(seedPath.resolve("operators.json"));
            List<Station> stations = loadStations(seedPath.resolve("stations.json"));

            operatorRepository.saveAll(operators);
            stationRepository.saveAll(stations);
            if (empty) {
                log.info("Seeded {} operators and {} stations from {}", operators.size(), stations.size(), seedPath);
            } else {
                log.info("Resynced {} operators and {} stations from {}", operators.size(), stations.size(), seedPath);
            }
        } catch (IOException e) {
            log.error("Failed to seed data: {}", e.getMessage());
        }
    }

    private Path resolveSeedPath() {
        Path configured = Path.of(properties.getSeedPath()).toAbsolutePath().normalize();
        if (Files.exists(configured.resolve("stations.json"))) {
            return configured;
        }
        Path[] candidates = {
                Path.of("../../../data/seed").toAbsolutePath().normalize(),
                Path.of("../../data/seed").toAbsolutePath().normalize(),
                Path.of("/app/data/seed")
        };
        for (Path candidate : candidates) {
            if (Files.exists(candidate.resolve("stations.json"))) {
                return candidate;
            }
        }
        return configured;
    }

    private List<Operator> loadOperators(Path path) throws IOException {
        return objectMapper.readValue(path.toFile(), new TypeReference<List<Operator>>() {});
    }

    private List<Station> loadStations(Path path) throws IOException {
        List<Map<String, Object>> raw = objectMapper.readValue(path.toFile(), new TypeReference<>() {});
        return raw.stream().map(this::mapStation).toList();
    }

    private Station mapStation(Map<String, Object> raw) {
        Station s = new Station();
        s.setId((String) raw.get("id"));
        s.setName((String) raw.get("name"));
        s.setOperatorId((String) raw.get("operatorId"));
        s.setOperatorName((String) raw.get("operatorName"));
        s.setLat(((Number) raw.get("lat")).doubleValue());
        s.setLng(((Number) raw.get("lng")).doubleValue());
        s.setAddress((String) raw.get("address"));
        s.setCity(raw.containsKey("city") ? (String) raw.get("city") : "Lagos");
        s.setArea((String) raw.get("area"));
        s.setStatus(StationStatus.valueOf((String) raw.get("status")));
        s.setConnectors(objectMapper.convertValue(raw.get("connectors"), new TypeReference<List<String>>() {}));
        s.setPowerKw(((Number) raw.get("powerKw")).intValue());
        s.setWaitMinutes(((Number) raw.get("waitMinutes")).intValue());
        s.setReliabilityScore(((Number) raw.get("reliabilityScore")).intValue());
        s.setGridStatus(GridStatus.valueOf((String) raw.get("gridStatus")));
        s.setLastUpdated(Instant.parse((String) raw.get("lastUpdated")));
        s.setEvoScore(0);
        return s;
    }
}
