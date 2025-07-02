// src/main/java/com/prodash/infrastructure/adapter/out/llm/LlmMapper.java
package com.prodash.infrastructure.adapter.out.llm;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.prodash.domain.model.Proposal;
import com.prodash.infrastructure.adapter.out.llm.dto.AnalysisPayload;
import com.prodash.infrastructure.adapter.out.llm.dto.LlmApiRequest;
import com.prodash.infrastructure.adapter.out.llm.dto.LlmApiResponse;
import com.prodash.infrastructure.config.PromptManager;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LlmMapper {

    private static final Logger log = LoggerFactory.getLogger(LlmMapper.class);
    private final Gson gson = new Gson();
    private final PromptManager promptManager;

    public LlmMapper(PromptManager promptManager) {
        this.promptManager = promptManager;
    }

    /**
     * Creates an LlmApiRequest from a list of proposals and a prompt name.
     */
    public LlmApiRequest toApiRequest(List<Proposal> proposals, String promptName, String modelName) {
        List<AnalysisPayload> payloads = proposals.stream()
                .filter(p -> p.getSummary() != null && !p.getSummary().isBlank())
                .map(p -> new AnalysisPayload(p.getId(), p.getSummary()))
                .collect(Collectors.toList());

        String payloadJson = gson.toJson(payloads);
        String promptTemplate = promptManager.getPrompt(promptName);
        String finalPrompt = promptTemplate.replace("{ementas_json}", payloadJson);

        LlmApiRequest.Message userMessage = new LlmApiRequest.Message("user", finalPrompt);
        return new LlmApiRequest(modelName, List.of(userMessage));
    }

    /**
     * Updates proposals with the results from the LLM API response.
     */
    public List<Proposal> updateProposalsFromApiResponse(LlmApiResponse response, List<Proposal> originalProposals) {
        List<LlmResult> llmResults = parseResultsFromResponse(response);
        
        if (llmResults.isEmpty()) {
            return originalProposals;
        }

        Map<String, LlmResult> resultMap = llmResults.stream()
                .filter(r -> r.getProposalId() != null && !r.getProposalId().isBlank())
                .collect(Collectors.toMap(LlmResult::getProposalId, Function.identity(), (first, second) -> {
                    log.warn("Duplicate proposalId found in LLM response: {}. Using the first entry.", first.getProposalId());
                    return first;
                }));

        originalProposals.forEach(proposal -> {
            LlmResult result = resultMap.get(proposal.getId());
            if (result != null) {
                // Update summary if available
                if (result.getSummary() != null) {
                    proposal.setSummary(result.getSummary());
                }
                // Update score and justification if available
                if (result.getImpactScore() != null) {
                    proposal.setImpactScore(result.getImpactScore().doubleValue());
                    proposal.setJustification(result.getJustification());
                }
            }
        });
        
        return originalProposals;
    }

    private List<LlmResult> parseResultsFromResponse(LlmApiResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            log.warn("LLM response is empty or invalid.");
            return List.of();
        }

        String content = response.getChoices().get(0).getMessage().getContent();
        try {
            // The LLM often wraps the JSON array in ```json ... ```, so we extract it.
            int startIndex = content.indexOf('[');
            int endIndex = content.lastIndexOf(']');

            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                String jsonArrayString = content.substring(startIndex, endIndex + 1);
                Type resultListType = new TypeToken<List<LlmResult>>() {}.getType();
                return gson.fromJson(jsonArrayString, resultListType);
            } else {
                log.error("Could not find a valid JSON array in the LLM response content.");
                return List.of();
            }
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse JSON from LLM response: {}", content, e);
            return List.of();
        }
    }

    /**
     * Internal DTO to represent a single result item from the LLM's JSON output.
     */
    @Getter
    private static class LlmResult {
        private String proposalId;
        private String summary;
        private Integer impactScore;
        private String justification;
    }
}