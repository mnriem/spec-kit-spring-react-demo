package com.llmanalytics.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.llmanalytics.api.SuccessEnvelope;
import com.llmanalytics.api.dto.CreateInteractionRequest;
import com.llmanalytics.api.dto.InteractionDetailResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class IngestIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("llmanalytics_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_KEY = "dev-key-change-in-production";

    @Test
    void fullIngestAndRetrieveCycle() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().minusSeconds(2);
        OffsetDateTime end = start.plusSeconds(2);

        CreateInteractionRequest req = new CreateInteractionRequest(
                "Integration Test Project", "A test project",
                "Integration Experiment", null,
                "v1", null,
                "gpt-4o", "Test prompt for integration test", null,
                1000, 500, start, end, null);

        // POST - create interaction
        MvcResult postResult = mockMvc.perform(post("/api/interactions")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = postResult.getResponse().getContentAsString();
        // Extract id from the response
        var envelope = objectMapper.readTree(responseBody);
        String id = envelope.get("data").get("id").asText();

        assertThat(id).isNotBlank();

        // Verify derived fields
        assertThat(envelope.get("data").get("total_tokens").asInt()).isEqualTo(1500);
        assertThat(envelope.get("data").get("latency_ms").asLong()).isGreaterThanOrEqualTo(0L);

        // GET by ID
        MvcResult getResult = mockMvc.perform(get("/api/interactions/{id}", id)
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andReturn();

        var getEnvelope = objectMapper.readTree(getResult.getResponse().getContentAsString());
        assertThat(getEnvelope.get("data").get("prompt").asText()).isEqualTo("Test prompt for integration test");

        // DELETE
        mockMvc.perform(delete("/api/interactions/{id}", id)
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isNoContent());

        // Verify 404 after delete
        mockMvc.perform(get("/api/interactions/{id}", id)
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isNotFound());
    }
}
