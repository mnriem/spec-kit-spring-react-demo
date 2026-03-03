package com.llmanalytics.domain.service;

import com.llmanalytics.api.dto.ExperimentResponse;
import com.llmanalytics.domain.model.Experiment;
import com.llmanalytics.domain.repository.ExperimentRepository;
import com.llmanalytics.domain.service.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExperimentService {

    private final ExperimentRepository experimentRepository;

    public ExperimentService(ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    public Experiment findOrCreate(UUID projectId, String name, String description) {
        String normalised = ProjectService.normalise(name);
        return experimentRepository.findByProjectIdAndNormalisedName(projectId, normalised)
                .orElseGet(() -> {
                    Experiment experiment = Experiment.builder()
                            .projectId(projectId)
                            .name(name)
                            .normalisedName(normalised)
                            .description(description)
                            .build();
                    return experimentRepository.save(experiment);
                });
    }

    @Transactional(readOnly = true)
    public List<ExperimentResponse> findByProjectId(UUID projectId) {
        return experimentRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExperimentResponse findById(UUID id) {
        Experiment exp = experimentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Experiment not found: " + id));
        return toResponse(exp);
    }

    public void deleteById(UUID id) {
        if (!experimentRepository.existsById(id)) {
            throw new EntityNotFoundException("Experiment not found: " + id);
        }
        experimentRepository.deleteById(id);
    }

    private ExperimentResponse toResponse(Experiment exp) {
        long iterCount = experimentRepository.countIterationsByExperimentId(exp.getId());
        return new ExperimentResponse(
                exp.getId(), exp.getProjectId(), exp.getName(), exp.getDescription(),
                exp.getCreatedAt(), iterCount);
    }
}
