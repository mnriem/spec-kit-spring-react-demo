package com.llmanalytics.api.controller;

import com.llmanalytics.api.SuccessEnvelope;
import com.llmanalytics.api.dto.ExperimentResponse;
import com.llmanalytics.domain.service.ExperimentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ExperimentController {

    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @GetMapping("/api/projects/{projectId}/experiments")
    public SuccessEnvelope<List<ExperimentResponse>> listByProject(@PathVariable UUID projectId) {
        return SuccessEnvelope.of(experimentService.findByProjectId(projectId));
    }

    @DeleteMapping("/api/experiments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        experimentService.deleteById(id);
    }
}
