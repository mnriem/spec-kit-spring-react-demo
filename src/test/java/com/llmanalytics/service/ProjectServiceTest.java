package com.llmanalytics.service;

import com.llmanalytics.domain.model.Project;
import com.llmanalytics.domain.repository.ExperimentRepository;
import com.llmanalytics.domain.repository.InteractionRepository;
import com.llmanalytics.domain.repository.ProjectRepository;
import com.llmanalytics.domain.service.ProjectService;
import com.llmanalytics.domain.service.exception.DuplicateNameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ExperimentRepository experimentRepository;
    @Mock
    private InteractionRepository interactionRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void findOrCreate_newProject_creates() {
        when(projectRepository.findByNormalisedName("test project")).thenReturn(Optional.empty());
        Project created = Project.builder().id(UUID.randomUUID()).name("Test Project").normalisedName("test project").build();
        when(projectRepository.save(any())).thenReturn(created);

        Project result = projectService.findOrCreate("Test Project", null);

        assertThat(result.getName()).isEqualTo("Test Project");
        verify(projectRepository).save(any());
    }

    @Test
    void findOrCreate_existingProject_returnsExisting() {
        Project existing = Project.builder().id(UUID.randomUUID()).name("Test Project").normalisedName("test project").build();
        when(projectRepository.findByNormalisedName("test project")).thenReturn(Optional.of(existing));

        Project result = projectService.findOrCreate("Test Project", null);

        assertThat(result.getId()).isEqualTo(existing.getId());
        verify(projectRepository, never()).save(any());
    }

    @Test
    void create_duplicateName_throwsDuplicateNameException() {
        Project existing = Project.builder().id(UUID.randomUUID()).name("Test Project").normalisedName("test project").build();
        when(projectRepository.findByNormalisedName("test project")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> projectService.create("Test Project", null))
                .isInstanceOf(DuplicateNameException.class);
    }
}
