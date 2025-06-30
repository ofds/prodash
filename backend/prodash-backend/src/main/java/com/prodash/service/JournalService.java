package com.prodash.service;

import com.google.gson.Gson;
import com.prodash.dto.llm.ChatRequest;
import com.prodash.dto.llm.ChatResponse;
import com.prodash.dto.llm.Message;
import com.prodash.model.JournalEntry;
import com.prodash.model.Proposal;
import com.prodash.repository.JournalRepository;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JournalService {

    private static final Logger logger = LoggerFactory.getLogger(JournalService.class);
    private final JournalRepository journalRepository;
    private final MongoTemplate mongoTemplate;
    private final PromptManager promptManager;
    private final RestTemplate restTemplate;
    private final Gson gson = new Gson();

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${llm.model.name}")
    private String modelName;

    public JournalService(JournalRepository journalRepository, MongoTemplate mongoTemplate, PromptManager promptManager, RestTemplate restTemplate) {
        this.journalRepository = journalRepository;
        this.mongoTemplate = mongoTemplate;
        this.promptManager = promptManager;
        this.restTemplate = restTemplate;
    }

    public void createDailyJournal() {
        LocalDate today = LocalDate.now();
        logger.info("Checking if a journal entry for {} is needed.", today);

        if (journalRepository.findByDate(today).isPresent()) {
            logger.info("Journal entry for {} already exists. Skipping.", today);
            return;
        }

        Query query = new Query(Criteria.where("updatedAt").gte(today.atStartOfDay()).lt(today.plusDays(1).atStartOfDay()))
                .with(Sort.by(Sort.Direction.DESC, "updatedAt"));
        List<Proposal> analyzedProposals = mongoTemplate.find(query, Proposal.class);

        if (analyzedProposals.isEmpty()) {
            logger.info("No newly analyzed proposals found for {}. No journal will be created.", today);
            return;
        }

        logger.info("Found {} proposals analyzed today to include in the journal.", analyzedProposals.size());

        String proposalsJson = gson.toJson(analyzedProposals.stream()
                .map(p -> Map.of("resumo", p.getSummaryLLM(), "categoria", p.getCategoryLLM(), "impacto", p.getImpactoLLM()))
                .collect(Collectors.toList()));
        
        String promptTemplate = promptManager.getPrompt("daily_journal_v1");
        String finalPrompt = promptTemplate.replace("{propostas_json}", proposalsJson);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey); // [FIX] Use the injected API key
            headers.setContentType(MediaType.APPLICATION_JSON);
            Message userMessage = new Message("user", finalPrompt);
            ChatRequest requestPayload = new ChatRequest(modelName, List.of(userMessage));
            HttpEntity<ChatRequest> entity = new HttpEntity<>(requestPayload, headers);
            
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(apiUrl, entity, ChatResponse.class); // [FIX] Use the injected API URL

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && !response.getBody().getChoices().isEmpty()) {
                String journalJson = response.getBody().getChoices().get(0).getMessage().getContent();
                
                // [FIX] Add robust JSON parsing for the journal response
                int startIndex = journalJson.indexOf("{");
                int endIndex = journalJson.lastIndexOf("}");
                if (startIndex != -1 && endIndex != -1) {
                    String jsonObjectString = journalJson.substring(startIndex, endIndex + 1);
                    Map<String, String> journalData = gson.fromJson(jsonObjectString, Map.class);
                    
                    JournalEntry newEntry = new JournalEntry();
                    newEntry.setDate(today);
                    newEntry.setTitle(journalData.get("titulo"));
                    newEntry.setNarrativeSummary(journalData.get("resumo_narrativo"));
                    newEntry.setRelatedProposalIds(analyzedProposals.stream().map(Proposal::getOriginalId).collect(Collectors.toList()));
                    
                    journalRepository.save(newEntry);
                    logger.info("Successfully created and saved journal entry for {}.", today);
                } else {
                     logger.error("Could not find a valid JSON object in the journal LLM response.");
                }
            }
        } catch (Exception e) {
            logger.error("Failed to generate or save daily journal.", e);
        }
    }
}
