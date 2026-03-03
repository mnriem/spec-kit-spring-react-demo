package com.llmanalytics.domain.repository;

import com.llmanalytics.domain.model.Interaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, UUID>,
        JpaSpecificationExecutor<Interaction> {

    // Summary metrics
    @Query(value = """
            SELECT COUNT(i), AVG(i.latency_ms), SUM(i.total_tokens), SUM(i.estimated_cost)
            FROM interactions i
            JOIN iterations it ON i.iteration_id = it.id
            JOIN experiments e ON it.experiment_id = e.id
            WHERE (:projectId IS NULL OR e.project_id = :projectId)
              AND i.started_at >= :from
              AND i.started_at <= :to
            """, nativeQuery = true)
    List<Object[]> findSummaryMetrics(UUID projectId, OffsetDateTime from, OffsetDateTime to);

    // Timeline — daily bucketing
    @Query(value = """
            SELECT DATE_TRUNC(:granularity, i.started_at) AS bucket,
                   COUNT(*) AS interaction_count,
                   AVG(i.latency_ms) AS avg_latency_ms,
                   SUM(i.total_tokens) AS total_tokens
            FROM interactions i
            JOIN iterations it ON i.iteration_id = it.id
            JOIN experiments e ON it.experiment_id = e.id
            WHERE (:projectId IS NULL OR e.project_id = :projectId)
              AND i.started_at >= :from
              AND i.started_at <= :to
            GROUP BY bucket
            ORDER BY bucket
            """, nativeQuery = true)
    List<Object[]> findTimeline(UUID projectId, OffsetDateTime from, OffsetDateTime to, String granularity);

    // Tokens by model
    @Query(value = """
            SELECT i.model, SUM(i.tokens_in) AS total_tokens_in, SUM(i.tokens_out) AS total_tokens_out
            FROM interactions i
            JOIN iterations it ON i.iteration_id = it.id
            JOIN experiments e ON it.experiment_id = e.id
            WHERE (:projectId IS NULL OR e.project_id = :projectId)
              AND i.started_at >= :from
              AND i.started_at <= :to
            GROUP BY i.model
            ORDER BY SUM(i.total_tokens) DESC
            """, nativeQuery = true)
    List<Object[]> findTokensByModel(UUID projectId, OffsetDateTime from, OffsetDateTime to);

    // Latency histogram buckets (native: width_bucket)
    @Query(value = """
            SELECT width_bucket(i.latency_ms, 0, :maxLatency, :buckets) AS bucket_num,
                   MIN(i.latency_ms) AS bucket_min,
                   MAX(i.latency_ms) AS bucket_max,
                   COUNT(*) AS count
            FROM interactions i
            JOIN iterations it ON i.iteration_id = it.id
            JOIN experiments e ON it.experiment_id = e.id
            WHERE (:projectId IS NULL OR e.project_id = :projectId)
              AND i.started_at >= :from
              AND i.started_at <= :to
            GROUP BY bucket_num
            ORDER BY bucket_num
            """, nativeQuery = true)
    List<Object[]> findLatencyDistribution(UUID projectId, OffsetDateTime from, OffsetDateTime to,
                                            int buckets, long maxLatency);

    // Per-iteration aggregate metrics (for comparison)
    @Query("""
            SELECT i.iterationId,
                   AVG(i.latencyMs),
                   AVG(i.tokensIn),
                   AVG(i.tokensOut),
                   AVG(i.totalTokens),
                   AVG(i.estimatedCost),
                   COUNT(i)
            FROM Interaction i
            WHERE i.iterationId IN :iterationIds
            GROUP BY i.iterationId
            """)
    List<Object[]> findAggregateMetricsByIterationIds(List<UUID> iterationIds);
}
