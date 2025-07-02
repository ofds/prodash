// src/main/java/com/prodash/infrastructure/adapter/out/llm/LlmAdapter.java
package com.prodash.infrastructure.adapter.out.llm;

import com.prodash.application.port.out.LlmPort;
import com.prodash.config.BatchSizeManager;
import com.prodash.domain.model.Proposal;
import com.prodash.infrastructure.adapter.out.llm.dto.LlmApiRequest;
import com.prodash.infrastructure.adapter.out.llm.dto.LlmApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LlmAdapter implements LlmPort {

    private static final Logger log = LoggerFactory.getLogger(LlmAdapter.class);

    private final RestTemplate restTemplate;
    private final LlmMapper llmMapper;
    private final BatchSizeManager batchSizeManager;

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
        return processBatch(proposals, "summarize_proposals_prompt");
    }

    @Override
    public List<Proposal> scoreProposals(List<Proposal> proposals) {
        return processBatch(proposals, "impact_score_prompt");
    }

    private List<Proposal> processBatch(List<Proposal> proposals, String promptName) {
        List<Proposal> validProposals = proposals.stream()
                .filter(p -> p.getSummary() != null && !p.getSummary().isBlank())
                .collect(Collectors.toList());

        if (validProposals.isEmpty()) {
            log.debug("No valid proposals to process for prompt: {}", promptName);
            return proposals;
        }

        LlmApiRequest request = llmMapper.toApiRequest(validProposals, promptName, modelName);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LlmApiRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<LlmApiResponse> response = restTemplate.postForEntity(apiUrl, entity, LlmApiResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return llmMapper.updateProposalsFromApiResponse(response.getBody(), proposals);
            } else {
                log.error("Received non-OK status from LLM API: {}", response.getStatusCode());
                // Throw an exception to signal failure to the calling service
                throw new HttpClientErrorException(response.getStatusCode(), "LLM API returned non-OK status.");
            }
        } catch (HttpClientErrorException e) {
            // Check for specific statuses that indicate the API is overloaded.
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS || e.getStatusCode().is5xxServerError()) {
                batchSizeManager.decreaseBatchSize();
            }
            // Re-throw the exception to let the service layer know the batch failed.
            throw e;
        } catch (RestClientException e) {
            log.error("A non-HTTP error occurred during API call for prompt: {}", promptName, e);
            // Re-throw to signal failure.
            throw e;
        }
    }
}