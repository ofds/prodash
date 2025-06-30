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
        if (proposals == null || proposals.isEmpty()) {
            return Collections.emptyList();
        }

        String promptTemplate = promptManager.getPrompt(promptName);
        List<EmentaInput> ementaInputs = proposals.stream()
                .map(p -> new EmentaInput(p.getOriginalId(), p.getEmenta()))
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
            logger.info("Sending batch of {} proposals to LLM using prompt '{}'...", proposals.size(), promptName);
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(apiUrl, entity, ChatResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && !response.getBody().getChoices().isEmpty()) {
                String llmJsonOutput = response.getBody().getChoices().get(0).getMessage().getContent();
                logger.debug("LLM raw response: {}", llmJsonOutput);

                // [FIX] Find the start and end of the JSON array to handle extra text from the LLM
                int startIndex = llmJsonOutput.indexOf("[");
                int endIndex = llmJsonOutput.lastIndexOf("]");

                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    String jsonArrayString = llmJsonOutput.substring(startIndex, endIndex + 1);
                    
                    Type resultListType = new TypeToken<List<LlmResult>>(){}.getType();
                    List<LlmResult> results = gson.fromJson(jsonArrayString, resultListType);
                    logger.info("Successfully processed and parsed {} results from LLM.", results.size());
                    return results;
                } else {
                    logger.error("Could not find a valid JSON array in the LLM response.");
                    return Collections.emptyList();
                }
            }
        } catch (Exception e) {
            logger.error("Error calling LLM API: " + e.getClass().getName() + " - " + e.getMessage());
        }

        return Collections.emptyList();
    }
}