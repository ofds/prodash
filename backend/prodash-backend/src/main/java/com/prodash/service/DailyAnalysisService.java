package com.prodash.service;

import com.prodash.dto.ProposalFilterDTO;
import com.prodash.dto.camara.ProposalDTO;
import com.prodash.dto.camara.ProposalDetailsDTO;
import com.prodash.dto.llm.LlmResult;
import com.prodash.model.Proposal;
import com.prodash.repository.ProposalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.domain.PageRequest;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DailyAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(DailyAnalysisService.class);
    private final MongoTemplate mongoTemplate;
    private final ProposalRepository proposalRepository;
    private final CamaraApiService camaraApiService;
    private final LlmService llmService;
    private static final int BATCH_SIZE = 10;

    public DailyAnalysisService(ProposalRepository proposalRepository, CamaraApiService camaraApiService, LlmService llmService, MongoTemplate mongoTemplate) {
        this.proposalRepository = proposalRepository;
        this.camaraApiService = camaraApiService;
        this.llmService = llmService;
        this.mongoTemplate = mongoTemplate;
    }

    public void analyzeTodaysProposals() {
        logger.info("--- Starting On-Demand Analysis for Today ---");

        ProposalFilterDTO filter = new ProposalFilterDTO();
        filter.setDataApresentacaoInicio(LocalDate.now());
        filter.setDataApresentacaoFim(LocalDate.now());
        List<ProposalDTO> proposalDTOs = camaraApiService.fetchProposalsByFilter(filter);

        if (proposalDTOs.isEmpty()) {
            logger.info("No new proposals found for today.");
            return;
        }

        List<Proposal> newProposals = new ArrayList<>();
        for (ProposalDTO dto : proposalDTOs) {
            if (proposalRepository.findByOriginalId(dto.getId()).isEmpty()) {
                ProposalDetailsDTO details = camaraApiService.fetchProposalDetails(dto.getId());
                if (details != null) {
                    Proposal p = new Proposal();
                    p.setOriginalId(details.getId());
                    p.setSiglaTipo(details.getSiglaTipo());
                    p.setNumero(details.getNumero());
                    p.setAno(details.getAno());
                    p.setEmenta(details.getEmenta());
                    p.setJustificativa(details.getJustificativa());
                    if (details.getStatusProposicao() != null) {
                        p.setStatusDescricao(details.getStatusProposicao().getDescricaoSituacao());
                    }
                    p.setCreatedAt(LocalDateTime.now());
                    newProposals.add(p);
                }
            }
        }

        if (newProposals.isEmpty()) {
            logger.info("All of today's proposals have already been ingested.");
            return;
        }

        logger.info("Found {} new proposals from today to analyze.", newProposals.size());

        for (int i = 0; i < newProposals.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, newProposals.size());
            List<Proposal> batch = newProposals.subList(i, end);

            List<LlmResult> llmResults = llmService.processBatch(batch, "proposal_analysis_v1");
            Map<Long, LlmResult> resultMap = llmResults.stream()
                    .collect(Collectors.toMap(LlmResult::getId, r -> r, (r1, r2) -> r1));

            for (Proposal proposal : batch) {
                LlmResult result = resultMap.get(proposal.getOriginalId());
                if (result != null) {
                    proposal.setSummaryLLM(result.getResumo());
                    proposal.setCategoryLLM(result.getCategoria());
                    proposal.setImpactoLLM(result.getImpacto());
                } else {
                    proposal.setSummaryLLM("Análise de IA falhou.");
                    proposal.setCategoryLLM("Outros");
                    proposal.setImpactoLLM("Não avaliado.");
                }
                proposal.setUpdatedAt(LocalDateTime.now());
            }
        }

        proposalRepository.saveAll(newProposals);
        logger.info("Successfully analyzed and saved {} new proposals from today.", newProposals.size());
    }
    /**
 * [NEW] Analyzes a batch of proposals to assign a numerical impact score.
 * @param limit The maximum number of proposals to process.
 */
public void analyzeProposalImpactScores(int limit) {
    logger.info("--- Starting On-Demand Job: Impact Score Analysis for {} proposals ---", limit);

    // Find proposals where the impact score has not yet been set
    Query query = new Query(Criteria.where("impactoScoreLLM").isNull());
    query.limit(limit);
    List<Proposal> proposalsToAnalyze = mongoTemplate.find(query, Proposal.class);

    if (proposalsToAnalyze.isEmpty()) {
        logger.info("No proposals found needing an impact score analysis.");
        return;
    }

    logger.info("Found {} proposals to analyze for impact score.", proposalsToAnalyze.size());

    // Process in batches
    for (int i = 0; i < proposalsToAnalyze.size(); i += BATCH_SIZE) {
        int end = Math.min(i + BATCH_SIZE, proposalsToAnalyze.size());
        List<Proposal> batch = proposalsToAnalyze.subList(i, end);

        List<LlmResult> llmResults = llmService.processBatch(batch, "proposal_impact_score_v1");
        
        if (llmResults.size() == batch.size()) {
            for (int j = 0; j < batch.size(); j++) {
                Proposal proposal = batch.get(j);
                LlmResult result = llmResults.get(j);

                if (result != null && result.getImpactoScore() != null) {
                    proposal.setImpactoScoreLLM(result.getImpactoScore());
                    // [NEW] Set the justification as well.
                    proposal.setImpactoJustificativaLLM(result.getJustificativa());
                } else {
                    proposal.setImpactoScoreLLM(-1); 
                    // [NEW] Add a default justification for failed cases.
                    proposal.setImpactoJustificativaLLM("Análise de IA falhou.");
                }
                proposal.setUpdatedAt(LocalDateTime.now());
            }
        } else {
            logger.error("LLM result count ({}) does not match batch size ({}). Marking batch as failed.", llmResults.size(), batch.size());
            for (Proposal proposal : batch) {
                proposal.setImpactoScoreLLM(-1);
                proposal.setImpactoJustificativaLLM("Falha na análise em lote.");
                proposal.setUpdatedAt(LocalDateTime.now());
            }
        }
    }

    proposalRepository.saveAll(proposalsToAnalyze);
    logger.info("Successfully analyzed and saved impact scores for {} proposals.", proposalsToAnalyze.size());
}
}
