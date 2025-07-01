package com.prodash.service;

import com.prodash.dto.ProposalFilterDTO;
import com.prodash.dto.camara.ProposalDTO;
import com.prodash.dto.camara.ProposalDetailsDTO;
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
}
