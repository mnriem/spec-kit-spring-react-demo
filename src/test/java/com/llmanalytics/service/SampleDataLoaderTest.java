package com.llmanalytics.service;

import com.llmanalytics.api.dto.SampleDataResponse;
import com.llmanalytics.domain.repository.ProjectRepository;
import com.llmanalytics.domain.service.InteractionService;
import com.llmanalytics.infra.seed.SampleDataLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SampleDataLoaderTest {

    @Mock
    private InteractionService interactionService;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private SampleDataLoader sampleDataLoader;

    @Test
    void load_onEmptySystem_seedsData() {
        when(projectRepository.count()).thenReturn(0L);
        when(interactionService.ingest(any())).thenAnswer(inv -> null);

        SampleDataResponse result = sampleDataLoader.load();

        assertThat(result.projectCount()).isGreaterThanOrEqualTo(3);
        assertThat(result.experimentCount()).isGreaterThanOrEqualTo(3);
        assertThat(result.iterationCount()).isGreaterThanOrEqualTo(6);
        assertThat(result.interactionCount()).isGreaterThanOrEqualTo(50);
        assertThat(result.message()).isNull();
    }

    @Test
    void load_whenDataExists_returnsWarning() {
        when(projectRepository.count()).thenReturn(5L);

        SampleDataResponse result = sampleDataLoader.load();

        assertThat(result.message()).contains("already");
        verifyNoInteractions(interactionService);
    }
}
