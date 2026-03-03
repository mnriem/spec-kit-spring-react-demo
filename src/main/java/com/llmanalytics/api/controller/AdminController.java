package com.llmanalytics.api.controller;

import com.llmanalytics.api.SuccessEnvelope;
import com.llmanalytics.api.dto.SampleDataResponse;
import com.llmanalytics.infra.seed.SampleDataLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SampleDataLoader sampleDataLoader;

    @PostMapping("/sample-data")
    public ResponseEntity<SuccessEnvelope<SampleDataResponse>> loadSampleData() {
        SampleDataResponse result = sampleDataLoader.load();
        int status = result.message() != null ? HttpStatus.OK.value() : HttpStatus.CREATED.value();
        return ResponseEntity.status(status).body(SuccessEnvelope.of(result));
    }
}
