package com.prodash.service;

import com.prodash.dto.camara.ProposalDTO;
import com.prodash.model.Proposal;
import com.prodash.repository.ProposalRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


/**
 * Service containing the business logic for processing and storing proposals.
 */
@Service
public class ProposalService {

    private static final Logger logger = LoggerFactory.getLogger(ProposalService.class);
    private final ProposalRepository proposalRepository;
    // In a real application, you would inject an LLM service here.
    // private final LlmService llmService;

    public ProposalService(ProposalRepository proposalRepository) {
        this.proposalRepository = proposalRepository;
    }

    /**
     * Processes a list of DTOs from the API, transforms them, and saves them to the database.
     * @param proposalDTOs The list of proposals fetched from the external API.
     */
    public void processAndSaveProposals(List<ProposalDTO> proposalDTOs) {
        for (ProposalDTO dto : proposalDTOs) {
            // Check if this proposal already exists in our DB
            Optional<Proposal> existingProposal = proposalRepository.findByOriginalId(dto.getId());

            if (existingProposal.isEmpty()) {
                // If it's a new proposal, create and save it
                Proposal newProposal = new Proposal();
                newProposal.setOriginalId(dto.getId());
                newProposal.setSiglaTipo(dto.getSiglaTipo());
                newProposal.setNumero(dto.getNumero());
                newProposal.setAno(dto.getAno());
                newProposal.setEmenta(dto.getEmenta());
                newProposal.setCreatedAt(LocalDateTime.now());
                newProposal.setUpdatedAt(LocalDateTime.now());

                // ** AI INTEGRATION POINT **
                // Here you would call the LLM to get the summary and category
                // For now, we'll use placeholder values.
                // String summary = llmService.summarize(dto.getEmenta());
                // String category = llmService.categorize(dto.getEmenta());
                newProposal.setSummaryLLM("This is an AI-generated summary placeholder.");
                newProposal.setCategoryLLM("Uncategorized");

                proposalRepository.save(newProposal);
                logger.info("Saved new proposal with original ID: {}", dto.getId());
            } else {
                logger.info("Proposal with original ID {} already exists. Skipping.", dto.getId());
                // Optionally, you could add logic here to update existing proposals if needed.
            }
        }
    }
}