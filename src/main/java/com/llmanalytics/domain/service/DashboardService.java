package com.llmanalytics.domain.service;

import com.llmanalytics.api.dto.*;
import com.llmanalytics.domain.repository.InteractionRepository;
import com.llmanalytics.domain.repository.ToolCallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final InteractionRepository interactionRepository;
    private final ToolCallRepository toolCallRepository;

    public DashboardSummaryResponse getSummary(UUID projectId, OffsetDateTime from, OffsetDateTime to) {
        OffsetDateTime[] range = defaultRange(from, to);
        List<Object[]> results = interactionRepository.findSummaryMetrics(projectId, range[0], range[1]);
        Object[] row = results.isEmpty() ? new Object[]{0L, null, null, null} : results.get(0);
        Long totalInteractions = row[0] == null ? 0L : ((Number) row[0]).longValue();
        Double avgLatency = row[1] == null ? null : ((Number) row[1]).doubleValue();
        Long totalTokens = row[2] == null ? null : ((Number) row[2]).longValue();
        BigDecimal totalCost = row[3] == null ? null : new BigDecimal(row[3].toString());
        return new DashboardSummaryResponse(totalInteractions, avgLatency, totalTokens, totalCost, range[0], range[1]);
    }

    public List<TimelineDataPoint> getTimeline(UUID projectId, OffsetDateTime from, OffsetDateTime to,
                                                String granularity) {
        OffsetDateTime[] range = defaultRange(from, to);
        String gran = granularity != null && List.of("hour", "day", "week", "month").contains(granularity)
                ? granularity : "day";
        List<Object[]> rows = interactionRepository.findTimeline(projectId, range[0], range[1], gran);
        return rows.stream().map(r -> {
            String bucket;
            if (r[0] instanceof Timestamp ts) {
                bucket = ts.toLocalDateTime().toInstant(ZoneOffset.UTC)
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE);
            } else {
                bucket = r[0].toString();
            }
            long count = ((Number) r[1]).longValue();
            Double avgLatency = r[2] == null ? null : ((Number) r[2]).doubleValue();
            Long tokens = r[3] == null ? null : ((Number) r[3]).longValue();
            return new TimelineDataPoint(bucket, count, avgLatency, tokens);
        }).toList();
    }

    public List<TokensByModelEntry> getTokensByModel(UUID projectId, OffsetDateTime from, OffsetDateTime to) {
        OffsetDateTime[] range = defaultRange(from, to);
        List<Object[]> rows = interactionRepository.findTokensByModel(projectId, range[0], range[1]);
        return rows.stream().map(r -> new TokensByModelEntry(
                (String) r[0],
                r[1] == null ? 0L : ((Number) r[1]).longValue(),
                r[2] == null ? 0L : ((Number) r[2]).longValue()
        )).toList();
    }

    public List<LatencyBucketEntry> getLatencyDistribution(UUID projectId, OffsetDateTime from,
                                                            OffsetDateTime to, int buckets) {
        OffsetDateTime[] range = defaultRange(from, to);
        int b = buckets <= 0 || buckets > 50 ? 10 : buckets;
        long maxLatency = 10_000L;
        List<Object[]> rows = interactionRepository.findLatencyDistribution(projectId, range[0], range[1], b, maxLatency);
        return rows.stream().map(r -> {
            long min = r[1] == null ? 0L : ((Number) r[1]).longValue();
            long max = r[2] == null ? 0L : ((Number) r[2]).longValue();
            long count = ((Number) r[3]).longValue();
            return new LatencyBucketEntry(min, max, count);
        }).toList();
    }

    public List<ToolUsageEntry> getToolUsage(UUID projectId, OffsetDateTime from,
                                              OffsetDateTime to, int limit) {
        OffsetDateTime[] range = defaultRange(from, to);
        int l = limit <= 0 || limit > 100 ? 20 : limit;
        List<Object[]> rows = toolCallRepository.findToolUsageFrequency(projectId, range[0], range[1], l);
        return rows.stream().map(r -> new ToolUsageEntry(
                (String) r[0],
                ((Number) r[1]).longValue()
        )).toList();
    }

    private OffsetDateTime[] defaultRange(OffsetDateTime from, OffsetDateTime to) {
        OffsetDateTime end = to != null ? to : OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime start = from != null ? from : end.minusDays(30);
        return new OffsetDateTime[]{start, end};
    }
}
