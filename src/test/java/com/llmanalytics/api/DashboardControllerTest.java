package com.llmanalytics.api;

import com.llmanalytics.api.dto.DashboardSummaryResponse;
import com.llmanalytics.api.dto.TimelineDataPoint;
import com.llmanalytics.api.dto.TokensByModelEntry;
import com.llmanalytics.api.dto.LatencyBucketEntry;
import com.llmanalytics.api.dto.ToolUsageEntry;
import com.llmanalytics.config.ApiKeyAuthFilter;
import com.llmanalytics.config.SecurityConfig;
import com.llmanalytics.domain.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.llmanalytics.api.controller.DashboardController.class)
@Import({SecurityConfig.class, ApiKeyAuthFilter.class})
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    private static final String VALID_KEY = "dev-key-change-in-production";

    @Test
    void summary_withApiKey_returns200() throws Exception {
        DashboardSummaryResponse summary = new DashboardSummaryResponse(10L, 300.0, 4500L, java.math.BigDecimal.valueOf(0.10), null, null);
        when(dashboardService.getSummary(any(), any(), any())).thenReturn(summary);

        mockMvc.perform(get("/api/dashboard/summary").header("X-API-Key", VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalInteractions").value(10));
    }

    @Test
    void summary_withoutApiKey_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void timeline_returns200() throws Exception {
        List<TimelineDataPoint> timeline = List.of(
                new TimelineDataPoint("2026-03-01", 5L, 200.0, 2500L)
        );
        when(dashboardService.getTimeline(any(), any(), any(), any())).thenReturn(timeline);

        mockMvc.perform(get("/api/dashboard/timeline").header("X-API-Key", VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].bucket").value("2026-03-01"));
    }

    @Test
    void tokensByModel_returns200() throws Exception {
        List<TokensByModelEntry> entries = List.of(new TokensByModelEntry("gpt-4o", 5000L, 2500L));
        when(dashboardService.getTokensByModel(any(), any(), any())).thenReturn(entries);

        mockMvc.perform(get("/api/dashboard/tokens-by-model").header("X-API-Key", VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].model").value("gpt-4o"));
    }

    @Test
    void latencyDistribution_returns200() throws Exception {
        List<LatencyBucketEntry> buckets = List.of(new LatencyBucketEntry(0L, 500L, 8L));
        when(dashboardService.getLatencyDistribution(any(), any(), any(), anyInt())).thenReturn(buckets);

        mockMvc.perform(get("/api/dashboard/latency-distribution").header("X-API-Key", VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].bucketMin").value(0));
    }

    @Test
    void toolUsage_returns200() throws Exception {
        List<ToolUsageEntry> tools = List.of(new ToolUsageEntry("search_tool", 15L));
        when(dashboardService.getToolUsage(any(), any(), any(), anyInt())).thenReturn(tools);

        mockMvc.perform(get("/api/dashboard/tool-usage").header("X-API-Key", VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].toolName").value("search_tool"));
    }
}
