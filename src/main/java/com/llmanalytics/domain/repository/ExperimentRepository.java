package com.llmanalytics.domain.repository;

import com.llmanalytics.domain.model.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, UUID> {

    List<Experiment> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    Optional<Experiment> findByProjectIdAndNormalisedName(UUID projectId, String normalisedName);

    @Query("SELECT COUNT(it) FROM Iteration it WHERE it.experimentId = :experimentId")
    long countIterationsByExperimentId(UUID experimentId);
}
