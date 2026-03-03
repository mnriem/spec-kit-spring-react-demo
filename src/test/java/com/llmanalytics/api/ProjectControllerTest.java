package com.llmanalytics.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.llmanalytics.api.dto.*;
import com.llmanalytics.domain.service.ExperimentService;
import com.llmanalytics.domain.service.IterationService;
import com.llmanalytics.domain.service.ProjectService;
import com.llmanalytics.domain.service.exception.DuplicateNameException;
import com.llmanalytics.domain.service.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        com.llmanalytics.api.controller.ProjectController.class,
        com.llmanalytics.api.controller.ExperimentController.class,
        com.llmanalytics.api.controller.IterationController.class
})
@Import({com.llmanalytics.api.GlobalExceptionHandler.class,
         com.llmanalytics.config.SecurityConfig.class,
         com.llmanalytics.config.ApiKeyAuthFilter.class})
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ExperimentService experimentService;

    @MockBean
    private IterationService iterationService;

    private static final String VALID_KEY = "dev-key-change-in-production";

    @Test
    void getProjects_returns200() throws Exception {
        when(projectService.findAll(any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/projects")
                        .header("X-API-Key", VALID_KEY))
                .andExpect(status().isOk());
    }

    @Test
    void postProject_valid_returns201() throws Exception {
        CreateProjectRequest req = new CreateProjectRequest("Test Project", null);
        ProjectResponse resp = new ProjectResponse(UUID.randomUUID(), "Test Project", null, OffsetDateTime.now(), 0, 0);
        when(projectService.create(anyString(), any())).thenReturn(resp);

        mockMvc.perform(post("/api/projects")
                        .header("X-API-Key", VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void postProject_duplicate_returns409() throws Exception {
        CreateProjectRequest req = new CreateProjectRequest("Duplicate", null);
        when(projectService.create(anyString(), any())).thenThrow(new DuplicateNameException("duplicate"));

        mockMvc.perform(post("/api/projects")
                        .header("X-API-Key", VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void getProject_notFound_returns404() throws Exception {
        when(projectService.findById(any())).thenThrow(new EntityNotFoundException("not found"));

        mockMvc.perform(get("/api/projects/{id}", UUID.randomUUID())
                        .header("X-API-Key", VALID_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProject_returns204() throws Exception {
        doNothing().when(projectService).deleteById(any());

        mockMvc.perform(delete("/api/projects/{id}", UUID.randomUUID())
                        .header("X-API-Key", VALID_KEY))
                .andExpect(status().isNoContent());
    }
}
