package com.prodash.service;

import com.google.gson.Gson;
import com.prodash.dto.llm.ChatRequest;
import com.prodash.dto.llm.ChatResponse;
import com.prodash.dto.llm.Message;
import com.prodash.model.JournalEntry;
import com.prodash.model.Proposal;
import com.prodash.repository.JournalRepository;
import com.prodash.repository.ProposalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JournalService {

    private static final Logger logger = LoggerFactory.getLogger(JournalService.class);
    private final JournalRepository journalRepository;
    private final ProposalRepository proposalRepository;
    private final MongoTemplate mongoTemplate;
    private final LlmService llmService; // Re-use the existing LlmService for the second call
    private final PromptManager promptManager;
    private final Gson gson = new Gson();

    @Value("${llm.model.name}")
    private String modelName;

    public JournalService(JournalRepository journalRepository, ProposalRepository proposalRepository, MongoTemplate mongoTemplate, LlmService llmService, PromptManager promptManager) {
        this.journalRepository = journalRepository;
        this.proposalRepository = proposalRepository;
        this.mongoTemplate = mongoTemplate;
        this.llmService = llmService;
        this.promptManager = promptManager;
    }

    /**
     * Creates a journal entry for today's legislative activities.
     */
    public void createDailyJournal() {
        LocalDate today = LocalDate.now();
        logger.info("Checking if a journal entry for {} is needed.", today);

        // 1. Check if a journal for today already exists to prevent duplicates.
        if (journalRepository.findByDate(today).isPresent()) {
            logger.info("Journal entry for {} already exists. Skipping.", today);
            return;
        }

        // 2. Find all proposals that were analyzed today.
        Query query = new Query(Criteria.where("updatedAt").gte(today.atStartOfDay()).lt(today.plusDays(1).atStartOfDay()))
                .with(Sort.by(Sort.Direction.DESC, "updatedAt"));
        List<Proposal> analyzedProposals = mongoTemplate.find(query, Proposal.class);

        if (analyzedProposals.isEmpty()) {
            logger.info("No newly analyzed proposals found for {}. No journal will be created.", today);
            return;
        }

        logger.info("Found {} proposals analyzed today to include in the journal.", analyzedProposals.size());

        // 3. Prepare the data for the journal prompt
        String proposalsJson = gson.toJson(analyzedProposals.stream()
                .map(p -> Map.of("resumo", p.getSummaryLLM(), "categoria", p.getCategoryLLM()))
                .collect(Collectors.toList()));
        
        String promptTemplate = promptManager.getPrompt("daily_journal_v1");
        String finalPrompt = promptTemplate.replace("{propostas_json}", proposalsJson);

        // 4. Make the second LLM call to generate the narrative summary
        try {
            // This part is simplified from LlmService for clarity, can be refactored
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("YOUR_OPENROUTER_API_KEY"); // Assumes key is available
            headers.setContentType(MediaType.APPLICATION_JSON);
            Message userMessage = new Message("user", finalPrompt);
            ChatRequest requestPayload = new ChatRequest(modelName, List.of(userMessage));
            HttpEntity<ChatRequest> entity = new HttpEntity<>(requestPayload, headers);
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity("https://openrouter.ai/api/v1/chat/completions", entity, ChatResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String journalJson = response.getBody().getChoices().get(0).getMessage().getContent();
                
                // 5. Parse the journal JSON and save it
                Map<String, String> journalData = gson.fromJson(journalJson, Map.class);
                
                JournalEntry newEntry = new JournalEntry();
                newEntry.setDate(today);
                newEntry.setTitle(journalData.get("titulo"));
                newEntry.setNarrativeSummary(journalData.get("resumo_narrativo"));
                newEntry.setRelatedProposalIds(analyzedProposals.stream().map(Proposal::getOriginalId).collect(Collectors.toList()));
                
                journalRepository.save(newEntry);
                logger.info("Successfully created and saved journal entry for {}.", today);
            }
        } catch (Exception e) {
            logger.error("Failed to generate or save daily journal.", e);
        }
    }
}