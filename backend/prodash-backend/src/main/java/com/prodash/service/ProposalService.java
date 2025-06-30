package com.prodash.service;

import com.prodash.dto.ProposalFilterDTO;
import com.prodash.dto.camara.ProposalDTO;
import com.prodash.dto.llm.LlmResult;
import com.prodash.model.Proposal;
import com.prodash.repository.ProposalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProposalService {

    private static final Logger logger = LoggerFactory.getLogger(ProposalService.class);
    private final ProposalRepository proposalRepository;
    private final CamaraApiService camaraApiService;
    private final LlmService llmService;
    private static final int BATCH_SIZE = 10;

    public ProposalService(ProposalRepository proposalRepository, CamaraApiService camaraApiService, LlmService llmService) {
        this.proposalRepository = proposalRepository;
        this.camaraApiService = camaraApiService;
        this.llmService = llmService;
    }

    // ===================================================================
    // PHASE 1: INGESTION METHODS
    // ===================================================================

    /**
     * [NEW] Ingests proposals from the last few days. Called by the daily scheduler.
     */
    public void ingestRecentProposals() {
        logger.info("Starting scheduled job: Ingesting recent proposal data...");
        ProposalFilterDTO recentProposalsFilter = new ProposalFilterDTO();
        recentProposalsFilter.setDataApresentacaoInicio(LocalDate.now().minusDays(3));
        
        List<ProposalDTO> proposalsDTOs = camaraApiService.fetchProposalsByFilter(recentProposalsFilter);
        ingestRawProposals(proposalsDTOs);
        logger.info("Finished scheduled job: Ingesting recent proposal data.");
    }

    public void ingestAllProposals(int startYear) {
        logger.info("STARTING INGESTION-ONLY FULL BACKFILL from year {} to present.", startYear);
        int currentYear = Year.now().getValue();
        for (int year = startYear; year <= currentYear; year++) {
            this.ingestProposalsForYear(year);
        }
        logger.info("COMPLETED INGESTION-ONLY FULL BACKFILL.");
    }

    public void ingestProposalsForYear(int year) {
        logger.info("Starting ingestion-only job for year {}...", year);
        List<ProposalDTO> proposalsDTOs = camaraApiService.fetchProposalsByYear(year);
        ingestRawProposals(proposalsDTOs);
    }

    /**
     * Helper method to save raw proposals to the database without AI analysis.
     */
    private void ingestRawProposals(List<ProposalDTO> proposalsDTOs) {
        List<Proposal> newProposals = new ArrayList<>();
        for (ProposalDTO dto : proposalsDTOs) {
            if (proposalRepository.findByOriginalId(dto.getId()).isEmpty()) {
                Proposal newProposal = new Proposal();
                newProposal.setOriginalId(dto.getId());
                newProposal.setSiglaTipo(dto.getSiglaTipo());
                newProposal.setNumero(dto.getNumero());
                newProposal.setAno(dto.getAno());
                newProposal.setEmenta(dto.getEmenta());
                newProposal.setCreatedAt(LocalDateTime.now());
                newProposal.setUpdatedAt(LocalDateTime.now());
                newProposals.add(newProposal);
            }
        }

        if (!newProposals.isEmpty()) {
            proposalRepository.saveAll(newProposals);
            logger.info("Ingested and saved {} new proposals.", newProposals.size());
        } else {
            logger.info("No new proposals to ingest.");
        }
    }

    // ===================================================================
    // PHASE 2: AI PROCESSING METHODS
    // ===================================================================

    public void processUnanalyzedProposals(int limit) {
        logger.info("Starting job to process a batch of {} unanalyzed proposals.", limit);
        List<Proposal> proposalsToProcess = proposalRepository.findBySummaryLLMIsNull(PageRequest.of(0, limit));

        if (proposalsToProcess.isEmpty()) {
            logger.info("No unanalyzed proposals found to process. All done for now!");
            return;
        }

        logger.info("Found {} unanalyzed proposals. Processing in batches of {}.", proposalsToProcess.size(), BATCH_SIZE);

        for (int i = 0; i < proposalsToProcess.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, proposalsToProcess.size());
            List<Proposal> batch = proposalsToProcess.subList(i, end);

            List<LlmResult> llmResults = llmService.processBatch(batch, "batch_summary_v1");
            Map<Long, LlmResult> resultMap = llmResults.stream()
                    .collect(Collectors.toMap(LlmResult::getId, r -> r, (r1, r2) -> r1));

            for (Proposal proposal : batch) {
                LlmResult result = resultMap.get(proposal.getOriginalId());
                if (result != null) {
                    proposal.setSummaryLLM(result.getResumo());
                    proposal.setCategoryLLM(result.getCategoria());
                } else {
                    proposal.setSummaryLLM("Análise de IA falhou.");
                    proposal.setCategoryLLM("Outros");
                }
                proposal.setUpdatedAt(LocalDateTime.now());
            }
        }

        proposalRepository.saveAll(proposalsToProcess);
        logger.info("Successfully analyzed and updated {} proposals.", proposalsToProcess.size());
    }
}