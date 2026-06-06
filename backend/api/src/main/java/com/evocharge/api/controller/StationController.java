package com.evocharge.api.controller;

import com.evocharge.api.model.Station;
import com.evocharge.api.model.StationStatus;
import com.evocharge.api.service.StationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public List<Station> list(
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) StationStatus status,
            @RequestParam(required = false) String connector,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city) {
        return stationService.findAll(operator, status, connector, search, city);
    }

    @GetMapping("/cities")
    public List<String> cities() {
        return stationService.listCities();
    }

    @GetMapping("/nearby")
    public List<Station> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radius,
            @RequestParam(required = false) String city) {
        return stationService.findNearby(lat, lng, radius, city);
    }

    @GetMapping("/{id}")
    public Station get(@PathVariable String id) {
        return stationService.findById(id);
    }

}
