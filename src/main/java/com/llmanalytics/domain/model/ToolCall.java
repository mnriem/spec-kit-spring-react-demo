package com.llmanalytics.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tool_calls")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "interaction_id", nullable = false)
    private UUID interactionId;

    @Column(name = "tool_name", nullable = false, length = 255)
    private String toolName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_arguments", columnDefinition = "JSONB")
    private String inputArguments;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output", columnDefinition = "JSONB")
    private String output;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "called_at")
    private OffsetDateTime calledAt;
}
