package com.llmanalytics.api;

import com.llmanalytics.api.dto.IterationMetrics;
import com.llmanalytics.config.ApiKeyAuthFilter;
import com.llmanalytics.config.SecurityConfig;
import com.llmanalytics.domain.service.ComparisonService;
import com.llmanalytics.domain.service.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.llmanalytics.api.controller.ComparisonController.class)
@Import({SecurityConfig.class, ApiKeyAuthFilter.class})
class ComparisonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComparisonService comparisonService;

    private static final String VALID_KEY = "dev-key-change-in-production";

    @Test
    void compare_twoValidIds_returns200() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<IterationMetrics> metrics = List.of(
                new IterationMetrics(id1, "A", 200.0, 100.0, 50.0, 150.0, 2.0, BigDecimal.valueOf(0.01), 5L),
                new IterationMetrics(id2, "B", 300.0, 150.0, 75.0, 225.0, 3.0, BigDecimal.valueOf(0.02), 8L)
        );
        when(comparisonService.compare(anyList())).thenReturn(metrics);

        mockMvc.perform(get("/api/comparison")
                        .param("iterationIds", id1.toString(), id2.toString())
                        .header("X-API-Key", VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void compare_oneId_returns400() throws Exception {
        UUID id1 = UUID.randomUUID();
        when(comparisonService.compare(anyList()))
                .thenThrow(new IllegalArgumentException("Comparison requires at least 2 iterations"));

        mockMvc.perform(get("/api/comparison")
                        .param("iterationIds", id1.toString())
                        .header("X-API-Key", VALID_KEY))
                .andExpect(status().isBadRequest());
    }

    @Test
    void compare_unknownId_returns404() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(comparisonService.compare(anyList()))
                .thenThrow(new EntityNotFoundException("Iteration not found"));

        mockMvc.perform(get("/api/comparison")
                        .param("iterationIds", id1.toString(), id2.toString())
                        .header("X-API-Key", VALID_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void compare_missingApiKey_returns401() throws Exception {
        mockMvc.perform(get("/api/comparison")
                        .param("iterationIds", UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }
}
