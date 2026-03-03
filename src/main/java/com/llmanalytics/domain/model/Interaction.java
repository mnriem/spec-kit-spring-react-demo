package com.llmanalytics.domain.model;

import com.llmanalytics.infra.persistence.GzipStringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "interactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "iteration_id", nullable = false)
    private UUID iterationId;

    @Column(nullable = false, length = 100)
    private String model;

    @Convert(converter = GzipStringConverter.class)
    @Column(name = "prompt_compressed", columnDefinition = "BYTEA")
    private String promptCompressed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_metadata", columnDefinition = "JSONB")
    private String responseMetadata;

    @Column(name = "tokens_in", nullable = false)
    private Integer tokensIn;

    @Column(name = "tokens_out", nullable = false)
    private Integer tokensOut;

    @Column(name = "total_tokens", nullable = false)
    private Integer totalTokens;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private OffsetDateTime endedAt;

    @Column(name = "latency_ms", nullable = false)
    private Long latencyMs;

    @Column(name = "tokens_per_second")
    private Double tokensPerSecond;

    @Column(name = "estimated_cost", precision = 12, scale = 8)
    private BigDecimal estimatedCost;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "interaction_id")
    @OrderBy("sequence_order ASC")
    private List<ToolCall> toolCalls;
}
