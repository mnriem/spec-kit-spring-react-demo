package com.llmanalytics.api.controller;

import com.llmanalytics.api.SuccessEnvelope;
import com.llmanalytics.api.dto.*;
import com.llmanalytics.domain.service.InteractionQueryService;
import com.llmanalytics.domain.service.InteractionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/interactions")
public class InteractionController {

    private final InteractionService interactionService;
    private final InteractionQueryService interactionQueryService;

    public InteractionController(InteractionService interactionService,
                                  InteractionQueryService interactionQueryService) {
        this.interactionService = interactionService;
        this.interactionQueryService = interactionQueryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SuccessEnvelope<InteractionDetailResponse> create(@Valid @RequestBody CreateInteractionRequest request) {
        return SuccessEnvelope.of(interactionService.ingest(request));
    }

    @GetMapping("/{id}")
    public SuccessEnvelope<InteractionDetailResponse> getById(@PathVariable UUID id) {
        return SuccessEnvelope.of(interactionQueryService.findById(id));
    }

    @GetMapping
    public SuccessEnvelope<Page<InteractionSummaryResponse>> list(
            @RequestParam(required = false) String model,
            @RequestParam(required = false) UUID iterationId,
            @RequestParam(required = false) UUID experimentId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) Long minLatencyMs,
            @RequestParam(required = false) Long maxLatencyMs,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {

        InteractionFilterParams params = new InteractionFilterParams(
                model, experimentId, iterationId, projectId, from, to, minLatencyMs, maxLatencyMs, page, size, sort);
        return SuccessEnvelope.of(interactionQueryService.findAll(params));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        interactionService.deleteById(id);
    }
}
