package com.llmanalytics.api.controller;

import com.llmanalytics.api.SuccessEnvelope;
import com.llmanalytics.api.dto.IterationMetrics;
import com.llmanalytics.domain.service.ComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comparison")
@RequiredArgsConstructor
public class ComparisonController {

    private final ComparisonService comparisonService;

    @GetMapping
    public ResponseEntity<SuccessEnvelope<List<IterationMetrics>>> compare(
            @RequestParam List<UUID> iterationIds) {
        return ResponseEntity.ok(SuccessEnvelope.of(comparisonService.compare(iterationIds)));
    }
}
