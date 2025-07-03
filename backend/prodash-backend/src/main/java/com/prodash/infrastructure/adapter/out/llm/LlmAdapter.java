package com.prodash.infrastructure.adapter.out.llm;

import com.prodash.application.port.out.LlmPort;
import com.prodash.config.BatchSizeManager;
import com.prodash.domain.model.Proposal;
import com.prodash.infrastructure.adapter.out.llm.dto.LlmApiRequest;
import com.prodash.infrastructure.adapter.out.llm.dto.LlmApiResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LlmAdapter implements LlmPort {

    private static final Logger log = LoggerFactory.getLogger(LlmAdapter.class);

    private final RestTemplate restTemplate;
    private final LlmMapper llmMapper;
    private final BatchSizeManager batchSizeManager;

    // Use constants for prompt names to avoid typos and errors
    private static final String SUMMARIZE_PROMPT = "summarize_proposals_prompt";
    private static final String SCORE_PROMPT = "impact_score_prompt";

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${llm.model.name}")
    private String modelName;

    public LlmAdapter(RestTemplate restTemplate, LlmMapper llmMapper, BatchSizeManager batchSizeManager) {
        this.restTemplate = restTemplate;
        this.llmMapper = llmMapper;
        this.batchSizeManager = batchSizeManager;
    }

    @Override
    public List<Proposal> summarizeProposals(List<Proposal> proposals) {
        // Pass the constant to ensure correctness
        return processBatch(proposals, SUMMARIZE_PROMPT);
    }

    @Override
    public List<Proposal> scoreProposals(List<Proposal> proposals) {
        // Pass the constant to ensure correctness
        return processBatch(proposals, SCORE_PROMPT);
    }

    @Retry(name = "llm-api", fallbackMethod = "processBatchFallback")
    public List<Proposal> processBatch(List<Proposal> proposals, String promptName) {
        log.debug("Attempting to process batch for prompt: {}", promptName);

        // **FIXED LOGIC:** This filter now correctly selects proposals based on the task.
        List<Proposal> validProposals;
        if (SUMMARIZE_PROMPT.equals(promptName)) {
            // For summarization, find proposals with an 'ementa' but no 'summary'.
            validProposals = proposals.stream()
                .filter(p -> p.getEmenta() != null && !p.getEmenta().isBlank() && p.getSummary() == null)
                .collect(Collectors.toList());
        } else {
            // For scoring, find proposals that already have a 'summary'.
            validProposals = proposals.stream()
                .filter(p -> p.getSummary() != null && !p.getSummary().isBlank())
                .collect(Collectors.toList());
        }

        if (validProposals.isEmpty()) {
            log.warn("No valid proposals to process for prompt: {}. Skipping LLM call.", promptName);
            // Return the original list to ensure proposals not meant for this step are preserved.
            return proposals;
        }

        LlmApiRequest request = llmMapper.toApiRequest(validProposals, promptName, modelName);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LlmApiRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<LlmApiResponse> response = restTemplate.postForEntity(apiUrl, entity, LlmApiResponse.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return llmMapper.updateProposalsFromApiResponse(response.getBody(), proposals);
        } else {
            log.error("Received non-OK status from LLM API: {}", response.getStatusCode());
            throw new HttpClientErrorException(response.getStatusCode(), "LLM API returned non-OK status.");
        }
    }

    // Fallback method for retries
    public List<Proposal> processBatchFallback(List<Proposal> proposals, String promptName, Throwable t) {
        log.error("All retry attempts failed for prompt: {}. Error: {}", promptName, t.getMessage());
        
        if (t instanceof HttpClientErrorException e && (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS || e.getStatusCode().is5xxServerError())) {
            log.warn("API capacity exceeded - reducing batch size.");
            batchSizeManager.decreaseBatchSize();
        }

        // Re-throw the original exception to ensure the calling service is aware of the failure.
        throw new RuntimeException("Failed to process batch after multiple retries.", t);
    }
}