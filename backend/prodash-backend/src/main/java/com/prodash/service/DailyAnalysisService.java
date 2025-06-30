package com.prodash.service;

import com.prodash.dto.ProposalFilterDTO;
import com.prodash.dto.camara.ProposalDTO;
import com.prodash.dto.llm.LlmResult;
import com.prodash.model.Proposal;
import com.prodash.repository.ProposalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DailyAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(DailyAnalysisService.class);
    private final ProposalRepository proposalRepository;
    private final CamaraApiService camaraApiService;
    private final LlmService llmService;
    private static final int BATCH_SIZE = 10;

    public DailyAnalysisService(ProposalRepository proposalRepository, CamaraApiService camaraApiService, LlmService llmService) {
        this.proposalRepository = proposalRepository;
        this.camaraApiService = camaraApiService;
        this.llmService = llmService;
    }

    /**
     * The main on-demand method. Fetches, analyzes, and saves today's proposals.
     */
    public void analyzeTodaysProposals() {
        logger.info("--- Starting On-Demand Analysis for Today ---");

        // 1. Fetch proposals presented today from the external API
        ProposalFilterDTO filter = new ProposalFilterDTO();
        filter.setDataApresentacaoInicio(LocalDate.now());
        filter.setDataApresentacaoFim(LocalDate.now());
        List<ProposalDTO> proposalDTOs = camaraApiService.fetchProposalsByFilter(filter);

        if (proposalDTOs.isEmpty()) {
            logger.info("No new proposals found for today.");
            return;
        }

        // 2. Filter out any that might already be in the DB and create new Proposal objects
        List<Proposal> newProposals = new ArrayList<>();
        for (ProposalDTO dto : proposalDTOs) {
            if (proposalRepository.findByOriginalId(dto.getId()).isEmpty()) {
                Proposal p = new Proposal();
                p.setOriginalId(dto.getId());
                p.setSiglaTipo(dto.getSiglaTipo());
                p.setNumero(dto.getNumero());
                p.setAno(dto.getAno());
                p.setEmenta(dto.getEmenta());
                p.setCreatedAt(LocalDateTime.now());
                newProposals.add(p);
            }
        }

        if (newProposals.isEmpty()) {
            logger.info("All of today's proposals have already been ingested.");
            return;
        }

        logger.info("Found {} new proposals from today to analyze.", newProposals.size());

        // 3. Process the new proposals in batches with the LLM
        for (int i = 0; i < newProposals.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, newProposals.size());
            List<Proposal> batch = newProposals.subList(i, end);

            // Use the new, more detailed analysis prompt
            List<LlmResult> llmResults = llmService.processBatch(batch, "proposal_analysis_v1");
            Map<Long, LlmResult> resultMap = llmResults.stream()
                    .collect(Collectors.toMap(LlmResult::getId, r -> r, (r1, r2) -> r1));

            for (Proposal proposal : batch) {
                LlmResult result = resultMap.get(proposal.getOriginalId());
                if (result != null) {
                    proposal.setSummaryLLM(result.getResumo());
                    proposal.setCategoryLLM(result.getCategoria());
                    proposal.setImpactoLLM(result.getImpacto()); // Save the new field
                } else {
                    proposal.setSummaryLLM("Análise de IA falhou.");
                    proposal.setCategoryLLM("Outros");
                    proposal.setImpactoLLM("Não avaliado.");
                }
                proposal.setUpdatedAt(LocalDateTime.now());
            }
        }

        // 4. Save all the fully analyzed proposals to the database
        proposalRepository.saveAll(newProposals);
        logger.info("Successfully analyzed and saved {} new proposals from today.", newProposals.size());
    }
}

