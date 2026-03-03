package com.llmanalytics.api;

import com.llmanalytics.api.dto.SampleDataResponse;
import com.llmanalytics.config.ApiKeyAuthFilter;
import com.llmanalytics.config.SecurityConfig;
import com.llmanalytics.infra.seed.SampleDataLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.llmanalytics.api.controller.AdminController.class)
@Import({SecurityConfig.class, ApiKeyAuthFilter.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SampleDataLoader sampleDataLoader;

    private static final String VALID_KEY = "dev-key-change-in-production";

    @Test
    void loadSampleData_emptySystem_returns201() throws Exception {
        SampleDataResponse response = new SampleDataResponse(3, 4, 8, 60, null);
        when(sampleDataLoader.load()).thenReturn(response);

        mockMvc.perform(post("/api/admin/sample-data")
                        .header("X-API-Key", VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.projectCount").value(3))
                .andExpect(jsonPath("$.data.interactionCount").value(60));
    }

    @Test
    void loadSampleData_dataExists_returns200WithWarning() throws Exception {
        SampleDataResponse response = new SampleDataResponse(3, 4, 8, 60, "Data already exists");
        when(sampleDataLoader.load()).thenReturn(response);

        mockMvc.perform(post("/api/admin/sample-data")
                        .header("X-API-Key", VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Data already exists"));
    }

    @Test
    void loadSampleData_missingApiKey_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/sample-data")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
