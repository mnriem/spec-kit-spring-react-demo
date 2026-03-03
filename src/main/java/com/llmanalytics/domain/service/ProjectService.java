package com.llmanalytics.domain.service;

import com.llmanalytics.api.dto.ExperimentResponse;
import com.llmanalytics.api.dto.ProjectResponse;
import com.llmanalytics.domain.model.Project;
import com.llmanalytics.domain.repository.ExperimentRepository;
import com.llmanalytics.domain.repository.InteractionRepository;
import com.llmanalytics.domain.repository.ProjectRepository;
import com.llmanalytics.domain.service.exception.DuplicateNameException;
import com.llmanalytics.domain.service.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ExperimentRepository experimentRepository;
    private final InteractionRepository interactionRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ExperimentRepository experimentRepository,
                          InteractionRepository interactionRepository) {
        this.projectRepository = projectRepository;
        this.experimentRepository = experimentRepository;
        this.interactionRepository = interactionRepository;
    }

    public Project findOrCreate(String name, String description) {
        String normalised = normalise(name);
        return projectRepository.findByNormalisedName(normalised)
                .orElseGet(() -> {
                    Project project = Project.builder()
                            .name(name)
                            .normalisedName(normalised)
                            .description(description)
                            .build();
                    return projectRepository.save(project);
                });
    }

    public ProjectResponse create(String name, String description) {
        String normalised = normalise(name);
        if (projectRepository.findByNormalisedName(normalised).isPresent()) {
            throw new DuplicateNameException("Project with name '" + name + "' already exists");
        }
        Project project = Project.builder()
                .name(name)
                .normalisedName(normalised)
                .description(description)
                .build();
        project = projectRepository.save(project);
        return toResponse(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> findAll(Pageable pageable) {
        return projectRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));
        return toResponse(project);
    }

    public void deleteById(UUID id) {
        if (!projectRepository.existsById(id)) {
            throw new EntityNotFoundException("Project not found: " + id);
        }
        projectRepository.deleteById(id);
    }

    private ProjectResponse toResponse(Project project) {
        long expCount = projectRepository.countExperimentsByProjectId(project.getId());
        long intCount = projectRepository.countInteractionsByProjectId(project.getId());
        return new ProjectResponse(
                project.getId(), project.getName(), project.getDescription(),
                project.getCreatedAt(), expCount, intCount);
    }

    static String normalise(String name) {
        if (name == null) return null;
        return name.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
