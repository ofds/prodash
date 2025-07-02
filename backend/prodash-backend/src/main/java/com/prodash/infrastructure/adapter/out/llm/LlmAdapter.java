package com.prodash.infrastructure.adapter.out.llm;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.prodash.application.port.out.LlmPort;
import com.prodash.domain.model.Proposal;
import com.prodash.infrastructure.adapter.out.llm.dto.*;
import com.prodash.infrastructure.config.PromptManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

@Component
public class LlmAdapter implements LlmPort {

    private static final Logger log = LoggerFactory.getLogger(LlmAdapter.class);

    private final RestTemplate restTemplate;
    private final PromptManager promptManager;
    private final Gson gson = new Gson();

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${llm.model.name}")
    private String modelName;

    public LlmAdapter(RestTemplate restTemplate, PromptManager promptManager) {
        this.restTemplate = restTemplate;
        this.promptManager = promptManager;
    }

    @Override
    public List<Proposal> summarizeProposals(List<Proposal> proposals) {
        List<LlmResult> allResults = processSingleBatch(proposals, "summarize_proposals_prompt");
        Map<String, LlmResult> resultMap = createValidatedResultMap(allResults);
        
        proposals.forEach(p -> {
            LlmResult result = resultMap.get(p.getId());
            if (result != null && result.getSummary() != null) {
                p.setSummary(result.getSummary());
            }
        });
        return proposals;
    }

    @Override
    public List<Proposal> scoreProposals(List<Proposal> proposals) {
        List<LlmResult> allResults = processSingleBatch(proposals, "impact_score_prompt");
        Map<String, LlmResult> resultMap = createValidatedResultMap(allResults);

        proposals.forEach(p -> {
            LlmResult result = resultMap.get(p.getId());
            if (result != null) {
                p.setImpactScore(result.getImpactScore());
                p.setJustification(result.getJustification());
            }
        });
        return proposals;
    }

    private List<LlmResult> processSingleBatch(List<Proposal> proposalBatch, String promptName) {
        log.debug("Processing a batch of {} proposals.", proposalBatch.size());
        
        List<Proposal> validProposals = proposalBatch.stream()
                .filter(p -> p.getSummary() != null && !p.getSummary().isBlank())
                .collect(Collectors.toList());

        if (validProposals.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, String> correlationIdToProposalId = validProposals.stream()
                .collect(Collectors.toMap(p -> UUID.randomUUID().toString(), Proposal::getId));
        
        Map<String, Proposal> proposalIdToProposal = validProposals.stream()
                .collect(Collectors.toMap(Proposal::getId, Function.identity()));

        String promptTemplate = promptManager.getPrompt(promptName);
        List<EmentaInput> ementaInputs = correlationIdToProposalId.entrySet().stream()
                .map(entry -> {
                    String correlationId = entry.getKey();
                    String proposalId = entry.getValue();
                    Proposal p = proposalIdToProposal.get(proposalId);
                    return new EmentaInput(p.getId(), p.getSummary(), correlationId);
                })
                .collect(Collectors.toList());

        String ementasJson = gson.toJson(ementaInputs);
        String finalPrompt = promptTemplate.replace("{ementas_json}", ementasJson);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Message userMessage = new Message("user", finalPrompt);
        ChatRequest requestPayload = new ChatRequest(modelName, List.of(userMessage));
        HttpEntity<ChatRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(apiUrl, entity, ChatResponse.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && !response.getBody().getChoices().isEmpty()) {
                String llmFullResponse = response.getBody().getChoices().get(0).getMessage().getContent();
                
                int startIndex = llmFullResponse.indexOf('[');
                int endIndex = llmFullResponse.lastIndexOf(']');

                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    String jsonArrayString = llmFullResponse.substring(startIndex, endIndex + 1);
                    Type resultListType = new TypeToken<List<LlmResult>>() {}.getType();
                    List<LlmResult> results = gson.fromJson(jsonArrayString, resultListType);
                    
                    results.forEach(r -> {
                        String originalId = correlationIdToProposalId.get(r.getCorrelationId());
                        if(originalId != null) {
                             try {
                                java.lang.reflect.Field idField = LlmResult.class.getDeclaredField("id");
                                idField.setAccessible(true);
                                idField.set(r, originalId);
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                log.error("Could not set original ID on LlmResult via reflection", e);
                            }
                        }
                    });
                    return results;
                }
            }
        } catch (Exception e) {
            log.error("API call or processing failed for a batch.", e);
        }
        return Collections.emptyList();
    }
    
    private Map<String, LlmResult> createValidatedResultMap(List<LlmResult> llmResults) {
        if (llmResults == null || llmResults.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<String> seenIds = new HashSet<>();
        List<LlmResult> distinctResults = new ArrayList<>();
        for (LlmResult result : llmResults) {
            if (result.getId() != null && seenIds.add(result.getId())) {
                distinctResults.add(result);
            } else {
                log.warn("LLM returned duplicate or null ID for a result. Ignoring subsequent entry for ID: {}", result.getId());
            }
        }
        
        return distinctResults.stream()
                .collect(Collectors.toMap(LlmResult::getId, Function.identity()));
    }
}