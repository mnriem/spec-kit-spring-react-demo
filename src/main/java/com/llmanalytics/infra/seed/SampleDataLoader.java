package com.llmanalytics.infra.seed;

import com.llmanalytics.api.dto.CreateInteractionRequest;
import com.llmanalytics.api.dto.SampleDataResponse;
import com.llmanalytics.api.dto.ToolCallRequest;
import com.llmanalytics.domain.repository.ProjectRepository;
import com.llmanalytics.domain.service.InteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleDataLoader {

    private final InteractionService interactionService;
    private final ProjectRepository projectRepository;

    private static final Random RNG = new Random(42);

    private static final List<String> MODELS = List.of(
            "gpt-4o", "gpt-4o-mini", "claude-3-5-sonnet-20241022"
    );

    private record ProjectSpec(String name, String description, List<ExperimentSpec> experiments) {}
    private record ExperimentSpec(String name, String description, List<String> iterations) {}

    private static final List<ProjectSpec> PROJECTS = List.of(
            new ProjectSpec("Customer Support Bot", "Automated customer support assistant",
                    List.of(
                            new ExperimentSpec("Response Quality", "Testing response quality",
                                    List.of("Baseline v1", "Optimised v2", "With Examples")),
                            new ExperimentSpec("Tone Variations", "Testing different tones",
                                    List.of("Formal", "Friendly"))
                    )),
            new ProjectSpec("Code Review Assistant", "Automated code review feedback",
                    List.of(
                            new ExperimentSpec("Security Focus", "Security-oriented prompts",
                                    List.of("Base Security", "OWASP Enhanced")),
                            new ExperimentSpec("Performance Focus", "Performance-oriented prompts",
                                    List.of("General", "With Profiling Tips"))
                    )),
            new ProjectSpec("Document Summarizer", "Summarise long documents",
                    List.of(
                            new ExperimentSpec("Summary Length", "Vary output length",
                                    List.of("Short (1 para)", "Medium (3 para)", "Detailed")),
                            new ExperimentSpec("Extraction Focus", "Key info extraction",
                                    List.of("Action Items", "Key Facts"))
                    ))
    );

    public SampleDataResponse load() {
        if (projectRepository.count() > 0) {
            return new SampleDataResponse(0, 0, 0, 0, "Data already exists — skipping seed");
        }

        AtomicInteger totalInteractions = new AtomicInteger(0);
        int projectCount = PROJECTS.size();
        int experimentCount = PROJECTS.stream().mapToInt(p -> p.experiments().size()).sum();
        int iterationCount = PROJECTS.stream()
                .flatMap(p -> p.experiments().stream())
                .mapToInt(e -> e.iterations().size())
                .sum();

        for (ProjectSpec project : PROJECTS) {
            for (ExperimentSpec experiment : project.experiments()) {
                for (String iteration : experiment.iterations()) {
                    // 8-12 interactions per iteration
                    int count = 8 + RNG.nextInt(5);
                    for (int i = 0; i < count; i++) {
                        try {
                            interactionService.ingest(buildInteraction(
                                    project.name(), project.description(),
                                    experiment.name(), experiment.description(),
                                    iteration, i));
                            totalInteractions.incrementAndGet();
                        } catch (Exception e) {
                            log.warn("Failed to seed interaction: {}", e.getMessage());
                        }
                    }
                }
            }
        }

        log.info("Sample data loaded: {} projects, {} experiments, {} iterations, {} interactions",
                projectCount, experimentCount, iterationCount, totalInteractions.get());

        return new SampleDataResponse(projectCount, experimentCount, iterationCount,
                totalInteractions.get(), null);
    }

    private CreateInteractionRequest buildInteraction(String projectName, String projectDesc,
                                                       String experimentName, String experimentDesc,
                                                       String iterationName, int index) {
        String model = MODELS.get(RNG.nextInt(MODELS.size()));
        int tokensIn = 200 + RNG.nextInt(800);
        int tokensOut = 50 + RNG.nextInt(500);
        int latencyMs = 100 + RNG.nextInt(2000);
        OffsetDateTime started = OffsetDateTime.now(ZoneOffset.UTC)
                .minusDays(RNG.nextInt(30))
                .minusHours(RNG.nextInt(24));
        OffsetDateTime ended = started.plusNanos((long) latencyMs * 1_000_000);

        // ~30% of interactions have tool calls
        List<ToolCallRequest> toolCalls = null;
        if (RNG.nextInt(10) < 3) {
            String[] tools = {"search_web", "read_file", "execute_code", "call_api", "query_database"};
            int numTools = 1 + RNG.nextInt(2);
            toolCalls = new java.util.ArrayList<>();
            for (int t = 0; t < numTools; t++) {
                String toolName = tools[RNG.nextInt(tools.length)];
                toolCalls.add(new ToolCallRequest(
                        toolName,
                        Map.of("query", "sample query " + index),
                        Map.of("result", "sample result"),
                        started.plusNanos((long)(latencyMs * 0.3 * 1_000_000))
                ));
            }
        }

        return new CreateInteractionRequest(
                projectName, projectDesc,
                experimentName, experimentDesc,
                iterationName, null,
                model,
                "Sample prompt #" + index + " for " + iterationName,
                Map.of("finish_reason", "stop", "usage", Map.of("total_tokens", tokensIn + tokensOut)),
                tokensIn, tokensOut,
                started, ended,
                toolCalls
        );
    }
}
