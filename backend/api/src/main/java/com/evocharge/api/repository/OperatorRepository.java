package com.evocharge.api.repository;

import com.evocharge.api.model.Operator;

import java.util.List;
import java.util.Optional;

public interface OperatorRepository {

    List<Operator> findAll();

    Optional<Operator> findById(String id);

    void saveAll(List<Operator> operators);
}
