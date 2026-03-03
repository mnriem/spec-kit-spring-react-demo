package com.llmanalytics.api.controller;

import com.llmanalytics.api.SuccessEnvelope;
import com.llmanalytics.api.dto.IterationResponse;
import com.llmanalytics.domain.service.IterationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class IterationController {

    private final IterationService iterationService;

    public IterationController(IterationService iterationService) {
        this.iterationService = iterationService;
    }

    @GetMapping("/api/experiments/{experimentId}/iterations")
    public SuccessEnvelope<List<IterationResponse>> listByExperiment(@PathVariable UUID experimentId) {
        return SuccessEnvelope.of(iterationService.findByExperimentId(experimentId));
    }
}
