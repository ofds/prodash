package com.prodash.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prodash.dto.llm.*;
import com.prodash.model.Proposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LlmService {

    private static final Logger logger = LoggerFactory.getLogger(LlmService.class);
    private final RestTemplate restTemplate;
    private final PromptManager promptManager;
    private final Gson gson = new Gson();

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${llm.model.name}")
    private String modelName;

    public LlmService(RestTemplate restTemplate, PromptManager promptManager) {
        this.restTemplate = restTemplate;
        this.promptManager = promptManager;
    }

    public List<LlmResult> processBatch(List<Proposal> proposals, String promptName) {
        logger.debug("Starting processBatch with promptName='{}'", promptName);
    
        if (proposals == null || proposals.isEmpty()) {
            logger.warn("Received null or empty proposals list.");
            return Collections.emptyList();
        }
    
        logger.debug("Received {} proposals for processing.", proposals.size());
    
        // Filter proposals with non-blank ementa
        List<Proposal> validProposals = proposals.stream()
                .filter(p -> p.getEmenta() != null && !p.getEmenta().isBlank())
                .collect(Collectors.toList());
    
        logger.debug("Filtered valid proposals: {}/{}", validProposals.size(), proposals.size());
    
        if (validProposals.isEmpty()) {
            logger.warn("No valid proposals with a non-blank ementa found.");
            return Collections.emptyList();
        }
    
        String promptTemplate = promptManager.getPrompt(promptName);
        if (promptTemplate == null || promptTemplate.isBlank()) {
            logger.error("Prompt '{}' not found or is blank.", promptName);
            return Collections.emptyList();
        }
        logger.debug("Using prompt template: {}", promptTemplate);
    
        List<EmentaInput> ementaInputs = validProposals.stream()
                .map(p -> new EmentaInput(p.getOriginalId(), p.getEmenta()))
                .collect(Collectors.toList());
    
        String ementasJson = gson.toJson(ementaInputs);
        logger.debug("Ementas JSON: {}", ementasJson);
    
        String finalPrompt = promptTemplate.replace("{ementas_json}", ementasJson);
        logger.trace("Final prompt: {}", finalPrompt);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        Message userMessage = new Message("user", finalPrompt);
        ChatRequest requestPayload = new ChatRequest(modelName, List.of(userMessage));
        HttpEntity<ChatRequest> entity = new HttpEntity<>(requestPayload, headers);
    
        try {
            logger.info("Sending batch of {} proposals to LLM using prompt '{}'", validProposals.size(), promptName);
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(apiUrl, entity, ChatResponse.class);
    
            logger.debug("Received response with status: {}", response.getStatusCode());
    
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null
                    && !response.getBody().getChoices().isEmpty()) {
    
                String llmJsonOutput = response.getBody().getChoices().get(0).getMessage().getContent();
                logger.debug("LLM raw response: {}", llmJsonOutput);
    
                int startIndex = llmJsonOutput.indexOf("[");
                int endIndex = llmJsonOutput.lastIndexOf("]");
    
                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    String jsonArrayString = llmJsonOutput.substring(startIndex, endIndex + 1);
                    logger.debug("Extracted JSON array: {}", jsonArrayString);
    
                    Type resultListType = new TypeToken<List<LlmResult>>() {}.getType();
                    List<LlmResult> results = gson.fromJson(jsonArrayString, resultListType);
                    logger.info("Parsed {} LLM results successfully.", results.size());
                    return results;
                } else {
                    logger.error("Failed to extract valid JSON array from LLM response.");
                    return Collections.emptyList();
                }
            } else {
                logger.error("LLM API returned unexpected response or empty body.");
            }
        } catch (Exception e) {
            logger.error("Exception while calling LLM API: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
        }
    
        logger.warn("Returning empty result due to earlier failure.");
        return Collections.emptyList();
    }
    
}