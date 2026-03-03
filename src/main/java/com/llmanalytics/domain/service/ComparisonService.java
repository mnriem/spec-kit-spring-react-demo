package com.llmanalytics.domain.service;

import com.llmanalytics.api.dto.IterationMetrics;
import com.llmanalytics.domain.model.Iteration;
import com.llmanalytics.domain.repository.InteractionRepository;
import com.llmanalytics.domain.repository.IterationRepository;
import com.llmanalytics.domain.repository.ToolCallRepository;
import com.llmanalytics.domain.service.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComparisonService {

    private final InteractionRepository interactionRepository;
    private final ToolCallRepository toolCallRepository;
    private final IterationRepository iterationRepository;

    public List<IterationMetrics> compare(List<UUID> iterationIds) {
        if (iterationIds == null || iterationIds.size() < 2) {
            throw new IllegalArgumentException("Comparison requires at least 2 iteration IDs");
        }

        // Validate all IDs exist and build name map
        Map<UUID, String> nameMap = new HashMap<>();
        for (UUID id : iterationIds) {
            Iteration iteration = iterationRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Iteration not found: " + id));
            nameMap.put(id, iteration.getName());
        }

        List<Object[]> metricsRows = interactionRepository.findAggregateMetricsByIterationIds(iterationIds);
        List<Object[]> toolRows = toolCallRepository.findAvgToolCallsPerInteraction(iterationIds);

        // Build tool-call-rate map: iterationId → avgToolCallsPerInteraction
        Map<UUID, Double> toolRateMap = new HashMap<>();
        for (Object[] row : toolRows) {
            UUID id = toUuid(row[0]);
            Double rate = row[1] == null ? null : ((Number) row[1]).doubleValue();
            toolRateMap.put(id, rate);
        }

        // Build metrics map
        Map<UUID, Object[]> metricsMap = new HashMap<>();
        for (Object[] row : metricsRows) {
            metricsMap.put(toUuid(row[0]), row);
        }

        return iterationIds.stream().map(id -> {
            Object[] row = metricsMap.get(id);
            if (row == null) {
                // No interactions yet for this iteration
                return new IterationMetrics(id, nameMap.get(id), null, null, null, null, null, null, 0L);
            }
            Double avgLatency = row[1] == null ? null : ((Number) row[1]).doubleValue();
            Double avgTokensIn = row[2] == null ? null : ((Number) row[2]).doubleValue();
            Double avgTokensOut = row[3] == null ? null : ((Number) row[3]).doubleValue();
            Double avgTotal = row[4] == null ? null : ((Number) row[4]).doubleValue();
            BigDecimal avgCost = row[5] == null ? null : new BigDecimal(row[5].toString());
            Long count = row[6] == null ? 0L : ((Number) row[6]).longValue();
            Double toolRate = toolRateMap.get(id);
            return new IterationMetrics(id, nameMap.get(id), avgLatency, avgTokensIn, avgTokensOut, avgTotal,
                    toolRate, avgCost, count);
        }).toList();
    }

    private UUID toUuid(Object val) {
        if (val instanceof UUID u) return u;
        return UUID.fromString(val.toString());
    }
}
