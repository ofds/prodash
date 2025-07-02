package com.prodash.infrastructure.adapter.out.llm;

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
import java.util.function.Function;
import java.util.stream.Collectors;

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
        List<LlmResult> llmResults = processBatch(proposals, "summarize_proposals_prompt");
        Map<String, LlmResult> resultMap = llmResults.stream()
                .collect(Collectors.toMap(LlmResult::getId, Function.identity()));
        
        proposals.forEach(p -> {
            LlmResult result = resultMap.get(p.getId());
            if (result != null) {
                p.setSummary(result.getSummary());
            }
        });
        return proposals;
    }

    @Override
    public List<Proposal> scoreProposals(List<Proposal> proposals) {
        List<LlmResult> llmResults = processBatch(proposals, "impact_score_prompt");
        Map<String, LlmResult> resultMap = llmResults.stream()
                .collect(Collectors.toMap(LlmResult::getId, Function.identity()));

        proposals.forEach(p -> {
            LlmResult result = resultMap.get(p.getId());
            if (result != null) {
                p.setImpactScore(result.getImpactScore());
                p.setJustification(result.getJustification());
            }
        });
        return proposals;
    }

    private List<LlmResult> processBatch(List<Proposal> proposals, String promptName) {
        if (proposals == null || proposals.isEmpty()) {
            return Collections.emptyList();
        }

        // Use summary as input for the LLM
        List<Proposal> validProposals = proposals.stream()
                .filter(p -> p.getSummary() != null && !p.getSummary().isBlank())
                .collect(Collectors.toList());

        if (validProposals.isEmpty()) {
            return Collections.emptyList();
        }

        String promptTemplate = promptManager.getPrompt(promptName);
        List<EmentaInput> ementaInputs = validProposals.stream()
                .map(p -> new EmentaInput(p.getId(), p.getSummary()))
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
                // Directly get the content which is expected to be a JSON array string
                String llmJsonOutput = response.getBody().getChoices().get(0).getMessage().getContent();
                
                // **REFACTORED SECTION**: Removed fragile string manipulation.
                // Directly parse the JSON string into the target list type.
                if (llmJsonOutput != null && !llmJsonOutput.isBlank()) {
                    Type resultListType = new TypeToken<List<LlmResult>>() {}.getType();
                    return gson.fromJson(llmJsonOutput, resultListType);
                }
            }
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse JSON from LLM response. Content may not be a valid JSON array.", e);
        } catch (RestClientException e) {
            log.error("Exception while calling LLM API: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }
}