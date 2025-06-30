package com.prodash.service;

import com.prodash.dto.camara.ProposalDTO;
import com.prodash.dto.llm.LlmResult;
import com.prodash.model.Proposal;
import com.prodash.repository.ProposalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service containing the business logic for processing and storing proposals.
 * Now with batch-based LLM integration.
 */
@Service
public class ProposalService {

    private static final Logger logger = LoggerFactory.getLogger(ProposalService.class);
    private final ProposalRepository proposalRepository;
    private final CamaraApiService camaraApiService;
    private final LlmService llmService; // Inject the LLM service
    private static final int BATCH_SIZE = 10; // Process 10 proposals per LLM call

    public ProposalService(ProposalRepository proposalRepository, CamaraApiService camaraApiService, LlmService llmService) {
        this.proposalRepository = proposalRepository;
        this.camaraApiService = camaraApiService;
        this.llmService = llmService;
    }

    /**
     * Orchestrates a full backfill of data from a given start year to the current year.
     * @param startYear The year to start the backfill from.
     */
    public void backfillAllProposals(int startYear) {
        logger.info("STARTING FULL BACKFILL from year {} to present.", startYear);
        int currentYear = Year.now().getValue();
        for (int year = startYear; year <= currentYear; year++) {
            this.backfillProposalsForYear(year);
        }
        logger.info("COMPLETED FULL BACKFILL.");
    }

    /**
     * Orchestrates the backfilling of data for a single specific year.
     * @param year The year to backfill.
     */
    public void backfillProposalsForYear(int year) {
        logger.info("Starting backfill job for year {}...", year);
        List<ProposalDTO> proposals = camaraApiService.fetchProposalsByYear(year);
        if (!proposals.isEmpty()) {
            this.processAndSaveProposals(proposals);
        }
        logger.info("Finished backfill job for year {}.", year);
    }

    /**
     * Processes a list of DTOs, saves new ones, and sends them to the LLM in batches.
     * @param proposalDTOs The list of proposals fetched from the external API.
     */
    public void processAndSaveProposals(List<ProposalDTO> proposalDTOs) {
        List<Proposal> newProposalsToProcess = new ArrayList<>();

        // First, filter out existing proposals and create new ones to be saved.
        for (ProposalDTO dto : proposalDTOs) {
            Optional<Proposal> existingProposal = proposalRepository.findByOriginalId(dto.getId());
            if (existingProposal.isEmpty()) {
                Proposal newProposal = new Proposal();
                newProposal.setOriginalId(dto.getId());
                newProposal.setSiglaTipo(dto.getSiglaTipo());
                newProposal.setNumero(dto.getNumero());
                newProposal.setAno(dto.getAno());
                newProposal.setEmenta(dto.getEmenta());
                newProposal.setCreatedAt(LocalDateTime.now());
                newProposal.setUpdatedAt(LocalDateTime.now());
                // Leave LLM fields null for now
                newProposalsToProcess.add(newProposal);
            }
        }
        
        if (newProposalsToProcess.isEmpty()) {
            logger.info("No new proposals to process.");
            return;
        }

        logger.info("Found {} new proposals to process and analyze.", newProposalsToProcess.size());

        // Process the new proposals in batches to send to the LLM
        for (int i = 0; i < newProposalsToProcess.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, newProposalsToProcess.size());
            List<Proposal> batch = newProposalsToProcess.subList(i, end);

            // Get AI summaries and categories for the batch using a specific prompt
            List<LlmResult> llmResults = llmService.processBatch(batch, "batch_summary_v1");
            
            // Create a map for easy lookup of results by proposal ID
            Map<Long, LlmResult> resultMap = llmResults.stream()
                    .collect(Collectors.toMap(LlmResult::getId, r -> r, (r1, r2) -> r1)); // Handle potential duplicates

            // Update proposals in the batch with the LLM results
            for (Proposal proposal : batch) {
                LlmResult result = resultMap.get(proposal.getOriginalId());
                if (result != null) {
                    proposal.setSummaryLLM(result.getResumo());
                    proposal.setCategoryLLM(result.getCategoria());
                } else {
                    // Fallback if LLM fails for a specific item
                    proposal.setSummaryLLM("Análise de IA falhou.");
                    proposal.setCategoryLLM("Outros");
                    logger.warn("LLM result not found for proposal ID: {}", proposal.getOriginalId());
                }
            }
        }

        // Save all the processed new proposals to the database at once
        proposalRepository.saveAll(newProposalsToProcess);
        logger.info("Successfully saved {} new, analyzed proposals to the database.", newProposalsToProcess.size());
    }
}