package com.llmanalytics.service;

import com.llmanalytics.api.dto.IterationMetrics;
import com.llmanalytics.domain.repository.InteractionRepository;
import com.llmanalytics.domain.repository.IterationRepository;
import com.llmanalytics.domain.repository.ToolCallRepository;
import com.llmanalytics.domain.service.ComparisonService;
import com.llmanalytics.domain.service.exception.EntityNotFoundException;
import com.llmanalytics.domain.model.Iteration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComparisonServiceTest {

    @Mock
    private InteractionRepository interactionRepository;

    @Mock
    private ToolCallRepository toolCallRepository;

    @Mock
    private IterationRepository iterationRepository;

    @InjectMocks
    private ComparisonService comparisonService;

    @Test
    void compare_twoIterations_returnsMetrics() {
        UUID iter1 = UUID.randomUUID();
        UUID iter2 = UUID.randomUUID();
        Iteration i1 = new Iteration();
        i1.setId(iter1);
        i1.setName("Iteration A");
        Iteration i2 = new Iteration();
        i2.setId(iter2);
        i2.setName("Iteration B");

        when(iterationRepository.findById(iter1)).thenReturn(Optional.of(i1));
        when(iterationRepository.findById(iter2)).thenReturn(Optional.of(i2));

        // [iterationId, avgLatency, avgTokensIn, avgTokensOut, avgTotal, avgCost, count]
        List<Object[]> metricsRows = List.of(
                new Object[]{iter1, 200.0, 100.0, 50.0, 150.0, BigDecimal.valueOf(0.01), 5L},
                new Object[]{iter2, 400.0, 200.0, 100.0, 300.0, BigDecimal.valueOf(0.02), 8L}
        );
        when(interactionRepository.findAggregateMetricsByIterationIds(List.of(iter1, iter2)))
                .thenReturn(metricsRows);

        List<Object[]> toolRows = List.of(
                new Object[]{iter1, 2.0},
                new Object[]{iter2, 3.5}
        );
        when(toolCallRepository.findAvgToolCallsPerInteraction(List.of(iter1, iter2)))
                .thenReturn(toolRows);

        List<IterationMetrics> result = comparisonService.compare(List.of(iter1, iter2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).avgLatencyMs()).isEqualTo(200.0);
        assertThat(result.get(1).avgLatencyMs()).isEqualTo(400.0);
    }

    @Test
    void compare_lessThanTwoIterations_throwsIllegalArgument() {
        UUID iter1 = UUID.randomUUID();
        assertThatThrownBy(() -> comparisonService.compare(List.of(iter1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 2");
    }

    @Test
    void compare_unknownIterationId_throwsEntityNotFound() {
        UUID iter1 = UUID.randomUUID();
        UUID iter2 = UUID.randomUUID();
        when(iterationRepository.findById(iter1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comparisonService.compare(List.of(iter1, iter2)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void compare_missingPricing_nullCostInResult() {
        UUID iter1 = UUID.randomUUID();
        UUID iter2 = UUID.randomUUID();
        Iteration i1 = new Iteration();
        i1.setId(iter1);
        i1.setName("A");
        Iteration i2 = new Iteration();
        i2.setId(iter2);
        i2.setName("B");

        when(iterationRepository.findById(iter1)).thenReturn(Optional.of(i1));
        when(iterationRepository.findById(iter2)).thenReturn(Optional.of(i2));

        List<Object[]> metricsRows = List.of(
                new Object[]{iter1, 200.0, 100.0, 50.0, 150.0, null, 5L},
                new Object[]{iter2, 300.0, 150.0, 75.0, 225.0, null, 3L}
        );
        when(interactionRepository.findAggregateMetricsByIterationIds(List.of(iter1, iter2)))
                .thenReturn(metricsRows);
        when(toolCallRepository.findAvgToolCallsPerInteraction(List.of(iter1, iter2)))
                .thenReturn(List.of());

        List<IterationMetrics> result = comparisonService.compare(List.of(iter1, iter2));

        assertThat(result.get(0).avgEstimatedCost()).isNull();
    }
}
