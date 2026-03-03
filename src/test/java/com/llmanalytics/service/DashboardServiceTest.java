package com.llmanalytics.service;

import com.llmanalytics.api.dto.DashboardSummaryResponse;
import com.llmanalytics.domain.repository.InteractionRepository;
import com.llmanalytics.domain.repository.ToolCallRepository;
import com.llmanalytics.domain.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private InteractionRepository interactionRepository;

    @Mock
    private ToolCallRepository toolCallRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void summary_withData_returnsMetrics() {
        Object[] row = {5L, 250.0, 7500L, java.math.BigDecimal.valueOf(0.05)};
        when(interactionRepository.findSummaryMetrics(any(), any(), any())).thenReturn(row);

        DashboardSummaryResponse result = dashboardService.getSummary(null, null, null);

        assertThat(result.totalInteractions()).isEqualTo(5L);
        assertThat(result.avgLatencyMs()).isEqualTo(250.0);
    }

    @Test
    void summary_emptyDataset_returnsZeros() {
        Object[] row = {0L, null, null, null};
        when(interactionRepository.findSummaryMetrics(any(), any(), any())).thenReturn(row);

        DashboardSummaryResponse result = dashboardService.getSummary(null, null, null);

        assertThat(result.totalInteractions()).isEqualTo(0L);
        assertThat(result.avgLatencyMs()).isNull();
        assertThat(result.totalTokens()).isNull();
    }

    @Test
    void timeline_returnsDataPoints() {
        List<Object[]> rows = List.<Object[]>of(new Object[]{
                java.sql.Timestamp.valueOf("2026-03-01 00:00:00"), 3L, 200.0, 1500L
        });
        when(interactionRepository.findTimeline(any(), any(), any(), any())).thenReturn(rows);

        var result = dashboardService.getTimeline(null, null, null, "day");

        assertThat(result).hasSize(1);
    }

    @Test
    void tokensByModel_groupedByModel() {
        List<Object[]> rows = List.of(
                new Object[]{"gpt-4o", 5000L, 2500L},
                new Object[]{"gpt-4o-mini", 1000L, 500L}
        );
        when(interactionRepository.findTokensByModel(any(), any(), any())).thenReturn(rows);

        var result = dashboardService.getTokensByModel(null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).model()).isEqualTo("gpt-4o");
    }
}
