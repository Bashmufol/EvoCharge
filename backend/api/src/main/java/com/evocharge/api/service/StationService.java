package com.evocharge.api.service;

import com.evocharge.api.dto.RecommendRequest;
import com.evocharge.api.dto.RecommendResponse;
import com.evocharge.api.model.Station;
import com.evocharge.api.model.StationStatus;
import com.evocharge.api.repository.StationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Station queries, nearby search, and recommendation orchestration. */
@Service
public class StationService {

    private final StationRepository stationRepository;
    private final GeoService geoService;
    private final EvoScoreService evoScoreService;

    public StationService(StationRepository stationRepository, GeoService geoService, EvoScoreService evoScoreService) {
        this.stationRepository = stationRepository;
        this.geoService = geoService;
        this.evoScoreService = evoScoreService;
    }

    public List<Station> findAll(String operatorId, StationStatus status, String connector, String search, String city) {
        return stationRepository.findFiltered(operatorId, status, connector, search, city);
    }

    public List<String> listCities() {
        return stationRepository.findAll().stream()
                .map(Station::getCity)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public Station findById(String id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Station not found: " + id));
    }

    public List<Station> findNearby(double lat, double lng, double radiusKm, String city) {
        return stationRepository.findAll().stream()
                .filter(s -> city == null || city.isBlank() || city.equalsIgnoreCase(s.getCity()))
                .filter(s -> geoService.withinRadius(lat, lng, s.getLat(), s.getLng(), radiusKm))
                .sorted(Comparator.comparingDouble(s -> geoService.distanceKm(lat, lng, s.getLat(), s.getLng())))
                .collect(Collectors.toList());
    }

    public RecommendResponse recommend(RecommendRequest request) {
        List<Station> nearby = findNearby(request.getLat(), request.getLng(), 15.0, null);
        if (nearby.isEmpty()) {
            nearby = stationRepository.findAll();
        }
        return evoScoreService.recommend(nearby, request);
    }

    public void updateStation(Station station) {
        stationRepository.save(station);
    }
}
