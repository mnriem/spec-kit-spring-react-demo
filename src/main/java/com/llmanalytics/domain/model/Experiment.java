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
@Table(name = "experiments",
       uniqueConstraints = @UniqueConstraint(
               name = "idx_experiments_project_normalised",
               columnNames = {"project_id", "normalised_name"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Experiment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

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
