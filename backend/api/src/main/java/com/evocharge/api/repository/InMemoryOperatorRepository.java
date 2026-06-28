package com.evocharge.api.repository;

import com.evocharge.api.model.Operator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-process operator store used for local development (default). */
@Repository
@ConditionalOnProperty(name = "evocharge.storage", havingValue = "local", matchIfMissing = true)
public class InMemoryOperatorRepository implements OperatorRepository {

    private final Map<String, Operator> operators = new ConcurrentHashMap<>();

    @Override
    public List<Operator> findAll() {
        return new ArrayList<>(operators.values());
    }

    @Override
    public Optional<Operator> findById(String id) {
        return Optional.ofNullable(operators.get(id));
    }

    @Override
    public void saveAll(List<Operator> operatorList) {
        operatorList.forEach(o -> operators.put(o.getId(), o));
    }
}
