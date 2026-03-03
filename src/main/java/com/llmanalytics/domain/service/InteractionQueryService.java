package com.llmanalytics.domain.service;

import com.llmanalytics.api.dto.InteractionDetailResponse;
import com.llmanalytics.api.dto.InteractionFilterParams;
import com.llmanalytics.api.dto.InteractionSummaryResponse;
import com.llmanalytics.api.dto.ToolCallResponse;
import com.llmanalytics.domain.model.Experiment;
import com.llmanalytics.domain.model.Interaction;
import com.llmanalytics.domain.model.Iteration;
import com.llmanalytics.domain.model.Project;
import com.llmanalytics.domain.repository.ExperimentRepository;
import com.llmanalytics.domain.repository.InteractionRepository;
import com.llmanalytics.domain.repository.IterationRepository;
import com.llmanalytics.domain.repository.ProjectRepository;
import com.llmanalytics.domain.service.exception.EntityNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class InteractionQueryService {

    private final InteractionRepository interactionRepository;
    private final IterationRepository iterationRepository;
    private final ExperimentRepository experimentRepository;
    private final ProjectRepository projectRepository;

    public InteractionQueryService(InteractionRepository interactionRepository,
                                    IterationRepository iterationRepository,
                                    ExperimentRepository experimentRepository,
                                    ProjectRepository projectRepository) {
        this.interactionRepository = interactionRepository;
        this.iterationRepository = iterationRepository;
        this.experimentRepository = experimentRepository;
        this.projectRepository = projectRepository;
    }

    public Page<InteractionSummaryResponse> findAll(InteractionFilterParams params) {
        Specification<Interaction> spec = buildSpec(params);
        Sort sort = buildSort(params.sort());
        Pageable pageable = PageRequest.of(params.page(), params.size(), sort);

        Page<Interaction> page = interactionRepository.findAll(spec, pageable);
        List<InteractionSummaryResponse> content = page.getContent().stream()
                .map(this::toSummary)
                .toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public InteractionDetailResponse findById(UUID id) {
        Interaction interaction = interactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Interaction not found: " + id));
        return toDetail(interaction);
    }

    private InteractionDetailResponse toDetail(Interaction i) {
        Iteration iteration = iterationRepository.findById(i.getIterationId()).orElse(null);
        Experiment experiment = iteration != null
                ? experimentRepository.findById(iteration.getExperimentId()).orElse(null)
                : null;
        Project project = experiment != null
                ? projectRepository.findById(experiment.getProjectId()).orElse(null)
                : null;

        List<ToolCallResponse> toolCalls = i.getToolCalls() == null ? List.of() :
                i.getToolCalls().stream()
                        .map(tc -> new ToolCallResponse(tc.getId(), tc.getToolName(),
                                tc.getInputArguments(), tc.getOutput(), tc.getSequenceOrder(), tc.getCalledAt()))
                        .toList();

        return new InteractionDetailResponse(
                i.getId(),
                i.getIterationId(), iteration != null ? iteration.getName() : null,
                experiment != null ? experiment.getId() : null,
                experiment != null ? experiment.getName() : null,
                project != null ? project.getId() : null,
                project != null ? project.getName() : null,
                i.getModel(), i.getTokensIn(), i.getTokensOut(), i.getTotalTokens(),
                i.getLatencyMs(), i.getTokensPerSecond(), i.getEstimatedCost(),
                toolCalls.size(),
                i.getStartedAt(), i.getEndedAt(), i.getCreatedAt(),
                i.getPromptCompressed(), i.getResponseMetadata(), toolCalls
        );
    }

    private InteractionSummaryResponse toSummary(Interaction i) {
        Iteration iteration = iterationRepository.findById(i.getIterationId()).orElse(null);
        Experiment experiment = iteration != null
                ? experimentRepository.findById(iteration.getExperimentId()).orElse(null)
                : null;
        Project project = experiment != null
                ? projectRepository.findById(experiment.getProjectId()).orElse(null)
                : null;

        int toolCallCount = i.getToolCalls() == null ? 0 : i.getToolCalls().size();

        return new InteractionSummaryResponse(
                i.getId(),
                i.getIterationId(), iteration != null ? iteration.getName() : null,
                experiment != null ? experiment.getId() : null,
                experiment != null ? experiment.getName() : null,
                project != null ? project.getId() : null,
                project != null ? project.getName() : null,
                i.getModel(), i.getTokensIn(), i.getTokensOut(), i.getTotalTokens(),
                i.getLatencyMs(), i.getTokensPerSecond(), i.getEstimatedCost(),
                toolCallCount,
                i.getStartedAt(), i.getEndedAt(), i.getCreatedAt()
        );
    }

    private Specification<Interaction> buildSpec(InteractionFilterParams params) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (params.model() != null && !params.model().isBlank()) {
                predicates.add(cb.equal(root.get("model"), params.model()));
            }
            if (params.iterationId() != null) {
                predicates.add(cb.equal(root.get("iterationId"), params.iterationId()));
            }
            if (params.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startedAt"), params.from()));
            }
            if (params.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startedAt"), params.to()));
            }
            if (params.minLatencyMs() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("latencyMs"), params.minLatencyMs()));
            }
            if (params.maxLatencyMs() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("latencyMs"), params.maxLatencyMs()));
            }
            if (params.experimentId() != null) {
                var sub = query.subquery(UUID.class);
                var iterRoot = sub.from(Iteration.class);
                sub.select(iterRoot.get("id"))
                   .where(cb.equal(iterRoot.get("experimentId"), params.experimentId()));
                predicates.add(root.get("iterationId").in(sub));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private Sort buildSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "startedAt");
        }
        String[] parts = sortParam.split(",");
        String field = parts[0];
        Sort.Direction dir = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }
}
