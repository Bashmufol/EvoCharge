package com.evocharge.api.repository;

import com.evocharge.api.model.Station;
import com.evocharge.api.model.StationStatus;

import java.util.List;
import java.util.Optional;

public interface StationRepository {

    List<Station> findAll();

    Optional<Station> findById(String id);

    void saveAll(List<Station> stations);

    void save(Station station);

    List<Station> findFiltered(String operatorId, StationStatus status, String connector, String search, String city);
}
