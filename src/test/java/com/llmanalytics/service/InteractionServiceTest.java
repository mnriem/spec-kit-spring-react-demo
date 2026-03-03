package com.llmanalytics.service;

import com.llmanalytics.api.dto.*;
import com.llmanalytics.domain.model.*;
import com.llmanalytics.domain.repository.*;
import com.llmanalytics.domain.service.ExperimentService;
import com.llmanalytics.domain.service.InteractionService;
import com.llmanalytics.domain.service.IterationService;
import com.llmanalytics.domain.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private InteractionRepository interactionRepository;
    @Mock
    private ToolCallRepository toolCallRepository;
    @Mock
    private ModelPricingRepository modelPricingRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private ExperimentService experimentService;
    @Mock
    private IterationService iterationService;
    @Mock
    private IterationRepository iterationRepository;
    @Mock
    private ExperimentRepository experimentRepository;

    private InteractionService interactionService;

    private final UUID iterationId = UUID.randomUUID();
    private final UUID projectId = UUID.randomUUID();
    private final UUID experimentId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        interactionService = new InteractionService(
                interactionRepository, modelPricingRepository,
                projectService, experimentService, iterationService,
                iterationRepository, experimentRepository);
    }

    @Test
    void ingest_happyPath_computesDerivedMetrics() {
        OffsetDateTime start = OffsetDateTime.now().minusSeconds(2);
        OffsetDateTime end = start.plusSeconds(2);

        Project project = Project.builder().id(projectId).name("P").normalisedName("p").build();
        Experiment experiment = Experiment.builder().id(experimentId).projectId(projectId).name("E").normalisedName("e").build();
        Iteration iteration = Iteration.builder().id(iterationId).experimentId(experimentId).name("I").normalisedName("i").build();

        when(projectService.findOrCreate(anyString(), any())).thenReturn(project);
        when(experimentService.findOrCreate(any(), anyString(), any())).thenReturn(experiment);
        when(iterationService.findOrCreate(any(), anyString(), any())).thenReturn(iteration);
        when(modelPricingRepository.findByModelIdentifier("gpt-4o")).thenReturn(Optional.of(
                ModelPricing.builder()
                        .modelIdentifier("gpt-4o")
                        .inputPricePerMillionTokens(new BigDecimal("2.50"))
                        .outputPricePerMillionTokens(new BigDecimal("10.00"))
                        .build()));

        Interaction saved = Interaction.builder()
                .id(UUID.randomUUID())
                .iterationId(iterationId)
                .model("gpt-4o")
                .tokensIn(1000)
                .tokensOut(500)
                .totalTokens(1500)
                .latencyMs(2000L)
                .startedAt(start)
                .endedAt(end)
                .toolCalls(List.of())
                .build();
        when(interactionRepository.save(any())).thenReturn(saved);

        CreateInteractionRequest req = new CreateInteractionRequest(
                "P", null, "E", null, "I", null, "gpt-4o", null, null, 1000, 500, start, end, null);

        InteractionDetailResponse result = interactionService.ingest(req);

        assertThat(result).isNotNull();
        assertThat(result.totalTokens()).isEqualTo(1500);
    }

    @Test
    void ingest_endedAtBeforeStartedAt_throws422() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.minusSeconds(1);

        Project project = Project.builder().id(projectId).name("P").normalisedName("p").build();
        Experiment experiment = Experiment.builder().id(experimentId).projectId(projectId).name("E").normalisedName("e").build();
        Iteration iteration = Iteration.builder().id(iterationId).experimentId(experimentId).name("I").normalisedName("i").build();

        when(projectService.findOrCreate(anyString(), any())).thenReturn(project);
        when(experimentService.findOrCreate(any(), anyString(), any())).thenReturn(experiment);
        when(iterationService.findOrCreate(any(), anyString(), any())).thenReturn(iteration);

        CreateInteractionRequest req = new CreateInteractionRequest(
                "P", null, "E", null, "I", null, "gpt-4o", null, null, 100, 50, start, end, null);

        assertThatThrownBy(() -> interactionService.ingest(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ended_at");
    }

    @Test
    void ingest_tokensOutZero_valid() {
        OffsetDateTime start = OffsetDateTime.now().minusSeconds(1);
        OffsetDateTime end = start.plusSeconds(1);

        Project project = Project.builder().id(projectId).name("P").normalisedName("p").build();
        Experiment experiment = Experiment.builder().id(experimentId).projectId(projectId).name("E").normalisedName("e").build();
        Iteration iteration = Iteration.builder().id(iterationId).experimentId(experimentId).name("I").normalisedName("i").build();

        when(projectService.findOrCreate(anyString(), any())).thenReturn(project);
        when(experimentService.findOrCreate(any(), anyString(), any())).thenReturn(experiment);
        when(iterationService.findOrCreate(any(), anyString(), any())).thenReturn(iteration);
        when(modelPricingRepository.findByModelIdentifier(any())).thenReturn(Optional.empty());

        Interaction saved = Interaction.builder()
                .id(UUID.randomUUID())
                .iterationId(iterationId)
                .model("gpt-4o")
                .tokensIn(100)
                .tokensOut(0)
                .totalTokens(100)
                .latencyMs(1000L)
                .startedAt(start)
                .endedAt(end)
                .toolCalls(List.of())
                .build();
        when(interactionRepository.save(any())).thenReturn(saved);

        CreateInteractionRequest req = new CreateInteractionRequest(
                "P", null, "E", null, "I", null, "gpt-4o", null, null, 100, 0, start, end, null);

        assertThatNoException().isThrownBy(() -> interactionService.ingest(req));
    }

    @Test
    void ingest_modelWithNoPricing_nullCost() {
        OffsetDateTime start = OffsetDateTime.now().minusSeconds(1);
        OffsetDateTime end = start.plusSeconds(1);

        Project project = Project.builder().id(projectId).name("P").normalisedName("p").build();
        Experiment experiment = Experiment.builder().id(experimentId).projectId(projectId).name("E").normalisedName("e").build();
        Iteration iteration = Iteration.builder().id(iterationId).experimentId(experimentId).name("I").normalisedName("i").build();

        when(projectService.findOrCreate(anyString(), any())).thenReturn(project);
        when(experimentService.findOrCreate(any(), anyString(), any())).thenReturn(experiment);
        when(iterationService.findOrCreate(any(), anyString(), any())).thenReturn(iteration);
        when(modelPricingRepository.findByModelIdentifier("unknown-model")).thenReturn(Optional.empty());

        Interaction saved = Interaction.builder()
                .id(UUID.randomUUID())
                .iterationId(iterationId)
                .model("unknown-model")
                .tokensIn(100)
                .tokensOut(50)
                .totalTokens(150)
                .latencyMs(1000L)
                .estimatedCost(null)
                .startedAt(start)
                .endedAt(end)
                .toolCalls(List.of())
                .build();
        when(interactionRepository.save(any())).thenReturn(saved);

        CreateInteractionRequest req = new CreateInteractionRequest(
                "P", null, "E", null, "I", null, "unknown-model", null, null, 100, 50, start, end, null);

        InteractionDetailResponse result = interactionService.ingest(req);
        assertThat(result.estimatedCost()).isNull();
    }
}
