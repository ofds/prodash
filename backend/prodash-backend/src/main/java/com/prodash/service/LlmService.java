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
    private final PromptManager promptManager; // Inject PromptManager
    private final Gson gson = new Gson();

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${llm.model.name}")
    private String modelName;

    // The prompt template is no longer injected from properties
    // @Value("${llm.prompt.batch-process.template}")
    // private String promptTemplate;

    public LlmService(RestTemplate restTemplate, PromptManager promptManager) { // Update constructor
        this.restTemplate = restTemplate;
        this.promptManager = promptManager;
    }

    /**
     * Processes a batch of proposals using a dynamically selected prompt.
     * @param proposals A list of Proposal objects to be processed.
     * @param promptName The name of the prompt to use (e.g., "batch_summary_v1").
     * @return A list of LlmResult objects with the AI-generated data.
     */
    public List<LlmResult> processBatch(List<Proposal> proposals, String promptName) {
        if (proposals == null || proposals.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Get the prompt template from the PromptManager
        String promptTemplate = promptManager.getPrompt(promptName);

        // 2. Format the input for the prompt
        List<EmentaInput> ementaInputs = proposals.stream()
                .map(p -> new EmentaInput(p.getOriginalId(), p.getEmenta()))
                .collect(Collectors.toList());
        String ementasJson = gson.toJson(ementaInputs);

        // 3. Build the final prompt from the template
        String finalPrompt = promptTemplate.replace("{ementas_json}", ementasJson);

        // ... The rest of the method (API call and parsing) remains the same ...
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Message userMessage = new Message("user", finalPrompt);
        ChatRequest requestPayload = new ChatRequest(modelName, List.of(userMessage));

        HttpEntity<ChatRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            logger.info("Sending batch of {} proposals to LLM using prompt '{}'...", proposals.size(), promptName);
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(apiUrl, entity, ChatResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String llmJsonOutput = response.getBody().getChoices().get(0).getMessage().getContent();
                logger.debug("LLM raw response: {}", llmJsonOutput);

                Type resultListType = new TypeToken<List<LlmResult>>(){}.getType();
                List<LlmResult> results = gson.fromJson(llmJsonOutput, resultListType);
                logger.info("Successfully processed and parsed {} results from LLM.", results.size());
                return results;
            }
        } catch (Exception e) {
            logger.error("Error calling LLM API: {}", e.getMessage());
        }

        return Collections.emptyList();
    }
}