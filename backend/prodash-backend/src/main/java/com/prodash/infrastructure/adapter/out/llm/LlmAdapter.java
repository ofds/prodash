package com.prodash.infrastructure.adapter.out.llm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prodash.application.port.out.LlmPort;
import com.prodash.domain.model.Proposal;
import com.prodash.infrastructure.adapter.out.llm.dto.*;
import com.prodash.infrastructure.config.PromptManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LlmAdapter implements LlmPort {

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
        Map<String, LlmResult> resultMap = llmResults.stream().collect(Collectors.toMap(LlmResult::getId, Function.identity()));
        
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
        Map<String, LlmResult> resultMap = llmResults.stream().collect(Collectors.toMap(LlmResult::getId, Function.identity()));

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
        if (proposals == null || proposals.isEmpty()) return Collections.emptyList();

        List<Proposal> validProposals = proposals.stream()
                .filter(p -> p.getSummary() != null && !p.getSummary().isBlank()) // Use summary as input
                .collect(Collectors.toList());

        if (validProposals.isEmpty()) return Collections.emptyList();

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
                String llmJsonOutput = response.getBody().getChoices().get(0).getMessage().getContent();
                
                int startIndex = llmJsonOutput.indexOf("[");
                int endIndex = llmJsonOutput.lastIndexOf("]");

                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    String jsonArrayString = llmJsonOutput.substring(startIndex, endIndex + 1);
                    Type resultListType = new TypeToken<List<LlmResult>>() {}.getType();
                    return gson.fromJson(jsonArrayString, resultListType);
                }
            }
        } catch (Exception e) {
            System.err.println("Exception while calling LLM API: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}