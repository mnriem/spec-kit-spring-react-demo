package com.llmanalytics.domain.repository;

import com.llmanalytics.domain.model.ToolCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ToolCallRepository extends JpaRepository<ToolCall, UUID> {

    List<ToolCall> findByInteractionIdOrderBySequenceOrderAsc(UUID interactionId);

    // Tool usage frequency
    @Query(value = """
            SELECT tc.tool_name, COUNT(*) AS usage_count
            FROM tool_calls tc
            JOIN interactions i ON tc.interaction_id = i.id
            JOIN iterations it ON i.iteration_id = it.id
            JOIN experiments e ON it.experiment_id = e.id
            WHERE (:projectId IS NULL OR e.project_id = :projectId)
              AND i.started_at >= :from
              AND i.started_at <= :to
            GROUP BY tc.tool_name
            ORDER BY usage_count DESC
            LIMIT :limitCount
            """, nativeQuery = true)
    List<Object[]> findToolUsageFrequency(UUID projectId, OffsetDateTime from, OffsetDateTime to, int limitCount);

    // Average tool calls per interaction (for comparison)
    @Query(value = """
            SELECT i.iteration_id,
                   CAST(COUNT(tc.id) AS DOUBLE PRECISION) / NULLIF(COUNT(DISTINCT tc.interaction_id), 0)
            FROM tool_calls tc
            JOIN interactions i ON tc.interaction_id = i.id
            WHERE i.iteration_id IN :iterationIds
            GROUP BY i.iteration_id
            """, nativeQuery = true)
    List<Object[]> findAvgToolCallsPerInteraction(List<UUID> iterationIds);
}
