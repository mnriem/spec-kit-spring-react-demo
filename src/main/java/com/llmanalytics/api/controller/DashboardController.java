package com.llmanalytics.api.controller;

import com.llmanalytics.api.SuccessEnvelope;
import com.llmanalytics.api.dto.*;
import com.llmanalytics.domain.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<SuccessEnvelope<DashboardSummaryResponse>> getSummary(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to) {
        return ResponseEntity.ok(SuccessEnvelope.of(dashboardService.getSummary(projectId, from, to)));
    }

    @GetMapping("/timeline")
    public ResponseEntity<SuccessEnvelope<List<TimelineDataPoint>>> getTimeline(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(defaultValue = "day") String granularity) {
        return ResponseEntity.ok(SuccessEnvelope.of(
                dashboardService.getTimeline(projectId, from, to, granularity)));
    }

    @GetMapping("/tokens-by-model")
    public ResponseEntity<SuccessEnvelope<List<TokensByModelEntry>>> getTokensByModel(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to) {
        return ResponseEntity.ok(SuccessEnvelope.of(
                dashboardService.getTokensByModel(projectId, from, to)));
    }

    @GetMapping("/latency-distribution")
    public ResponseEntity<SuccessEnvelope<List<LatencyBucketEntry>>> getLatencyDistribution(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(defaultValue = "10") int buckets) {
        return ResponseEntity.ok(SuccessEnvelope.of(
                dashboardService.getLatencyDistribution(projectId, from, to, buckets)));
    }

    @GetMapping("/tool-usage")
    public ResponseEntity<SuccessEnvelope<List<ToolUsageEntry>>> getToolUsage(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(SuccessEnvelope.of(
                dashboardService.getToolUsage(projectId, from, to, limit)));
    }
}
