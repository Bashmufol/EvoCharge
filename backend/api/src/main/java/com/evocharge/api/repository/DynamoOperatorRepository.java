package com.evocharge.api.repository;

import com.evocharge.api.config.EvoChargeProperties;
import com.evocharge.api.model.Operator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** DynamoDB-backed operator store for the {@code aws} Spring profile. */
@Repository
@ConditionalOnProperty(name = "evocharge.storage", havingValue = "dynamodb")
public class DynamoOperatorRepository implements OperatorRepository {

    private final DynamoDbTable<OperatorRecord> table;

    public DynamoOperatorRepository(DynamoDbClient dynamoDbClient, EvoChargeProperties properties) {
        DynamoDbEnhancedClient enhanced = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.table = enhanced.table(properties.getDynamodb().getOperatorsTable(), TableSchema.fromBean(OperatorRecord.class));
    }

    @Override
    public List<Operator> findAll() {
        return table.scan().items().stream().map(this::toModel).collect(Collectors.toList());
    }

    @Override
    public Optional<Operator> findById(String id) {
        OperatorRecord record = table.getItem(Key.builder().partitionValue(id).build());
        return Optional.ofNullable(record).map(this::toModel);
    }

    @Override
    public void saveAll(List<Operator> operators) {
        operators.forEach(o -> table.putItem(fromModel(o)));
    }

    private Operator toModel(OperatorRecord r) {
        Operator o = new Operator();
        o.setId(r.getId());
        o.setName(r.getName());
        o.setLogoUrl(r.getLogoUrl());
        o.setStationCount(r.getStationCount());
        o.setCoverage(r.getCoverage());
        return o;
    }

    private OperatorRecord fromModel(Operator o) {
        OperatorRecord r = new OperatorRecord();
        r.setId(o.getId());
        r.setName(o.getName());
        r.setLogoUrl(o.getLogoUrl());
        r.setStationCount(o.getStationCount());
        r.setCoverage(o.getCoverage());
        return r;
    }

    @DynamoDbBean
    public static class OperatorRecord {
        private String id;
        private String name;
        private String logoUrl;
        private int stationCount;
        private String coverage;

        @DynamoDbPartitionKey
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
        public int getStationCount() { return stationCount; }
        public void setStationCount(int stationCount) { this.stationCount = stationCount; }
        public String getCoverage() { return coverage; }
        public void setCoverage(String coverage) { this.coverage = coverage; }
    }
}
