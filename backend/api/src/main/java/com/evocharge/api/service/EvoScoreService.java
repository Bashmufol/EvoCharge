package com.evocharge.api.service;

import com.evocharge.api.dto.RecommendRequest;
import com.evocharge.api.dto.RecommendResponse;
import com.evocharge.api.dto.RecommendResponse.RankedStation;
import com.evocharge.api.model.Station;
import com.evocharge.api.model.StationStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Ranks stations by distance, availability, wait time, reliability, and connector match. */
@Service
public class EvoScoreService {

    private final GeoService geoService;

    public EvoScoreService(GeoService geoService) {
        this.geoService = geoService;
    }

    public double calculate(Station station, double userLat, double userLng, String connectorType) {
        double distance = geoService.distanceKm(userLat, userLng, station.getLat(), station.getLng());
        double distanceScore = Math.max(0, 100 - (distance * 15));
        double availabilityScore = switch (station.getStatus()) {
            case AVAILABLE -> 100;
            case BUSY -> 40;
            case OFFLINE -> 0;
        };
        double waitScore = Math.max(0, 100 - (station.getWaitMinutes() * 4));
        double reliabilityScore = station.getReliabilityScore();
        double connectorScore = station.getConnectors().stream()
                .anyMatch(c -> c.equalsIgnoreCase(connectorType)) ? 100 : 30;

        return distanceScore * 0.30
                + availabilityScore * 0.25
                + waitScore * 0.20
                + reliabilityScore * 0.15
                + connectorScore * 0.10;
    }

    public RecommendResponse recommend(List<Station> stations, RecommendRequest request) {
        RecommendResponse response = new RecommendResponse();
        List<RankedStation> ranked = stations.stream()
                .filter(s -> s.getStatus() != StationStatus.OFFLINE)
                .map(s -> {
                    double score = calculate(s, request.getLat(), request.getLng(), request.getConnectorType());
                    double dist = geoService.distanceKm(request.getLat(), request.getLng(), s.getLat(), s.getLng());
                    int eta = geoService.estimateEtaMinutes(dist);
                    s.setEvoScore(Math.round(score * 10.0) / 10.0);

                    RankedStation rs = new RankedStation();
                    rs.setStation(s);
                    rs.setEvoScore(s.getEvoScore());
                    rs.setDistanceKm(Math.round(dist * 100.0) / 100.0);
                    rs.setEtaMinutes(eta);
                    rs.setReason(buildReason(s, dist, eta));
                    return rs;
                })
                .sorted(Comparator.comparingDouble(RankedStation::getEvoScore).reversed())
                .limit(3)
                .collect(Collectors.toList());
        response.setRecommendations(ranked);
        return response;
    }

    private String buildReason(Station s, double dist, int eta) {
        return String.format("%s - %.1f km away, ~%d min drive, %d min wait, %d%% reliable",
                s.getStatus() == StationStatus.AVAILABLE ? "Available now" : "Busy but viable",
                dist, eta, s.getWaitMinutes(), s.getReliabilityScore());
    }
}
