package com.evocharge.api.controller;

import com.evocharge.api.dto.AdvisorRequest;
import com.evocharge.api.dto.AdvisorResponse;
import com.evocharge.api.service.AdvisorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/advisor")
public class AdvisorController {

    private final AdvisorService advisorService;

    public AdvisorController(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @PostMapping
    public AdvisorResponse advise(@RequestBody AdvisorRequest request) {
        return advisorService.advise(request);
    }
}
