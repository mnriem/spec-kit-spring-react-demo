package com.llmanalytics.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.llmanalytics.api.dto.CreateInteractionRequest;
import com.llmanalytics.api.dto.InteractionDetailResponse;
import com.llmanalytics.domain.service.InteractionService;
import com.llmanalytics.domain.service.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.llmanalytics.api.controller.InteractionController.class)
@Import({com.llmanalytics.api.GlobalExceptionHandler.class,
         com.llmanalytics.config.SecurityConfig.class,
         com.llmanalytics.config.ApiKeyAuthFilter.class})
class InteractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InteractionService interactionService;

    @MockBean
    private com.llmanalytics.domain.service.InteractionQueryService interactionQueryService;

    private static final String VALID_KEY = "dev-key-change-in-production";

    @Test
    void postInteraction_valid_returns201() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().minusSeconds(1);
        OffsetDateTime end = start.plusSeconds(1);

        CreateInteractionRequest req = new CreateInteractionRequest(
                "Project", null, "Experiment", null, "Iteration", null,
                "gpt-4o", null, null, 100, 50, start, end, null);

        InteractionDetailResponse response = new InteractionDetailResponse(
                UUID.randomUUID(), UUID.randomUUID(), "iter", UUID.randomUUID(), "exp",
                UUID.randomUUID(), "proj", "gpt-4o", 100, 50, 150, 1000L, 150.0, null, 0,
                start, end, start, null, null, List.of());

        when(interactionService.ingest(any())).thenReturn(response);

        mockMvc.perform(post("/api/interactions")
                        .header("X-API-Key", VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void postInteraction_missingApiKey_returns401() throws Exception {
        mockMvc.perform(post("/api/interactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postInteraction_missingRequiredFields_returns422() throws Exception {
        String invalidBody = "{\"tokens_in\": -1}";

        mockMvc.perform(post("/api/interactions")
                        .header("X-API-Key", VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isUnprocessableEntity());
    }
}
