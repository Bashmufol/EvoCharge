package com.evocharge.api.service;

import com.evocharge.api.dto.AnalyticsSummary;
import com.evocharge.api.dto.DemandArea;
import com.evocharge.api.model.Station;
import com.evocharge.api.model.StationStatus;
import com.evocharge.api.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final StationRepository stationRepository;

    private static final Map<String, double[]> AREA_CENTERS = Map.ofEntries(
            Map.entry("Victoria Island", new double[]{6.4281, 3.4219}),
            Map.entry("Lekki", new double[]{6.4474, 3.4700}),
            Map.entry("Ikoyi", new double[]{6.4541, 3.4316}),
            Map.entry("Lagos Island", new double[]{6.4549, 3.3942}),
            Map.entry("Ajah", new double[]{6.4683, 3.5852}),
            Map.entry("Yaba", new double[]{6.5155, 3.3713}),
            Map.entry("Surulere", new double[]{6.4969, 3.3550}),
            Map.entry("Ikeja", new double[]{6.5833, 3.3515}),
            Map.entry("Maryland", new double[]{6.5784, 3.3676}),
            Map.entry("Festac", new double[]{6.4700, 3.2800}),
            Map.entry("Maitama", new double[]{9.0820, 7.4951}),
            Map.entry("Wuse", new double[]{9.0765, 7.4898}),
            Map.entry("Garki", new double[]{9.0434, 7.4891}),
            Map.entry("Jabi", new double[]{9.0760, 7.4200}),
            Map.entry("Gwarinpa", new double[]{9.1180, 7.3980}),
            Map.entry("Central Area", new double[]{9.0579, 7.4891}),
            Map.entry("GRA Phase 2", new double[]{4.8156, 7.0134}),
            Map.entry("Trans Amadi", new double[]{4.8150, 7.0350}),
            Map.entry("Rumuola", new double[]{4.8420, 7.0050}),
            Map.entry("Airport", new double[]{9.0068, 7.2632}),
            Map.entry("Township", new double[]{4.7770, 7.0130}),
            Map.entry("Peter Odili", new double[]{4.8200, 7.0500}),
            Map.entry("Ada George", new double[]{4.8500, 4.9200}),
            Map.entry("Elelenwo", new double[]{4.8800, 7.0800})
    );

    public AnalyticsService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public AnalyticsSummary getSummary() {
        List<Station> stations = stationRepository.findAll();
        AnalyticsSummary summary = new AnalyticsSummary();
        summary.setTotalStations(stations.size());
        summary.setAvailable((int) stations.stream().filter(s -> s.getStatus() == StationStatus.AVAILABLE).count());
        summary.setBusy((int) stations.stream().filter(s -> s.getStatus() == StationStatus.BUSY).count());
        summary.setOffline((int) stations.stream().filter(s -> s.getStatus() == StationStatus.OFFLINE).count());

        int online = summary.getAvailable() + summary.getBusy();
        summary.setUtilizationPercent(online == 0 ? 0 :
                Math.round((summary.getBusy() * 100.0 / online) * 10.0) / 10.0);

        double avgWait = stations.stream()
                .filter(s -> s.getStatus() != StationStatus.OFFLINE)
                .mapToInt(Station::getWaitMinutes)
                .average()
                .orElse(0);
        summary.setAvgWaitMinutes(Math.round(avgWait * 10.0) / 10.0);
        summary.setPeakHour(17);

        Map<String, Integer> byOp = stations.stream()
                .collect(Collectors.groupingBy(Station::getOperatorName, LinkedHashMap::new, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        summary.setByOperator(byOp);
        return summary;
    }

    public List<DemandArea> getDemandByArea() {
        return getDemandByArea(null);
    }

    public List<DemandArea> getDemandByArea(String city) {
        List<Station> stations = stationRepository.findAll().stream()
                .filter(s -> city == null || city.isBlank() || city.equalsIgnoreCase(s.getCity()))
                .toList();

        Map<String, List<Station>> byArea = stations.stream()
                .collect(Collectors.groupingBy(s -> s.getCity() + "|" + s.getArea(), LinkedHashMap::new, Collectors.toList()));

        List<DemandArea> areas = new ArrayList<>();
        byArea.forEach((key, areaStations) -> {
            Station first = areaStations.get(0);
            DemandArea da = new DemandArea();
            da.setCity(first.getCity());
            da.setArea(first.getArea());
            double[] center = AREA_CENTERS.getOrDefault(first.getArea(), new double[]{
                    areaStations.get(0).getLat(), areaStations.get(0).getLng()
            });
            da.setLat(center[0]);
            da.setLng(center[1]);
            da.setStationCount(areaStations.size());

            long busy = areaStations.stream().filter(s -> s.getStatus() == StationStatus.BUSY).count();
            long offline = areaStations.stream().filter(s -> s.getStatus() == StationStatus.OFFLINE).count();
            double demand = (busy * 30.0) + (areaStations.size() * 10.0) + (offline * 5.0);
            da.setDemandScore(Math.min(100, Math.round(demand)));
            da.setUnmetDemand(da.getDemandScore() > 55 && areaStations.size() < 4);
            da.setEstimatedDailySessions((int) (da.getDemandScore() * 2.5));
            areas.add(da);
        });

        areas.sort((a, b) -> Double.compare(b.getDemandScore(), a.getDemandScore()));
        return areas;
    }
}
