package com.llmanalytics.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.llmanalytics.api.dto.*;
import com.llmanalytics.domain.model.*;
import com.llmanalytics.domain.repository.ExperimentRepository;
import com.llmanalytics.domain.repository.InteractionRepository;
import com.llmanalytics.domain.repository.IterationRepository;
import com.llmanalytics.domain.repository.ModelPricingRepository;
import com.llmanalytics.domain.repository.ToolCallRepository;
import com.llmanalytics.domain.service.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final ModelPricingRepository modelPricingRepository;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final IterationService iterationService;
    private final IterationRepository iterationRepository;
    private final ExperimentRepository experimentRepository;
    private final ToolCallRepository toolCallRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InteractionService(InteractionRepository interactionRepository,
                               ModelPricingRepository modelPricingRepository,
                               ProjectService projectService,
                               ExperimentService experimentService,
                               IterationService iterationService,
                               IterationRepository iterationRepository,
                               ExperimentRepository experimentRepository,
                               ToolCallRepository toolCallRepository) {
        this.interactionRepository = interactionRepository;
        this.modelPricingRepository = modelPricingRepository;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.iterationService = iterationService;
        this.iterationRepository = iterationRepository;
        this.experimentRepository = experimentRepository;
        this.toolCallRepository = toolCallRepository;
    }

    public InteractionDetailResponse ingest(CreateInteractionRequest req) {
        // Resolve hierarchy
        Project project = projectService.findOrCreate(req.projectName(), req.projectDescription());
        Experiment experiment = experimentService.findOrCreate(project.getId(), req.experimentName(), req.experimentDescription());
        Iteration iteration = iterationService.findOrCreate(experiment.getId(), req.iterationName(), req.iterationDescription());

        // Validate timing
        if (req.endedAt().isBefore(req.startedAt())) {
            throw new IllegalArgumentException("ended_at must be >= started_at");
        }

        // Compute derived fields
        long latencyMs = Duration.between(req.startedAt(), req.endedAt()).toMillis();
        int totalTokens = req.tokensIn() + req.tokensOut();
        Double tokensPerSecond = latencyMs > 0
                ? (double) totalTokens / latencyMs * 1000.0
                : null;

        // Cost calculation
        Optional<ModelPricing> pricing = modelPricingRepository.findByModelIdentifier(req.model());
        BigDecimal estimatedCost = pricing.map(p -> {
            BigDecimal inputCost = p.getInputPricePerMillionTokens()
                    .multiply(BigDecimal.valueOf(req.tokensIn()))
                    .divide(BigDecimal.valueOf(1_000_000), 8, RoundingMode.HALF_UP);
            BigDecimal outputCost = p.getOutputPricePerMillionTokens()
                    .multiply(BigDecimal.valueOf(req.tokensOut()))
                    .divide(BigDecimal.valueOf(1_000_000), 8, RoundingMode.HALF_UP);
            return inputCost.add(outputCost);
        }).orElse(null);

        // Build tool calls (saved separately after interaction to set interaction_id)
        List<ToolCallRequest> toolCallRequests = req.toolsCalled();

        // Serialize response_metadata
        String responseMetadataJson = toJson(req.responseMetadata());

        Interaction interaction = Interaction.builder()
                .iterationId(iteration.getId())
                .model(req.model())
                .promptCompressed(req.prompt())
                .responseMetadata(responseMetadataJson)
                .tokensIn(req.tokensIn())
                .tokensOut(req.tokensOut())
                .totalTokens(totalTokens)
                .startedAt(req.startedAt())
                .endedAt(req.endedAt())
                .latencyMs(latencyMs)
                .tokensPerSecond(tokensPerSecond)
                .estimatedCost(estimatedCost)
                .toolCalls(new ArrayList<>())
                .build();

        interaction = interactionRepository.save(interaction);

        // Now save tool calls with the persisted interaction ID
        List<ToolCall> toolCalls = new ArrayList<>();
        if (toolCallRequests != null) {
            for (int i = 0; i < toolCallRequests.size(); i++) {
                ToolCallRequest tc = toolCallRequests.get(i);
                toolCalls.add(ToolCall.builder()
                        .interactionId(interaction.getId())
                        .toolName(tc.toolName())
                        .inputArguments(toJson(tc.inputArguments()))
                        .output(toJson(tc.output()))
                        .sequenceOrder(i)
                        .calledAt(tc.calledAt())
                        .build());
            }
            toolCalls = toolCallRepository.saveAll(toolCalls);
            interaction.setToolCalls(toolCalls);
        }
        return toDetailResponse(interaction, iteration, experiment, project);
    }

    @Transactional(readOnly = true)
    public InteractionDetailResponse findById(UUID id) {
        Interaction interaction = interactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Interaction not found: " + id));
        Iteration iteration = iterationRepository.findById(interaction.getIterationId()).orElse(null);
        Experiment experiment = iteration != null
                ? experimentRepository.findById(iteration.getExperimentId()).orElse(null)
                : null;
        return toDetailResponseFromIteration(interaction, iteration, experiment);
    }

    public void deleteById(UUID id) {
        if (!interactionRepository.existsById(id)) {
            throw new EntityNotFoundException("Interaction not found: " + id);
        }
        interactionRepository.deleteById(id);
    }

    private InteractionDetailResponse toDetailResponse(Interaction saved, Iteration iteration,
                                                         Experiment experiment, Project project) {
        List<ToolCallResponse> toolCallResponses = saved.getToolCalls() == null
                ? List.of()
                : saved.getToolCalls().stream()
                    .map(tc -> new ToolCallResponse(tc.getId(), tc.getToolName(),
                            tc.getInputArguments(), tc.getOutput(), tc.getSequenceOrder(), tc.getCalledAt()))
                    .toList();

        return new InteractionDetailResponse(
                saved.getId(),
                iteration.getId(), iteration.getName(),
                experiment.getId(), experiment.getName(),
                project.getId(), project.getName(),
                saved.getModel(),
                saved.getTokensIn(), saved.getTokensOut(), saved.getTotalTokens(),
                saved.getLatencyMs(), saved.getTokensPerSecond(), saved.getEstimatedCost(),
                toolCallResponses.size(),
                saved.getStartedAt(), saved.getEndedAt(), saved.getCreatedAt(),
                saved.getPromptCompressed(),
                saved.getResponseMetadata(),
                toolCallResponses
        );
    }

    private InteractionDetailResponse toDetailResponseFromIteration(Interaction saved, Iteration iteration,
                                                                      Experiment experiment) {
        // Simplified version when we don't have full hierarchy
        List<ToolCallResponse> toolCallResponses = saved.getToolCalls() == null
                ? List.of()
                : saved.getToolCalls().stream()
                    .map(tc -> new ToolCallResponse(tc.getId(), tc.getToolName(),
                            tc.getInputArguments(), tc.getOutput(), tc.getSequenceOrder(), tc.getCalledAt()))
                    .toList();

        String iterName = iteration != null ? iteration.getName() : null;
        UUID expId = iteration != null ? iteration.getExperimentId() : null;
        String expName = experiment != null ? experiment.getName() : null;

        return new InteractionDetailResponse(
                saved.getId(),
                saved.getIterationId(), iterName,
                expId, expName, null, null,
                saved.getModel(),
                saved.getTokensIn(), saved.getTokensOut(), saved.getTotalTokens(),
                saved.getLatencyMs(), saved.getTokensPerSecond(), saved.getEstimatedCost(),
                toolCallResponses.size(),
                saved.getStartedAt(), saved.getEndedAt(), saved.getCreatedAt(),
                saved.getPromptCompressed(),
                saved.getResponseMetadata(),
                toolCallResponses
        );
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
