package com.llmanalytics.api.controller;

import com.llmanalytics.api.SuccessEnvelope;
import com.llmanalytics.api.dto.CreateProjectRequest;
import com.llmanalytics.api.dto.ProjectResponse;
import com.llmanalytics.domain.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public SuccessEnvelope<Page<ProjectResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return SuccessEnvelope.of(
                projectService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SuccessEnvelope<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        return SuccessEnvelope.of(projectService.create(request.name(), request.description()));
    }

    @GetMapping("/{id}")
    public SuccessEnvelope<ProjectResponse> getById(@PathVariable UUID id) {
        return SuccessEnvelope.of(projectService.findById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        projectService.deleteById(id);
    }
}
