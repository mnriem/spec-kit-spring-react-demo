package com.llmanalytics.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "iterations",
       uniqueConstraints = @UniqueConstraint(
               name = "idx_iterations_experiment_normalised",
               columnNames = {"experiment_id", "normalised_name"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Iteration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "experiment_id", nullable = false)
    private UUID experimentId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "normalised_name", nullable = false, length = 255)
    private String normalisedName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
