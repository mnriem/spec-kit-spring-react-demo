package com.llmanalytics.domain.repository;

import com.llmanalytics.domain.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByNormalisedName(String normalisedName);

    Page<Project> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(e) FROM Experiment e WHERE e.projectId = :projectId")
    long countExperimentsByProjectId(UUID projectId);

    @Query("""
            SELECT COUNT(i) FROM Interaction i
            JOIN Iteration it ON i.iterationId = it.id
            JOIN Experiment e ON it.experimentId = e.id
            WHERE e.projectId = :projectId
            """)
    long countInteractionsByProjectId(UUID projectId);
}
