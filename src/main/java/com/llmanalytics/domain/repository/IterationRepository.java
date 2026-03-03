package com.llmanalytics.domain.repository;

import com.llmanalytics.domain.model.Iteration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IterationRepository extends JpaRepository<Iteration, UUID> {

    List<Iteration> findByExperimentIdOrderByCreatedAtDesc(UUID experimentId);

    Optional<Iteration> findByExperimentIdAndNormalisedName(UUID experimentId, String normalisedName);
}
