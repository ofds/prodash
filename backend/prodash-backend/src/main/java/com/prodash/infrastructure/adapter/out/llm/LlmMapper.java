package com.prodash.infrastructure.adapter.out.llm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import com.prodash.domain.model.AnalysisResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LlmMapper {

    private static final Logger log = LoggerFactory.getLogger(LlmMapper.class);
    private final Gson gson = new GsonBuilder().setLenient().create();
    private final PromptManager promptManager;

    // Use constants for prompt names to avoid typos
    private static final String SUMMARIZE_PROMPT = "summarize_proposals_prompt";
    private static final String SCORE_PROMPT = "impact_score_prompt";

    public LlmMapper(PromptManager promptManager) {
        this.promptManager = promptManager;
    }

    /**
     * Creates an LlmApiRequest from a list of proposals and a prompt name.
     */
    public LlmApiRequest toApiRequest(List<Proposal> proposals, String promptName, String modelName) {

        // **FIXED LOGIC:** Create payload based on the specific task (summarize vs.
        // score)
        List<AnalysisPayload> payloads;
        String placeholder;

        if (SUMMARIZE_PROMPT.equals(promptName)) {
            // For summarization, use the proposal's 'ementa' as the text to be analyzed.
            payloads = proposals.stream()
                    .filter(p -> p.getEmenta() != null && !p.getEmenta().isBlank())
                    .map(p -> new AnalysisPayload(p.getId(), p.getEmenta()))
                    .collect(Collectors.toList());
            placeholder = "{ementas_json}"; // Assumes this is the placeholder in your summarization prompt
        } else {
            // For scoring, use the proposal's 'summary' as the text.
            payloads = proposals.stream()
                    .filter(p -> p.getSummary() != null && !p.getSummary().isBlank())
                    .map(p -> new AnalysisPayload(p.getId(), p.getSummary()))
                    .collect(Collectors.toList());
            placeholder = "{summaries_json}"; // Use a different placeholder for clarity if needed
        }

        if (payloads.isEmpty()) {
            log.warn("No valid proposals found to create a payload for prompt: {}. The list will be empty.",
                    promptName);
        }

        String payloadJson = gson.toJson(payloads);
        String promptTemplate = promptManager.getPrompt(promptName);
        String finalPrompt = promptTemplate.replace(placeholder, payloadJson);

        LlmApiRequest.Message userMessage = new LlmApiRequest.Message("user", finalPrompt);
        return new LlmApiRequest(modelName, List.of(userMessage));
    }

    /**
     * Updates a list of proposals with summaries and impact scores from the LLM API
     * response.
     * This method efficiently maps results back to the original proposals using
     * their unique IDs.
     *
     * @param response          The raw API response from the LLM.
     * @param originalProposals The list of proposals that were sent for processing.
     * @return The original list of proposals, now updated with the new data.
     */
    public List<Proposal> updateProposalsFromApiResponse(LlmApiResponse response, List<Proposal> originalProposals) {
        // 1. Parse the complex API response into a simple list of result objects.
        // CORRECTION: Using the correct method name `parseResultsFromResponse`.
        List<LlmResult> llmResults = parseResultsFromResponse(response);

        if (llmResults == null || llmResults.isEmpty()) {
            log.warn("LLM response contained no results to process.");
            return originalProposals;
        }

        // 2. Create a Map of the results from the LLM, keyed by the proposal's ID.
        // CORRECTION: Using the correct getter `getProposalId()`.
        Map<String, LlmResult> resultMap = llmResults.stream()
                .filter(r -> r.getId() != null && !r.getId().isBlank())
                .collect(Collectors.toMap(
                        LlmResult::getId, // Key of the map is the proposal ID
                        Function.identity(), // Value is the result object itself
                        (existing, replacement) -> { // Handle cases where the LLM returns duplicate IDs
                            log.warn("Duplicate proposalId found in LLM response: {}. Using the first result.",
                                    existing.getId());
                            return existing;
                        }));

        // 3. Iterate through the original list of proposals and update each one
        // with the corresponding result found in the map.
        for (Proposal proposal : originalProposals) {
            LlmResult result = resultMap.get(proposal.getId());
            if (result != null) {
                // Update summary if it's provided in the result
                if (result.getSummary() != null && !result.getSummary().isBlank()) {
                    proposal.setSummary(result.getSummary());
                }
                // Update impact score and justification if provided
                if (result.getImpactScore() != null) {
                    proposal.setImpactScore(result.getImpactScore().doubleValue());
                    proposal.setJustification(result.getJustification());
                }
            }
        }

        // 4. Return the mutated list of original proposals.
        return originalProposals;
    }

    private List<LlmResult> parseResultsFromResponse(LlmApiResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            log.warn("LLM response is empty or has no choices.");
            return List.of();
        }

        String content = response.getChoices().get(0).getMessage().getContent();
        try {
            // This defensive code attempts to extract a JSON array from the LLM response,
            // even if it's wrapped in markdown code fences.
            int startIndex = content.indexOf('[');
            int endIndex = content.lastIndexOf(']');

            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                String jsonArrayString = content.substring(startIndex, endIndex + 1);
                Type resultListType = new TypeToken<List<LlmResult>>() {
                }.getType();
                return gson.fromJson(jsonArrayString, resultListType);
            } else {
                log.error("Could not find a valid JSON array in the LLM response content. Content was: {}", content);
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
        private String id;
        private String summary;
        private Double impactScore;
        private String justification;
    }

    /**
     * Creates an LlmApiRequest for a single proposal using a user-defined prompt.
     * This is for the on-demand analysis feature.
     */
    public LlmApiRequest toApiRequest(Proposal proposal, String userPrompt, String modelName) {
        // The user's prompt is expected to contain a placeholder for the proposal's
        // text.
        String proposalText = proposal.getSummary() != null ? proposal.getSummary() : proposal.getEmenta();
        String finalPrompt = userPrompt.replace("{proposal_text}", proposalText);

        LlmApiRequest.Message userMessage = new LlmApiRequest.Message("user", finalPrompt);
        return new LlmApiRequest(modelName, List.of(userMessage));
    }

    /**
     * Parses the LLM API response and maps it to an AnalysisResult domain object.
     * This is designed to handle a single JSON object response, not an array.
     */
    public AnalysisResult toAnalysisResult(LlmApiResponse response, String proposalId, String jobId) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            log.warn("LLM response is empty or has no choices for proposalId: {}", proposalId);
            throw new IllegalArgumentException("Empty or invalid LLM response.");
        }

        String content = response.getChoices().get(0).getMessage().getContent();
        try {
            // Find the start and end of the JSON object
            int startIndex = content.indexOf('{');
            int endIndex = content.lastIndexOf('}');

            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                String jsonObjectString = content.substring(startIndex, endIndex + 1);
                JsonObject resultJson = JsonParser.parseString(jsonObjectString).getAsJsonObject();

                Double score = resultJson.has("impactScore") ? resultJson.get("impactScore").getAsDouble() : null;
                String justification = resultJson.has("justification") ? resultJson.get("justification").getAsString()
                        : null;

                if (score == null || justification == null) {
                    log.error("LLM response for proposal {} is missing 'impact_score' or 'justification'. Content: {}",
                            proposalId, content);
                    throw new IllegalArgumentException("Invalid LLM response format.");
                }

                return new AnalysisResult(null, jobId, proposalId, score, justification);
            } else {
                log.error("Could not find a valid JSON object in the LLM response. Content was: {}", content);
                throw new IllegalArgumentException("Could not find valid JSON object in response.");
            }
        } catch (Exception e) {
            log.error("Failed to parse JSON object from LLM response for proposal {}: {}", proposalId, content, e);
            throw new RuntimeException("Failed to parse JSON from LLM response.", e);
        }
    }
}