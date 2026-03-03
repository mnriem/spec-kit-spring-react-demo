package com.llmanalytics.domain.service;

import com.llmanalytics.api.dto.IterationResponse;
import com.llmanalytics.domain.model.Iteration;
import com.llmanalytics.domain.repository.IterationRepository;
import com.llmanalytics.domain.service.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class IterationService {

    private final IterationRepository iterationRepository;

    public IterationService(IterationRepository iterationRepository) {
        this.iterationRepository = iterationRepository;
    }

    public Iteration findOrCreate(UUID experimentId, String name, String description) {
        String normalised = ProjectService.normalise(name);
        return iterationRepository.findByExperimentIdAndNormalisedName(experimentId, normalised)
                .orElseGet(() -> {
                    Iteration iteration = Iteration.builder()
                            .experimentId(experimentId)
                            .name(name)
                            .normalisedName(normalised)
                            .description(description)
                            .build();
                    return iterationRepository.save(iteration);
                });
    }

    @Transactional(readOnly = true)
    public List<IterationResponse> findByExperimentId(UUID experimentId) {
        return iterationRepository.findByExperimentIdOrderByCreatedAtDesc(experimentId).stream()
                .map(it -> new IterationResponse(
                        it.getId(), it.getExperimentId(), it.getName(),
                        it.getDescription(), it.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public IterationResponse findById(UUID id) {
        Iteration it = iterationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Iteration not found: " + id));
        return new IterationResponse(it.getId(), it.getExperimentId(), it.getName(),
                it.getDescription(), it.getCreatedAt());
    }
}
