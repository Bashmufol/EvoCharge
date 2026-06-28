package com.evocharge.api.controller;

import com.evocharge.api.dto.RecommendRequest;
import com.evocharge.api.dto.RecommendResponse;
import com.evocharge.api.service.StationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** EvoScore-based station ranking for a given location and connector type. */
@RestController
@RequestMapping("/api/v1/recommend")
public class RecommendController {

    private final StationService stationService;

    public RecommendController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping
    public RecommendResponse recommend(@RequestBody RecommendRequest request) {
        return stationService.recommend(request);
    }
}
