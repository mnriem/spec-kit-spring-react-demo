package com.llmanalytics.service;

import com.llmanalytics.api.dto.InteractionFilterParams;
import com.llmanalytics.api.dto.InteractionSummaryResponse;
import com.llmanalytics.domain.model.Interaction;
import com.llmanalytics.domain.repository.ExperimentRepository;
import com.llmanalytics.domain.repository.InteractionRepository;
import com.llmanalytics.domain.repository.IterationRepository;
import com.llmanalytics.domain.repository.ProjectRepository;
import com.llmanalytics.domain.service.InteractionQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionQueryServiceTest {

    @Mock
    private InteractionRepository interactionRepository;
    @Mock
    private IterationRepository iterationRepository;
    @Mock
    private ExperimentRepository experimentRepository;
    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private InteractionQueryService service;

    private static InteractionFilterParams defaultParams(String model, UUID iterationId,
                                                          UUID experimentId,
                                                          OffsetDateTime from, OffsetDateTime to,
                                                          Long minLatency, Long maxLatency) {
        return new InteractionFilterParams(model, experimentId, iterationId, null, from, to, minLatency, maxLatency, 0, 20, null);
    }

    @Test
    void findAll_modelFilter_buildSpec() {
        when(interactionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        var params = defaultParams("gpt-4o", null, null, null, null, null, null);
        var result = service.findAll(params);

        assertThat(result.getContent()).isEmpty();
        verify(interactionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_dateRangeFilter_passedToSpec() {
        when(interactionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        var from = OffsetDateTime.now().minusDays(7);
        var to = OffsetDateTime.now();
        var params = defaultParams(null, null, null, from, to, null, null);
        var result = service.findAll(params);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAll_latencyRangeFilter_passedToSpec() {
        when(interactionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        var params = defaultParams(null, null, null, null, null, 100L, 500L);
        service.findAll(params);

        verify(interactionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_emptyResult_returnsEmptyPage() {
        when(interactionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        var params = defaultParams(null, null, null, null, null, null, null);
        var result = service.findAll(params);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
}
