package com.evocharge.api.controller;

import com.evocharge.api.model.Operator;
import com.evocharge.api.repository.OperatorRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Lists charging network operators. */
@RestController
@RequestMapping("/api/v1/operators")
public class OperatorController {

    private final OperatorRepository operatorRepository;

    public OperatorController(OperatorRepository operatorRepository) {
        this.operatorRepository = operatorRepository;
    }

    @GetMapping
    public List<Operator> list() {
        return operatorRepository.findAll();
    }
}
