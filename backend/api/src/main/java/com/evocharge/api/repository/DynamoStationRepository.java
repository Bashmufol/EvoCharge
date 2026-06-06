package com.evocharge.api.repository;

import com.evocharge.api.config.EvoChargeProperties;
import com.evocharge.api.model.GridStatus;
import com.evocharge.api.model.Station;
import com.evocharge.api.model.StationStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "evocharge.storage", havingValue = "dynamodb")
public class DynamoStationRepository implements StationRepository {

    private final DynamoDbTable<StationRecord> table;

    public DynamoStationRepository(DynamoDbClient dynamoDbClient, EvoChargeProperties properties) {
        DynamoDbEnhancedClient enhanced = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.table = enhanced.table(properties.getDynamodb().getStationsTable(), TableSchema.fromBean(StationRecord.class));
    }

    @Override
    public List<Station> findAll() {
        return table.scan().items().stream().map(this::toModel).collect(Collectors.toList());
    }

    @Override
    public Optional<Station> findById(String id) {
        StationRecord record = table.getItem(Key.builder().partitionValue(id).build());
        return Optional.ofNullable(record).map(this::toModel);
    }

    @Override
    public void saveAll(List<Station> stations) {
        stations.forEach(this::save);
    }

    @Override
    public void save(Station station) {
        table.putItem(fromModel(station));
    }

    @Override
    public List<Station> findFiltered(String operatorId, StationStatus status, String connector, String search, String city) {
        return findAll().stream()
                .filter(s -> operatorId == null || operatorId.isBlank() || operatorId.equals(s.getOperatorId()))
                .filter(s -> status == null || status == s.getStatus())
                .filter(s -> connector == null || connector.isBlank()
                        || s.getConnectors().stream().anyMatch(c -> c.equalsIgnoreCase(connector)))
                .filter(s -> city == null || city.isBlank()
                        || city.equalsIgnoreCase(s.getCity()))
                .filter(s -> search == null || search.isBlank()
                        || s.getName().toLowerCase().contains(search.toLowerCase())
                        || s.getArea().toLowerCase().contains(search.toLowerCase())
                        || (s.getCity() != null && s.getCity().toLowerCase().contains(search.toLowerCase())))
                .collect(Collectors.toList());
    }

    private Station toModel(StationRecord r) {
        Station s = new Station();
        s.setId(r.getId());
        s.setName(r.getName());
        s.setOperatorId(r.getOperatorId());
        s.setOperatorName(r.getOperatorName());
        s.setLat(r.getLat());
        s.setLng(r.getLng());
        s.setAddress(r.getAddress());
        s.setCity(r.getCity() != null ? r.getCity() : "Lagos");
        s.setArea(r.getArea());
        s.setStatus(StationStatus.valueOf(r.getStatus()));
        s.setConnectors(r.getConnectors());
        s.setPowerKw(r.getPowerKw());
        s.setWaitMinutes(r.getWaitMinutes());
        s.setReliabilityScore(r.getReliabilityScore());
        s.setGridStatus(GridStatus.valueOf(r.getGridStatus()));
        s.setEvoScore(r.getEvoScore());
        s.setLastUpdated(r.getLastUpdated() != null ? Instant.parse(r.getLastUpdated()) : Instant.now());
        return s;
    }

    private StationRecord fromModel(Station s) {
        StationRecord r = new StationRecord();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setOperatorId(s.getOperatorId());
        r.setOperatorName(s.getOperatorName());
        r.setLat(s.getLat());
        r.setLng(s.getLng());
        r.setAddress(s.getAddress());
        r.setCity(s.getCity());
        r.setArea(s.getArea());
        r.setStatus(s.getStatus().name());
        r.setConnectors(s.getConnectors());
        r.setPowerKw(s.getPowerKw());
        r.setWaitMinutes(s.getWaitMinutes());
        r.setReliabilityScore(s.getReliabilityScore());
        r.setGridStatus(s.getGridStatus().name());
        r.setEvoScore(s.getEvoScore());
        r.setLastUpdated(s.getLastUpdated() != null ? s.getLastUpdated().toString() : Instant.now().toString());
        return r;
    }

    @DynamoDbBean
    public static class StationRecord {
        private String id;
        private String name;
        private String operatorId;
        private String operatorName;
        private double lat;
        private double lng;
        private String address;
        private String city;
        private String area;
        private String status;
        private List<String> connectors;
        private int powerKw;
        private int waitMinutes;
        private int reliabilityScore;
        private String gridStatus;
        private double evoScore;
        private String lastUpdated;

        @DynamoDbPartitionKey
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getOperatorId() { return operatorId; }
        public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getArea() { return area; }
        public void setArea(String area) { this.area = area; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<String> getConnectors() { return connectors; }
        public void setConnectors(List<String> connectors) { this.connectors = connectors; }
        public int getPowerKw() { return powerKw; }
        public void setPowerKw(int powerKw) { this.powerKw = powerKw; }
        public int getWaitMinutes() { return waitMinutes; }
        public void setWaitMinutes(int waitMinutes) { this.waitMinutes = waitMinutes; }
        public int getReliabilityScore() { return reliabilityScore; }
        public void setReliabilityScore(int reliabilityScore) { this.reliabilityScore = reliabilityScore; }
        public String getGridStatus() { return gridStatus; }
        public void setGridStatus(String gridStatus) { this.gridStatus = gridStatus; }
        public double getEvoScore() { return evoScore; }
        public void setEvoScore(double evoScore) { this.evoScore = evoScore; }
        public String getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}
