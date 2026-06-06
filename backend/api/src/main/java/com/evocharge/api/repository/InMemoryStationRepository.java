package com.evocharge.api.repository;

import com.evocharge.api.model.Station;
import com.evocharge.api.model.StationStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "evocharge.storage", havingValue = "local", matchIfMissing = true)
public class InMemoryStationRepository implements StationRepository {

    private final Map<String, Station> stations = new ConcurrentHashMap<>();

    @Override
    public List<Station> findAll() {
        return new ArrayList<>(stations.values());
    }

    @Override
    public Optional<Station> findById(String id) {
        return Optional.ofNullable(stations.get(id));
    }

    @Override
    public void saveAll(List<Station> stationList) {
        stationList.forEach(s -> stations.put(s.getId(), s));
    }

    @Override
    public void save(Station station) {
        stations.put(station.getId(), station);
    }

    @Override
    public List<Station> findFiltered(String operatorId, StationStatus status, String connector, String search, String city) {
        return stations.values().stream()
                .filter(s -> operatorId == null || operatorId.isBlank() || operatorId.equals(s.getOperatorId()))
                .filter(s -> status == null || status == s.getStatus())
                .filter(s -> connector == null || connector.isBlank()
                        || s.getConnectors().stream().anyMatch(c -> c.equalsIgnoreCase(connector)))
                .filter(s -> city == null || city.isBlank()
                        || city.equalsIgnoreCase(s.getCity()))
                .filter(s -> search == null || search.isBlank()
                        || s.getName().toLowerCase().contains(search.toLowerCase())
                        || s.getArea().toLowerCase().contains(search.toLowerCase())
                        || (s.getCity() != null && s.getCity().toLowerCase().contains(search.toLowerCase()))
                        || s.getAddress().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
    }
}
