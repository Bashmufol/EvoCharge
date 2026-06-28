package com.evocharge.api.controller;

import com.evocharge.api.dto.AnalyticsSummary;
import com.evocharge.api.dto.DemandArea;
import com.evocharge.api.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Network summary KPIs and demand-by-area aggregates. */
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public AnalyticsSummary summary() {
        return analyticsService.getSummary();
    }

    @GetMapping("/demand-by-area")
    public List<DemandArea> demandByArea(@RequestParam(required = false) String city) {
        return analyticsService.getDemandByArea(city);
    }
}
